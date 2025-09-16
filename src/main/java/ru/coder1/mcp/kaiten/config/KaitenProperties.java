package ru.coder1.mcp.kaiten.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kaiten")
public record KaitenProperties(
        String apiUrl,
        String apiToken
) {}