package io.github.kartikhub.session;

import io.github.kartikhub.exception.SessionNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * @deprecated This class is being replaced by {@link KiteSessionManager} which consolidates
 * session and token management functionality. Use KiteSessionManager instead.
 */
@Deprecated(forRemoval = true)
@Slf4j
@Component
public class TokenSessionMapping {
    private final Map<String, SessionInfo> tokenToSessionMap = new ConcurrentHashMap<>();
    private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(5); // Tokens expire after 5 minutes

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
        if (clientSessionId == null || clientSessionId.isEmpty()) {
            log.error("Session ID cannot be null or empty");
            throw new SessionNotFoundException("Session ID is missing. Please login again.");
        }

        boolean hasValidSession = tokenToSessionMap.values().stream()
            .anyMatch(info -> !info.isExpired() && clientSessionId.equals(info.getClientSessionId()));
        
        if (!hasValidSession) {
            log.error("No valid session found for clientSessionId: {}", clientSessionId);
            throw new SessionNotFoundException("No valid session found. Please login again.");
        }
    }

    public void cleanupExpiredTokens() {
        log.info("Token cleanup job - this method is deprecated, use KiteSessionManager instead");
        int beforeSize = tokenToSessionMap.size();

        tokenToSessionMap.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                log.debug("Removing expired token mapping for requestToken: {}", entry.getKey());
            }
            return expired;
        });

        int removedCount = beforeSize - tokenToSessionMap.size();
        if (removedCount > 0) {
            log.info("Token cleanup complete. Removed {} expired tokens. {} active tokens remaining.",
                     removedCount, tokenToSessionMap.size());
        }
    }
}
