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

@Component
public class KiteSession {
    private static final Logger log = LoggerFactory.getLogger(KiteSession.class);
    private static final Duration SESSION_TIMEOUT = Duration.ofHours(6);
    
    // Thread-safe map to store session data
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    
    @Data
    public static class SessionData {
        private String sessionId;
        private String accessToken;
        private String userId;
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
    
    public void createSession(String sessionId, String accessToken, String userId) {
        log.debug("Creating new session for user: {} with session ID: {}", userId, sessionId);
        sessions.put(sessionId, new SessionData(sessionId, accessToken, userId));
    }
    
    public SessionData getSession(String sessionId) {
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
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredSessions() {
        log.debug("Running session cleanup");
        sessions.entrySet()
            .removeIf(entry -> {
                boolean expired = entry.getValue().isExpired();
                if (expired) {
                    log.debug("Cleaning up expired session: {}", entry.getKey());
                }
                return expired;
            });
    }
}
