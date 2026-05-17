package com.havrem.platewise.integration;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.invite.CreateInviteRequest;
import com.havrem.platewise.dto.invite.InviteDto;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.ItemList;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class InviteIntegrationTest extends IntegrationTestBase {

    @Test
    void inviteAcceptFlow_addsRecipientAsMemberAndShowsListInTheirShared() {
        String emailA = uniqueEmail();
        String tokenA = signupAndGetToken(emailA);
        String emailB = uniqueEmail();
        String tokenB = signupAndGetToken(emailB);

        Long categoryA = createCategory(tokenA, "Groceries");
        Long listId = createItemList(tokenA, "Weekly", categoryA);

        // A invites B
        client.post().uri("/item-lists/" + listId + "/invites")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateInviteRequest(emailB))
                .exchange()
                .expectStatus().isCreated();

        // B sees the invite
        InviteDto[] invites = client.get().uri("/invites")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InviteDto[].class)
                .returnResult()
                .getResponseBody();
        assertThat(invites).isNotNull().hasSize(1);
        assertThat(invites[0].listId()).isEqualTo(listId);
        assertThat(invites[0].inviterEmail()).isEqualTo(emailA);

        // B accepts
        client.post().uri("/invites/" + invites[0].id() + "/accept")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNoContent();

        // B can now read the list
        ItemListDto bView = client.get().uri("/item-lists/" + listId)
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemListDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(bView).isNotNull();
        assertThat(bView.title()).isEqualTo("Weekly");
        assertThat(bView.type()).isEqualTo(ItemList.Type.GROCERY);
        // B sees the list under their own Shared category, not A's "Groceries"
        assertThat(bView.category().kind()).isEqualTo(Category.Kind.SHARED);

        // Invite is gone after accept
        client.get().uri("/invites")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void invite_nonOwnerCannotInvite() {
        String tokenA = signupAndGetToken(uniqueEmail());
        String emailB = uniqueEmail();
        String tokenB = signupAndGetToken(emailB);
        String emailC = uniqueEmail();
        signupAndGetToken(emailC);

        Long categoryA = createCategory(tokenA, "Groceries");
        Long listId = createItemList(tokenA, "Weekly", categoryA);

        // A invites B
        client.post().uri("/item-lists/" + listId + "/invites")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateInviteRequest(emailB))
                .exchange()
                .expectStatus().isCreated();

        InviteDto[] invites = client.get().uri("/invites")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectBody(InviteDto[].class)
                .returnResult()
                .getResponseBody();
        client.post().uri("/invites/" + invites[0].id() + "/accept")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNoContent();

        // B (member, not owner) tries to invite C
        client.post().uri("/item-lists/" + listId + "/invites")
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateInviteRequest(emailC))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void invite_decline_removesInviteWithoutAddingMember() {
        String tokenA = signupAndGetToken(uniqueEmail());
        String emailB = uniqueEmail();
        String tokenB = signupAndGetToken(emailB);

        Long categoryA = createCategory(tokenA, "Groceries");
        Long listId = createItemList(tokenA, "Weekly", categoryA);

        client.post().uri("/item-lists/" + listId + "/invites")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateInviteRequest(emailB))
                .exchange()
                .expectStatus().isCreated();

        InviteDto[] invites = client.get().uri("/invites")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectBody(InviteDto[].class)
                .returnResult()
                .getResponseBody();
        client.post().uri("/invites/" + invites[0].id() + "/decline")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNoContent();

        // B cannot read the list
        client.get().uri("/item-lists/" + listId)
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNotFound();
    }

    private Long createCategory(String token, String name) {
        CategoryDto created = client.post().uri("/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCategoryRequest(name, "icon"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CategoryDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        return created.id();
    }

    private Long createItemList(String token, String title, Long categoryId) {
        ItemListDto created = client.post().uri("/item-lists")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemListRequest(title, categoryId, ItemList.Type.GROCERY))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemListDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        return created.id();
    }
}
