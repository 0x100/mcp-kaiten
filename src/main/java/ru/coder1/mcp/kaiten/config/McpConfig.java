package ru.coder1.mcp.kaiten.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.coder1.mcp.kaiten.tools.KaitenTools;

@Configuration
public class McpConfig {

    @Bean
    ToolCallbackProvider toolProvider(KaitenTools kaitenTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(kaitenTools)
                .build();
    }
}