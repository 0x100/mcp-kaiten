package ru.coder1.mcp.kaiten.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import ru.coder1.mcp.kaiten.client.KaitenClientImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KaitenTools {

    private final KaitenClientImpl kaitenClient;

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
        params.put("spaceId", spaceId);
        params.put("boardId", boardId);
        // текстовый запрос
        if (query != null && !query.isBlank()) params.put("query", query);

        // точные/доп. фильтры
        if (title != null) params.put("title", title);
        if (externalId != null) params.put("externalId", externalId);
        if (columnId != null) params.put("columnId", columnId);
        if (laneId != null) params.put("laneId", laneId);
        if (ownerId != null) params.put("ownerId", ownerId);
        if (asap != null) params.put("asap", asap);
        if (status != null) params.put("status", status);

        // списки (CSV -> массив)
        if (assigneeIdsCsv != null && !assigneeIdsCsv.isBlank()) {
            params.put("assigneeIds", parseCsvToLongList(assigneeIdsCsv));
        }
        if (tagIdsCsv != null && !tagIdsCsv.isBlank()) {
            params.put("tagIds", parseCsvToLongList(tagIdsCsv));
        }

        // даты/диапазоны
        if (dueDateFrom != null) params.put("dueDateFrom", dueDateFrom);
        if (dueDateTo != null) params.put("dueDateTo", dueDateTo);
        if (createdAtFrom != null) params.put("createdAtFrom", createdAtFrom);
        if (createdAtTo != null) params.put("createdAtTo", createdAtTo);
        if (updatedAtFrom != null) params.put("updatedAtFrom", updatedAtFrom);
        if (updatedAtTo != null) params.put("updatedAtTo", updatedAtTo);

        // сортировка/пагинация
        if (orderBy != null) params.put("orderBy", orderBy);
        if (orderDir != null) params.put("orderDir", orderDir);
        if (limit != null) params.put("limit", limit);
        if (offset != null) params.put("offset", offset);

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
        params.put("spaceId", spaceId);
        params.put("boardId", boardId);
        params.put("query", query);
        if (limit != null) params.put("limit", limit);
        if (offset != null) params.put("offset", offset);
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
        if (from != null) params.put("from", from);
        if (to != null) params.put("to", to);
        if (userIdsCsv != null && !userIdsCsv.isBlank()) {
            params.put("userIds", parseCsvToLongList(userIdsCsv));
        }
        if (cardIdsCsv != null && !cardIdsCsv.isBlank()) {
            params.put("cardIds", parseCsvToLongList(cardIdsCsv));
        }
        if (spaceId != null) params.put("spaceId", spaceId);
        if (boardId != null) params.put("boardId", boardId);
        if (limit != null) params.put("limit", limit);
        if (offset != null) params.put("offset", offset);
        if (orderBy != null) params.put("orderBy", orderBy);
        if (orderDir != null) params.put("orderDir", orderDir);
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
}