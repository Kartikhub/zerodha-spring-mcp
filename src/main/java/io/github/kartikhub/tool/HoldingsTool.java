package io.github.kartikhub.tool;

import io.github.kartikhub.service.KiteService;
import io.github.kartikhub.exception.SessionNotFoundException;
import io.github.kartikhub.session.KiteSessionManager;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Holding;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldingsTool {
    private final KiteService kiteService;
    private final KiteSessionManager sessionManager;

    @Tool(
        name = "get_holdings",
        description = "Get the current user's portfolio holdings from Zerodha Kite. " +
                     "Returns a list of holdings with details like trading symbol, quantity, average price, " +
                     "last price, and P&L. Must be logged in first using the login tool. " +
                     "Requires the session ID returned from the login tool."
    )
    public ArrayList<Holding> getHoldings(String sessionId, ToolContext context) {
        McpSyncServerExchange exchange = McpToolUtils.getMcpExchange(context).get();
        String clientSessionId = sessionId;
        
        log.debug("MCP Tool: Fetching holdings for client session: {}", clientSessionId);
        exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.INFO, "server", 
            String.format("Fetching portfolio holdings for session: %s", clientSessionId)));

        try {
            ArrayList<Holding> holdings = kiteService.getHoldings(clientSessionId);
            String message = String.format("Successfully retrieved %d holdings for session: %s", holdings.size(), clientSessionId);
            exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.INFO, "server", message));
            return holdings;
        } catch (SessionNotFoundException e) {
            String error = String.format("Session not found for %s. Please log in using the login tool first.", clientSessionId);
            log.error(error, e);
            exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.ERROR, "server", error));
            throw new IllegalStateException(error, e);
        } catch (KiteException e) {
            String error = String.format("Kite API error (code: %d): %s", e.code, e.getMessage());
            log.error("Error for session {}: {}", clientSessionId, error, e);
            exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.ERROR, "server", error));
            throw new RuntimeException(error, e);
        } catch (IOException e) {
            String error = String.format("Network error while fetching holdings for session %s: %s", clientSessionId, e.getMessage());
            log.error(error, e);
            exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.ERROR, "server", error));
            throw new RuntimeException(error, e);
        } catch (Exception e) {
            String error = String.format("Unexpected error while fetching holdings for session %s: %s", clientSessionId, e.getMessage());
            log.error(error, e);
            exchange.loggingNotification(new LoggingMessageNotification(LoggingLevel.ERROR, "server", error));
            throw new RuntimeException(error, e);
        }
    }
}
