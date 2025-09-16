package ru.coder1.mcp.kaiten.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@SuppressWarnings("all")
@RequiredArgsConstructor
public class KaitenClient {

    private final RestClient client;


    public Map<String, Object> getCard(String cardId) {
        return client.get()
                .uri("/cards/{id}", cardId)
                .retrieve()
                .body(Map.class);
    }

    public Map<String, Object> searchCards(String query) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/cards")
                        .queryParam("query", query).build())
                .retrieve()
                .body(Map.class);
    }
}