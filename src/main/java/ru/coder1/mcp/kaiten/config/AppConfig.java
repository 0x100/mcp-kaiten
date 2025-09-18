package ru.coder1.mcp.kaiten.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KaitenProperties.class)
public class AppConfig {

    @Bean
    RestClient kaitenRestClient(KaitenProperties props) {
        var apiUrl = props.apiUrl();
        var apiToken = props.apiToken();

        Assert.hasText(apiUrl, "KAITEN_API_URL is not configured");
        Assert.hasText(apiToken, "KAITEN_API_TOKEN is not configured");

        return RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
