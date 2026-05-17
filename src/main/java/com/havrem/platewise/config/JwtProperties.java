package com.havrem.platewise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long accessTtlSeconds, long refreshTtlSeconds) {
}
