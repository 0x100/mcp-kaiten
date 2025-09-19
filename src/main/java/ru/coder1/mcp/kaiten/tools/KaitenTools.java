package ru.coder1.mcp.kaiten.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import ru.coder1.mcp.kaiten.client.KaitenClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KaitenTools {

    private final KaitenClient kaitenClient;

    // ---------- SPACES ----------
    @Tool(
            name = "kaiten.getSpaces",
            description = "Получить список пространств (spaces). Поддерживает пагинацию."
    )
    public Map<String, Object> getSpaces(
            @ToolParam(description = "Лимит записей на страницу (по умолчанию 50)") Integer limit,
            @ToolParam(description = "Смещение (offset) записей") Integer offset,
            @ToolParam(description = "Включать ли архивные пространства (true/false)") Boolean includeArchived
    ) {
        return kaitenClient.getSpaces(limit, offset, includeArchived);
    }

    // ---------- BOARDS ----------
    @Tool(
            name = "kaiten.getBoards",
            description = "Получить список досок (boards) в указанном пространстве. Поддерживает пагинацию."
    )
    public Map<String, Object> getBoards(
            @ToolParam(description = "ID пространства (spaceId)", required = true) Long spaceId,
            @ToolParam(description = "Лимит записей на страницу (по умолчанию 50)") Integer limit,
            @ToolParam(description = "Смещение (offset) записей") Integer offset,
            @ToolParam(description = "Включать ли архивные доски (true/false)") Boolean includeArchived
    ) {
        return kaitenClient.getBoards(spaceId, limit, offset, includeArchived);
    }

    // ---------- CARDS: LIST/SEARCH ----------
    @Tool(
            name = "kaiten.listCards",
            description = "Список карточек в рамках space/board с фильтрами (аналог retrieve-card-list). " +
                    "Минимально требуются spaceId и boardId."
    )
    public Map<String, Object> listCards(
            @ToolParam(description = "ID пространства (spaceId)", required = true) Long spaceId,
            @ToolParam(description = "ID доски (boardId)", required = true) Long boardId,
            @ToolParam(description = "Текстовый запрос (поиск по названию/описанию — зависит от настроек API)") String query,

            @ToolParam(description = "Фильтр по точному заголовку (если поддерживается)") String title,
            @ToolParam(description = "Внешний ID карточки (externalId)") String externalId,

            @ToolParam(description = "ID колонки (columnId)") Long columnId,
            @ToolParam(description = "ID дорожки (laneId)") Long laneId,
            @ToolParam(description = "ID исполнителей (assigneeIds), через запятую") String assigneeIdsCsv,
            @ToolParam(description = "ID владельца/автора (ownerId)") Long ownerId,
            @ToolParam(description = "ID тегов (tagIds), через запятую") String tagIdsCsv,
            @ToolParam(description = "Флаг ASAP (true/false)") Boolean asap,

            @ToolParam(description = "Фильтр по статусу/типу (если поддерживается, например status)") String status,
            @ToolParam(description = "Диапазон даты дедлайна: dueDateFrom (ISO 8601)") String dueDateFrom,
            @ToolParam(description = "Диапазон даты дедлайна: dueDateTo (ISO 8601)") String dueDateTo,
            @ToolParam(description = "Создано c: createdAtFrom (ISO 8601)") String createdAtFrom,
            @ToolParam(description = "Создано до: createdAtTo (ISO 8601)") String createdAtTo,
            @ToolParam(description = "Обновлено c: updatedAtFrom (ISO 8601)") String updatedAtFrom,
            @ToolParam(description = "Обновлено до: updatedAtTo (ISO 8601)") String updatedAtTo,

            @ToolParam(description = "Поле сортировки (orderBy), например: createdAt|updatedAt|dueDate|title") String orderBy,
            @ToolParam(description = "Направление сортировки (orderDir): asc|desc") String orderDir,
            @ToolParam(description = "Лимит записей на страницу (по умолчанию 50)") Integer limit,
            @ToolParam(description = "Смещение (offset) записей") Integer offset
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        // обязательные
        params.put("space_id", spaceId);
        params.put("board_id", boardId);
        // текстовый запрос
        putIfNotBlank(params, "query", query);

        // точные/доп. фильтры
        putIfNotBlank(params, "title", title);
        putIfNotBlank(params, "external_id", externalId);
        putIfNotNull(params, "column_id", columnId);
        putIfNotNull(params, "lane_id", laneId);
        putIfNotNull(params, "owner_id", ownerId);
        putIfNotNull(params, "asap", asap);
        putIfNotBlank(params, "status", status);

        // списки (CSV -> массив)
        if (assigneeIdsCsv != null && !assigneeIdsCsv.isBlank()) {
            params.put("assignee_ids[]", parseCsvToLongList(assigneeIdsCsv));
        }
        if (tagIdsCsv != null && !tagIdsCsv.isBlank()) {
            params.put("tag_ids[]", parseCsvToLongList(tagIdsCsv));
        }

        // даты/диапазоны
        putIfNotBlank(params, "due_date_from", dueDateFrom);
        putIfNotBlank(params, "due_date_to", dueDateTo);
        putIfNotBlank(params, "created_at_from", createdAtFrom);
        putIfNotBlank(params, "created_at_to", createdAtTo);
        putIfNotBlank(params, "updated_at_from", updatedAtFrom);
        putIfNotBlank(params, "updated_at_to", updatedAtTo);

        // сортировка/пагинация
        putIfNotBlank(params, "order_by", orderBy);
        putIfNotBlank(params, "order_dir", orderDir);
        putIfNotNull(params, "limit", limit);
        putIfNotNull(params, "offset", offset);

        return kaitenClient.listCards(params);
    }

    @Tool(
            name = "kaiten.searchCards",
            description = "Поиск карточек по текстовому запросу ВНУТРИ указанного пространства и доски."
    )
    public Map<String, Object> searchCards(
            @ToolParam(description = "ID пространства (spaceId)", required = true) Long spaceId,
            @ToolParam(description = "ID доски (boardId)", required = true) Long boardId,
            @ToolParam(description = "Текстовый запрос (query)", required = true) String query,
            @ToolParam(description = "Лимит записей на страницу") Integer limit,
            @ToolParam(description = "Смещение (offset) записей") Integer offset
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("space_id", spaceId);
        params.put("board_id", boardId);
        params.put("query", query);
        putIfNotNull(params, "limit", limit);
        putIfNotNull(params, "offset", offset);
        return kaitenClient.listCards(params);
    }

    // ---------- CARD: GET ----------
    @Tool(name = "kaiten.getCard", description = "Получить карточку по её ID (retrieve-card). Можно запросить расширенное представление.")
    public Map<String, Object> getCard(
            @ToolParam(description = "ID карточки (cardId)", required = true) String cardId,
            @ToolParam(description = "Список дополнительных полей/сущностей для расширения ответа (через запятую), напр. customProperties,tags,attachments") String includeCsv
    ) {
        List<String> include = includeCsv != null && !includeCsv.isBlank()
                ? Arrays.stream(includeCsv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
                : Collections.emptyList();
        return kaitenClient.getCard(cardId, include);
    }

    // ---------- TIMESHEETS ----------
    @Tool(
            name = "kaiten.getTimesheets",
            description = "Получить списания времени (timesheets) c фильтрами по датам/пользователям/карточкам. " +
                    "Поддерживает пагинацию."
    )
    public Map<String, Object> getTimesheets(
            @ToolParam(description = "Дата/время с (ISO 8601), напр. 2025-09-01T00:00:00Z") String from,
            @ToolParam(description = "Дата/время по (ISO 8601), напр. 2025-09-30T23:59:59Z") String to,
            @ToolParam(description = "ID пользователей (userIds), через запятую") String userIdsCsv,
            @ToolParam(description = "ID карточек (cardIds), через запятую") String cardIdsCsv,
            @ToolParam(description = "ID пространства (spaceId), опционально для узкой выборки") Long spaceId,
            @ToolParam(description = "ID доски (boardId), опционально для узкой выборки") Long boardId,
            @ToolParam(description = "Лимит записей на страницу (по умолчанию 50)") Integer limit,
            @ToolParam(description = "Смещение (offset) записей") Integer offset,
            @ToolParam(description = "Сортировка: поле (orderBy)") String orderBy,
            @ToolParam(description = "Сортировка: направление (orderDir): asc|desc") String orderDir
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        putIfNotBlank(params, "from", from);
        putIfNotBlank(params, "to", to);
        if (userIdsCsv != null && !userIdsCsv.isBlank()) {
            params.put("user_ids[]", parseCsvToLongList(userIdsCsv));
        }
        if (cardIdsCsv != null && !cardIdsCsv.isBlank()) {
            params.put("card_ids[]", parseCsvToLongList(cardIdsCsv));
        }
        putIfNotNull(params, "space_id", spaceId);
        putIfNotNull(params, "board_id", boardId);
        putIfNotNull(params, "limit", limit);
        putIfNotNull(params, "offset", offset);
        putIfNotBlank(params, "order_by", orderBy);
        putIfNotBlank(params, "order_dir", orderDir);
        return kaitenClient.getTimesheets(params);
    }

    // ---------- helpers ----------
    private static List<Long> parseCsvToLongList(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }

    private static void putIfNotBlank(Map<String, Object> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value);
        }
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }
}
