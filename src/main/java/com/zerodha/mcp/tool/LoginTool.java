package com.zerodha.mcp.tool;

import com.zerodha.mcp.service.KiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest.ContextInclusionStrategy;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginTool {
    private final KiteService kiteService;

    @Tool(
        name = "login",
        description = "Login to Zerodha Kite. Returns a login URL that the user must open in their browser " +
                     "to authenticate. After successful authentication, the user will be redirected back to " +
                     "complete the login process. This tool must be called before using any other Kite tools."
    )
    public Map<String, String> login(ToolContext context) {
        McpSyncServerExchange exchange = McpToolUtils.getMcpExchange(context).get();
        
        log.debug("MCP Tool: Initiating login");
        String loginUrl = kiteService.getLoginUrl();
        
        // Send a notification about the login process
        exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.INFO, "server", "Generating login URL for Zerodha Kite authentication"));

        // Create a message using CreateMessageRequest
      /*  TextContent textContent = new TextContent("Please wait while we prepare your login URL...");
        SamplingMessage message = new SamplingMessage(Role.ASSISTANT, textContent);
        CreateMessageRequest request = CreateMessageRequest.builder()
            .messages(Collections.singletonList(message))
            .includeContext(ContextInclusionStrategy.NONE)
            .build();
        exchange.createMessage(request);*/

        Map<String, String> response = Map.of(
            "loginUrl", loginUrl,
            "message", "Please open this URL in your browser to login. Once you've logged in successfully, you can proceed with other operations."
        );
        
        // Send a completion notification
        exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.INFO, "server", "Login URL generated successfully"));

        return response;
    }
}
