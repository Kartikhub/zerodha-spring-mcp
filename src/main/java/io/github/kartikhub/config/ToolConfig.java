package io.github.kartikhub.config;

import io.github.kartikhub.tool.HoldingsTool;
import io.github.kartikhub.tool.LoginTool;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {
    
    @Bean
    public ToolCallbackProvider kiteTools(LoginTool loginTool, HoldingsTool holdingsTool) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(loginTool, holdingsTool)
            .build();
    }
}
