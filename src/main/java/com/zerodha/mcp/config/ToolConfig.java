package com.zerodha.mcp.config;

import com.zerodha.mcp.tool.HoldingsTool;
import com.zerodha.mcp.tool.LoginTool;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {
    
//    @Bean
//    public ToolCallbackProvider kiteTools(LoginTool loginTool, HoldingsTool holdingsTool) {
//        return MethodToolCallbackProvider.builder()
//            .toolObjects(loginTool, holdingsTool)
//            .build();
//    }
}
