package com.zerodha.mcp.session;

import com.zerodha.mcp.exception.SessionNotFoundException;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;

/**
 * Unified session manager for Kite API sessions.
 * Handles both the OAuth flow token mappings and authenticated API sessions.
 */
@Component
public class KiteSessionManager {
    private static final Logger log = LoggerFactory.getLogger(KiteSessionManager.class);
    private static final Duration SESSION_TIMEOUT = Duration.ofHours(6);
    private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(5);

    // Thread-safe maps to store session data and token mappings
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    private final Map<String, TokenMapping> tokenMappings = new ConcurrentHashMap<>();

    @Data
    public static class SessionData {
        private final String sessionId;
        private String accessToken;
        private final String userId;
        private boolean authenticated;
        private Instant lastAccessed;

        public SessionData(String sessionId, String accessToken, String userId) {
            this.sessionId = sessionId;
            this.accessToken = accessToken;
            this.userId = userId;
            this.authenticated = false;
            this.lastAccessed = Instant.now();
        }

        public void touch() {
            this.lastAccessed = Instant.now();
        }

        public boolean isExpired() {
            return Duration.between(lastAccessed, Instant.now()).compareTo(SESSION_TIMEOUT) > 0;
        }
    }

    @Data
    public static class TokenMapping {
        private final String clientSessionId;
        private final Instant createdAt;

        public TokenMapping(String clientSessionId) {
            this.clientSessionId = clientSessionId;
            this.createdAt = Instant.now();
        }

        public boolean isExpired() {
            return Duration.between(createdAt, Instant.now()).compareTo(TOKEN_EXPIRY) > 0;
        }
    }

    // Session management methods

    public void createSession(String sessionId, String accessToken, String userId) {
        if (sessionId == null || sessionId.isEmpty()) {
            log.error("Cannot create session: sessionId is null or empty");
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (userId == null || userId.isEmpty()) {
            log.error("Cannot create session: userId is null or empty");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        log.debug("Creating new session for user: {} with session ID: {}", userId, sessionId);
        sessions.put(sessionId, new SessionData(sessionId, accessToken, userId));
    }

    public SessionData getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            log.error("Cannot get session: sessionId is null or empty");
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        SessionData session = sessions.get(sessionId);
        if (session == null) {
            log.debug("Session not found: {}", sessionId);
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }

        if (session.isExpired()) {
            log.debug("Session expired: {}", sessionId);
            removeSession(sessionId);
            throw new SessionNotFoundException("Session expired: " + sessionId);
        }

        session.touch();
        return session;
    }

    public void removeSession(String sessionId) {
        log.debug("Removing session: {}", sessionId);
        sessions.remove(sessionId);
    }

    public boolean isAuthenticated(String sessionId) {
        SessionData session = getSession(sessionId);
        return session.isAuthenticated();
    }

    public void setAuthenticated(String sessionId, boolean authenticated) {
        SessionData session = getSession(sessionId);
        session.setAuthenticated(authenticated);
        log.debug("Session {} authentication status set to: {}", sessionId, authenticated);
    }

    public String getUserId(String sessionId) {
        return getSession(sessionId).getUserId();
    }

    public String getAccessToken(String sessionId) {
        return getSession(sessionId).getAccessToken();
    }

    // Token mapping methods (previously in TokenSessionMapping)

    public void storeTokenMapping(String requestToken, String clientSessionId) {
        log.debug("Storing token mapping: requestToken={}, clientSessionId={}", requestToken, clientSessionId);
        tokenMappings.put(requestToken, new TokenMapping(clientSessionId));
    }

    public String getClientSessionIdFromToken(String requestToken) {
        TokenMapping mapping = tokenMappings.get(requestToken);
        if (mapping == null || mapping.isExpired()) {
            log.debug("No valid token mapping found for requestToken: {}", requestToken);
            return null;
        }
        log.debug("Found session {} for requestToken: {}", mapping.getClientSessionId(), requestToken);
        return mapping.getClientSessionId();
    }

    public void validateClientSession(String clientSessionId) {
        if (clientSessionId == null || clientSessionId.isEmpty()) {
            log.error("Session ID cannot be null or empty");
            throw new SessionNotFoundException("Session ID is missing. Please login again.");
        }

        boolean hasValidSession = sessions.containsKey(clientSessionId) &&
                                 !sessions.get(clientSessionId).isExpired() &&
                                 sessions.get(clientSessionId).isAuthenticated();

        if (!hasValidSession) {
            log.error("No valid session found for clientSessionId: {}", clientSessionId);
            throw new SessionNotFoundException("No valid session found. Please login again.");
        }
    }

    // Cleanup schedules

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredSessions() {
        log.info("Running session cleanup job");
        int beforeSize = sessions.size();

        sessions.entrySet()
            .removeIf(entry -> {
                boolean expired = entry.getValue().isExpired();
                if (expired) {
                    log.debug("Cleaning up expired session: {}", entry.getKey());
                }
                return expired;
            });

        int removedCount = beforeSize - sessions.size();
        if (removedCount > 0) {
            log.info("Session cleanup complete. Removed {} expired sessions. {} active sessions remaining.",
                     removedCount, sessions.size());
        }
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupExpiredTokens() {
        int beforeSize = tokenMappings.size();

        tokenMappings.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                log.debug("Removing expired token mapping for requestToken: {}", entry.getKey());
            }
            return expired;
        });

        int removedCount = beforeSize - tokenMappings.size();
        if (removedCount > 0) {
            log.info("Token cleanup complete. Removed {} expired tokens. {} active tokens remaining.",
                     removedCount, tokenMappings.size());
        }
    }
}
