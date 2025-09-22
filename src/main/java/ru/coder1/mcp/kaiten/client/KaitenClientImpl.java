package ru.coder1.mcp.kaiten.client;

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

import static java.lang.String.valueOf;

@Component
@RequiredArgsConstructor
public class KaitenClientImpl implements KaitenClient {

    private static final ParameterizedTypeReference<Object> ANY_JSON_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient restClient;


    @Override
    public Object getSpaces(Integer limit, Integer offset, Boolean includeArchived) {
        Map<String, Object> query = new LinkedHashMap<>();
        putIfNotNull(query, "limit", limit);
        putIfNotNull(query, "offset", offset);
        putIfNotNull(query, "include_archived", includeArchived);

        return get(new String[]{"spaces"}, query);
    }

    @Override
    public Object getBoards(Long spaceId, Integer limit, Integer offset, Boolean includeArchived) {
        Objects.requireNonNull(spaceId, "spaceId is required");

        Map<String, Object> query = new LinkedHashMap<>();
        query.put("space_id", spaceId);
        putIfNotNull(query, "limit", limit);
        putIfNotNull(query, "offset", offset);
        putIfNotNull(query, "include_archived", includeArchived);

        return get(new String[]{"space-boards"}, query);
    }

    @Override
    public Object listCards(Map<String, Object> params) {
        return get(new String[]{"cards"}, params);
    }

    @Override
    public Object getCard(String cardId, List<String> include) {
        Objects.requireNonNull(cardId, "cardId is required");
        Map<String, Object> q = new LinkedHashMap<>();
        if (include != null && !include.isEmpty()) {
            // include как CSV (если у вас повторяющиеся параметры, замените на q.put("include", include))
            q.put("include", String.join(",", include));
        }
        return get(new String[]{"cards", cardId}, q);
    }

    @Override
    public Object getTimesheets(Map<String, Object> params) {
        return get(new String[]{"timesheet"}, params);
    }

    // ============ LOW-LEVEL HELPERS ============

    private Object get(String[] segments, Map<String, Object> query) {
        try {
            return restClient.get()
                    .uri(ub -> buildUri(applySegments(ub, segments), query))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String path = "/" + String.join("/", segments);
                        throw new KaitenHttpException(res.getStatusCode().value(),
                                "HTTP %d on GET %s".formatted(res.getStatusCode().value(), path));
                    })
                    .body(ANY_JSON_TYPE);
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
        if (query != null) {
            for (var entry : query.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (value == null) continue;

                if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
                    builder = builder.queryParam(key, valueOf(value));
                } else if (value instanceof Iterable<?> it) {
                    for (var item : it) {
                        if (item != null) {
                            builder = builder.queryParam(key, valueOf(item));
                        }
                    }
                } else if (value.getClass().isArray()) {
                    var length = java.lang.reflect.Array.getLength(value);
                    for (int i = 0; i < length; i++) {
                        var val = java.lang.reflect.Array.get(value, i);
                        if (val != null) {
                            builder = builder.queryParam(key, valueOf(val));
                        }
                    }
                } else {
                    builder = builder.queryParam(key, valueOf(value));
                }
            }
        }
        return builder.build();
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
}
