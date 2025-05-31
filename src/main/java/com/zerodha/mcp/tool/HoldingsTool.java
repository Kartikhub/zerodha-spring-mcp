package com.zerodha.mcp.tool;

import com.zerodha.mcp.service.KiteService;
import com.zerodhatech.models.Holding;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest.ContextInclusionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldingsTool {
    private final KiteService kiteService;

    @Tool(
        name = "get_holdings",
        description = "Get the current user's portfolio holdings from Zerodha Kite. " +
                     "Returns a list of holdings with details like trading symbol, quantity, average price, " +
                     "last price, and P&L. Must be logged in first using the login tool."
    )
    public ArrayList<Holding> getHoldings(ToolContext context) {
        McpSyncServerExchange exchange = McpToolUtils.getMcpExchange(context).get();
        log.debug("MCP Tool: Fetching holdings");
        exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.INFO, "server", "Fetching portfolio holdings from Zerodha Kite"));

       /* // Create a message using CreateMessageRequest
        TextContent textContent = new TextContent("Retrieving your portfolio holdings...");
        SamplingMessage message = new SamplingMessage(Role.ASSISTANT, textContent);
        CreateMessageRequest request = CreateMessageRequest.builder()
            .messages(Collections.singletonList(message))
            .includeContext(ContextInclusionStrategy.NONE)
            .build();
        exchange.createMessage(request);*/

        try {
            ArrayList<Holding> holdings = kiteService.getHoldings();
            exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.INFO, "server", "Successfully retrieved " + holdings.size() + " holdings"));
            return holdings;
        } catch (Exception e) {
            exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.ERROR, "server", "Error fetching holdings: " + e.getMessage()));
            throw e;
        }
    }
}
