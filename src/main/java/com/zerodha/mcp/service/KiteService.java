package com.zerodha.mcp.service;

import com.zerodha.mcp.properties.KiteProperties;
import com.zerodha.mcp.session.KiteSession;
import com.zerodha.mcp.exception.SessionNotFoundException;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Holding;
import com.zerodhatech.models.Profile;
import com.zerodhatech.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

import io.modelcontextprotocol.server.McpServer;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class KiteService {
    private final KiteConnect kiteConnect;
    private final KiteProperties kiteProperties;
    private final KiteSession kiteSession;

    public String getLoginUrl(String clientSessionId) {
        log.info("Generating Kite login URL for client session: {}", clientSessionId);
        kiteSession.createSession(clientSessionId, null, kiteProperties.getUserId());
        return kiteConnect.getLoginURL();
    }

    public void generateSession(String clientSessionId, String requestToken) {
        try {
            log.info("Generating Kite session for client session: {}", clientSessionId);
            User user = kiteConnect.generateSession(requestToken, kiteProperties.getApiSecret());
            
            // Set tokens on the KiteConnect client
            kiteConnect.setAccessToken(user.accessToken);
            kiteConnect.setPublicToken(user.publicToken);
            
            // Update the existing session with the access token
            kiteSession.createSession(clientSessionId, user.accessToken, user.userId);
            kiteSession.setAuthenticated(clientSessionId, true);
            
            log.info("Successfully generated Kite session for user: {} with client session: {}", user.userId, clientSessionId);
        } catch (KiteException e) {
            log.error("Error generating Kite session - KiteException: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Kite session - KiteException: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error generating Kite session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Kite session: " + e.getMessage(), e);
        }
    }

    public Profile getProfile(String clientSessionId) {
        validateSession(clientSessionId);
        try {
            log.debug("Fetching Kite user profile for client session: {}", clientSessionId);
            String accessToken = kiteSession.getAccessToken(clientSessionId);
            kiteConnect.setAccessToken(accessToken);
            return kiteConnect.getProfile();
        } catch (KiteException | IOException e) {
            log.error("Error fetching Kite profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Kite profile: " + e.getMessage(), e);
        }
    }

    public ArrayList<Holding> getHoldings(String clientSessionId) throws KiteException, IOException {
        validateSession(clientSessionId);
        log.debug("Fetching Kite holdings for client session: {}", clientSessionId);
        String accessToken = kiteSession.getAccessToken(clientSessionId);
        kiteConnect.setAccessToken(accessToken);
        return (ArrayList<Holding>) kiteConnect.getHoldings();
    }

    private void validateSession(String clientSessionId) {
        if (!kiteSession.isAuthenticated(clientSessionId)) {
            String error = "Not authenticated with Kite. Please login first.";
            log.error("Session validation failed for client session {}: {}", clientSessionId, error);
            throw new SessionNotFoundException(error);
        }
    }
}
