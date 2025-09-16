package ru.coder1.mcp.kaiten.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KaitenProperties.class)
public class AppConfig {

    @Bean
    RestClient kaitenRestClient(KaitenProperties props) {
        return RestClient.builder()
                .baseUrl(props.apiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiToken())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}