package ru.coder1.mcp.kaiten.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.coder1.mcp.kaiten.tools.KaitenTools;

@Configuration
@RequiredArgsConstructor
public class McpConfig {

    private final KaitenTools kaitenTools;


    @Bean
    ToolCallbackProvider toolProvider() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(kaitenTools)
                .build();
    }
}