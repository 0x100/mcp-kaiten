package ru.coder1.mcp.kaiten.client;

import java.util.List;
import java.util.Map;

public interface KaitenClient {

    Object getSpaces(Integer limit, Integer offset, Boolean includeArchived);

    Object getBoards(Long spaceId, Integer limit, Integer offset, Boolean includeArchived);

    Object listCards(Map<String, Object> params);

    Object getCard(String cardId, List<String> include);

    Object getTimesheets(Map<String, Object> params);
}
