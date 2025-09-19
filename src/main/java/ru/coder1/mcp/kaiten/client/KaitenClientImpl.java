package ru.coder1.mcp.kaiten.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KaitenClientImpl implements KaitenClient {

    private final RestClient restClient;


    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    // ============ PUBLIC API ============

    @Override
    public Map<String, Object> getSpaces(Integer limit, Integer offset, Boolean includeArchived) {
        Map<String, Object> query = new LinkedHashMap<>();
        putIfNotNull(query, "limit", limit);
        putIfNotNull(query, "offset", offset);
        putIfNotNull(query, "include_archived", includeArchived);

        return get(new String[]{"spaces"}, query);
    }

    @Override
    public Map<String, Object> getBoards(Long spaceId, Integer limit, Integer offset, Boolean includeArchived) {
        Objects.requireNonNull(spaceId, "spaceId is required");

        Map<String, Object> query = new LinkedHashMap<>();
        query.put("space_id", spaceId);
        putIfNotNull(query, "limit", limit);
        putIfNotNull(query, "offset", offset);
        putIfNotNull(query, "include_archived", includeArchived);

        return get(new String[]{"space-boards"}, query);
    }

    @Override
    public Map<String, Object> listCards(Map<String, Object> params) {
        return get(new String[]{"cards"}, params);
    }

    @Override
    public Map<String, Object> getCard(String cardId, List<String> include) {
        Objects.requireNonNull(cardId, "cardId is required");
        Map<String, Object> q = new LinkedHashMap<>();
        if (include != null && !include.isEmpty()) {
            // include как CSV (если у вас повторяющиеся параметры, замените на q.put("include", include))
            q.put("include", String.join(",", include));
        }
        return get(new String[]{"cards", cardId}, q);
    }

    @Override
    public Map<String, Object> getTimesheets(Map<String, Object> params) {
        return get(new String[]{"timesheet"}, params);
    }

    // ============ LOW-LEVEL HELPERS ============

    private Map<String, Object> get(String[] segments, Map<String, Object> query) {
        try {
            return restClient.get()
                    .uri(ub -> buildUri(applySegments(ub, segments), query))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String path = "/" + String.join("/", segments);
                        throw new KaitenHttpException(res.getStatusCode().value(),
                                "HTTP " + res.getStatusCode().value() + " on GET " + path);
                    })
                    .body(MAP_TYPE);
        } catch (RestClientException e) {
            String path = "/" + String.join("/", segments);
            throw wrap(e, "GET " + path);
        }
    }

    /**
     * Применяет query-параметры к UriBuilder.
     * Поддерживает:
     *  - null → пропускаем
     *  - Iterable/массив → повторяющиеся параметры ?k=a&k=b
     *  - остальные → одно значение
     */
    private URI buildUri(UriBuilder builder, Map<String, Object> query) {
        UriBuilder b = builder;
        if (query != null) {
            for (Map.Entry<String, Object> e : query.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();
                if (val == null) continue;

                if (val instanceof CharSequence
                        || val instanceof Number
                        || val instanceof Boolean) {
                    b = b.queryParam(key, String.valueOf(val));
                } else if (val instanceof Iterable<?> it) {
                    for (Object v : it) {
                        if (v != null) b = b.queryParam(key, String.valueOf(v));
                    }
                } else if (val.getClass().isArray()) {
                    int len = java.lang.reflect.Array.getLength(val);
                    for (int i = 0; i < len; i++) {
                        Object v = java.lang.reflect.Array.get(val, i);
                        if (v != null) b = b.queryParam(key, String.valueOf(v));
                    }
                } else {
                    // fallback: toString
                    b = b.queryParam(key, String.valueOf(val));
                }
            }
        }
        return b.build();
    }

    private UriBuilder applySegments(UriBuilder builder, String[] segments) {
        UriBuilder current = builder;
        for (String segment : segments) {
            current = current.pathSegment(segment);
        }
        return current;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }

    private static RuntimeException wrap(Exception e, String action) {
        return new RuntimeException("Kaiten API call failed (" + action + "): " + e.getMessage(), e);
    }

    // ============ CUSTOM EXCEPTION (опционально) ============
    @Getter
    public static class KaitenHttpException extends RuntimeException {
        private final int status;

        public KaitenHttpException(int status, String message) {
            super(message);
            this.status = status;
        }
    }
}