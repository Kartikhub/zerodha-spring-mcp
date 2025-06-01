package com.zerodha.mcp.tool;

import com.zerodha.mcp.service.KiteService;
import com.zerodha.mcp.session.TokenSessionMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginTool {
    private final KiteService kiteService;
    private final TokenSessionMapping tokenSessionMapping;

    @Tool(
        name = "login",
        description = "Login to Zerodha Kite. Returns a login URL that the user must open in their browser " +
                     "to authenticate. After successful authentication, the user will be redirected back to " +
                     "complete the login process. This tool must be called before using any other Kite tools."
    )
    public Map<String, String> login(ToolContext context) {
        McpSyncServerExchange exchange = McpToolUtils.getMcpExchange(context).get();
        String clientSessionId = UUID.randomUUID().toString();
        
        log.debug("MCP Tool: Initiating login for client session: {}", clientSessionId);
        String loginUrl = kiteService.getLoginUrl(clientSessionId);

        // Store the session mapping (the request token will be stored in callback)
        tokenSessionMapping.storeMapping(clientSessionId, clientSessionId);
        
        exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.INFO, "server", 
            String.format("Generating login URL for Zerodha Kite authentication (session: %s)", clientSessionId)));

        // Encode the client_session as a redirect parameter
        String redirectParams = "client_session=" + clientSessionId;
        String loginUrlWithParams = loginUrl + "&redirect_params=" + java.net.URLEncoder.encode(redirectParams, java.nio.charset.StandardCharsets.UTF_8);

        Map<String, String> response = Map.of(
            "loginUrl", loginUrlWithParams,
            "sessionId", clientSessionId,
            "message", "Please open this URL in your browser to login. Once you've logged in successfully, you can proceed with other operations."
        );
        
        // Send a completion notification
        exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.INFO, "server", 
            "Login URL generated successfully for session: " + clientSessionId));

        return response;
    }
}
