package com.zerodha.mcp;

import com.zerodha.mcp.tool.HoldingsTool;
import com.zerodha.mcp.tool.LoginTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class Application {

 /*   @Bean
    public ToolCallbackProvider kiteTools(LoginTool loginTool, HoldingsTool holdingsTool) {
        log.info("Registering Kite MCP tools");
        return MethodToolCallbackProvider.builder()
                .toolObjects(loginTool, holdingsTool)
                .build();
    }*/

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
