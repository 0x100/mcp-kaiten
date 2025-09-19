package ru.coder1.mcp.kaiten.client;

import java.util.List;
import java.util.Map;

public interface KaitenClient {

    // spaces
    Map<String, Object> getSpaces(Integer limit, Integer offset, Boolean includeArchived);

    // boards
    Map<String, Object> getBoards(Long spaceId, Integer limit, Integer offset, Boolean includeArchived);

    // cards list/search (retrieve-card-list)
    Map<String, Object> listCards(Map<String, Object> params);

    // card by id (retrieve-card) + optional include expansions
    Map<String, Object> getCard(String cardId, List<String> include);

    // timesheets (timesheet/get-list)
    Map<String, Object> getTimesheets(Map<String, Object> params);
}
