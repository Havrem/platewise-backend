package com.havrem.platewise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havrem.platewise.config.GeminiProperties;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.ListSection;
import com.havrem.platewise.exception.ExternalServiceException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GeminiGroceryOrganizer implements GroceryOrganizer {
    private static final URI BASE_URI = URI.create("https://generativelanguage.googleapis.com");

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiGroceryOrganizer(GeminiProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public GroceryOrganization organize(ItemList list, List<Item> items, List<ListSection> existingSections) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ExternalServiceException("Gemini is not configured.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(generateContentUri())
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", properties.apiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody(list, items, existingSections))))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ExternalServiceException("Gemini could not organize this list.");
            }

            return parse(response.body());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ExternalServiceException("Gemini could not organize this list.", ex);
        }
    }

    private URI generateContentUri() {
        String model = (properties.model() == null || properties.model().isBlank())
                ? "gemini-2.5-flash"
                : properties.model();
        if (model.startsWith("models/")) {
            model = model.substring("models/".length());
        }
        String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8);
        return BASE_URI.resolve("/v1beta/models/" + encodedModel + ":generateContent");
    }

    private Map<String, Object> requestBody(ItemList list, List<Item> items, List<ListSection> existingSections) throws JsonProcessingException {
        return Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt(list, items, existingSections))))),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "responseMimeType", "application/json",
                        "responseJsonSchema", responseSchema()
                )
        );
    }

    private String prompt(ItemList list, List<Item> items, List<ListSection> existingSections) throws JsonProcessingException {
        List<PromptItem> promptItems = items.stream()
                .map(item -> new PromptItem(item.getId(), item.getText(), item.getCompleted()))
                .toList();
        List<PromptSection> promptSections = existingSections.stream()
                .map(section -> new PromptSection(section.getId(), section.getText()))
                .toList();

        return """
                Organize this grocery shopping list into food/store categories.

                Rules:
                - Return JSON only, matching the provided schema.
                - Preserve every item id exactly once.
                - Do not invent, remove, rewrite, or merge list items.
                - Section text must be short and user-editable.
                - Use labels in the same language as the item text when obvious.
                - Put unclear or non-food items in an "Other" section.
                - Existing sections are hints only; you may rename, remove, or replace them.

                List title: %s
                Items: %s
                Existing sections: %s
                """.formatted(
                list.getTitle(),
                objectMapper.writeValueAsString(promptItems),
                objectMapper.writeValueAsString(promptSections)
        );
    }

    private Map<String, Object> responseSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "sections", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "text", Map.of("type", "string"),
                                                "itemIds", Map.of(
                                                        "type", "array",
                                                        "items", Map.of("type", "integer")
                                                )
                                        ),
                                        "required", List.of("text", "itemIds")
                                )
                        )
                ),
                "required", List.of("sections")
        );
    }

    private GroceryOrganization parse(String body) throws JsonProcessingException {
        JsonNode textNode = objectMapper.readTree(body)
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (!textNode.isTextual()) {
            throw new ExternalServiceException("Gemini returned an unreadable organization.");
        }

        return objectMapper.readValue(textNode.asText(), GroceryOrganization.class);
    }

    private record PromptItem(Long id, String text, Boolean completed) {
    }

    private record PromptSection(Long id, String text) {
    }
}
