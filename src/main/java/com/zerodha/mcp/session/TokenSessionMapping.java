package com.zerodha.mcp.session;

import com.zerodha.mcp.exception.SessionNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TokenSessionMapping {
    private final Map<String, SessionInfo> tokenToSessionMap = new ConcurrentHashMap<>();
    private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(5); // Tokens expire after 5 minutes
    private final ScheduledExecutorService cleanupExecutor;

    public TokenSessionMapping() {
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.MINUTES);
    }

    public static class SessionInfo {
        private final String clientSessionId;
        private final Instant createdAt;

        public SessionInfo(String clientSessionId) {
            this.clientSessionId = clientSessionId;
            this.createdAt = Instant.now();
        }

        public String getClientSessionId() {
            return clientSessionId;
        }

        public boolean isExpired() {
            return Duration.between(createdAt, Instant.now()).compareTo(TOKEN_EXPIRY) > 0;
        }
    }

    public void storeMapping(String requestToken, String clientSessionId) {
        log.debug("Storing mapping: requestToken={}, clientSessionId={}", requestToken, clientSessionId);
        tokenToSessionMap.put(requestToken, new SessionInfo(clientSessionId));
    }

    public String getClientSessionId(String requestToken) {
        SessionInfo info = tokenToSessionMap.get(requestToken);
        if (info == null || info.isExpired()) {
            log.debug("No valid session found for requestToken: {}", requestToken);
            return null;
        }
        log.debug("Found session {} for requestToken: {}", info.getClientSessionId(), requestToken);
        return info.getClientSessionId();
    }

    public void validateSession(String clientSessionId) {
        boolean hasValidSession = tokenToSessionMap.values().stream()
            .anyMatch(info -> !info.isExpired() && clientSessionId.equals(info.getClientSessionId()));
        
        if (!hasValidSession) {
            log.error("No valid session found for clientSessionId: {}", clientSessionId);
            throw new SessionNotFoundException("No valid session found. Please login again.");
        }
    }

    private void cleanupExpiredTokens() {
        tokenToSessionMap.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                log.debug("Removing expired token mapping for requestToken: {}", entry.getKey());
            }
            return expired;
        });
    }
}
