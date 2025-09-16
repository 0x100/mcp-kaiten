package ru.coder1.mcp.kaiten.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import ru.coder1.mcp.kaiten.client.KaitenClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KaitenTools {

    private final KaitenClient kaitenClient;


    @Tool(name = "kaiten.getCard", description = "Получить карточку по ее ID")
    public Map<String, Object> getCard(@ToolParam(description = "cardId") String cardId) {
        return kaitenClient.getCard(cardId);
    }

    @Tool(name = "kaiten.searchCards", description = "Поиск карточек по текстовому запросу")
    public Map<String, Object> searchCards(@ToolParam(description = "query") String query) {
        return kaitenClient.searchCards(query);
    }
}