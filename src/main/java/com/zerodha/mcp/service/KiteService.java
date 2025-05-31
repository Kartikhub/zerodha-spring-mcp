package com.zerodha.mcp.service;

import com.zerodha.mcp.properties.KiteProperties;
import com.zerodha.mcp.session.KiteSession;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Holding;
import com.zerodhatech.models.Profile;
import com.zerodhatech.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class KiteService {
    private final KiteConnect kiteConnect;
    private final KiteProperties kiteProperties;
    private final KiteSession kiteSession;

    public String getLoginUrl() {
        log.info("Generating Kite login URL");
        return kiteConnect.getLoginURL();
    }

    public void generateSession(String requestToken) {
        try {
            log.info("Generating Kite session for request token");
            User user = kiteConnect.generateSession(requestToken, kiteProperties.getApiSecret());
            
            // Set tokens on the KiteConnect client
            kiteConnect.setAccessToken(user.accessToken);
            kiteConnect.setPublicToken(user.publicToken);
            
            // Update session state
            kiteSession.setAccessToken(user.accessToken);
            kiteSession.setUserId(user.userId);
            kiteSession.setAuthenticated(true);
            
            log.info("Successfully generated Kite session for user: {}", user.userId);
        }  catch (KiteException e) {
            log.error("Error generating Kite session - KiteException: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Kite session - KiteException: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error generating Kite session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Kite session: " + e.getMessage(), e);
        }
    }

    public Profile getProfile() {
        validateSession();
        try {
            log.debug("Fetching Kite user profile");
            return kiteConnect.getProfile();
        } catch (KiteException | IOException e) {
            log.error("Error fetching Kite profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Kite profile: " + e.getMessage(), e);
        }
    }

    public ArrayList<Holding> getHoldings() {
        validateSession();
        try {
            log.debug("Fetching Kite holdings");
            return (ArrayList<Holding>) kiteConnect.getHoldings();
        } catch (Exception | KiteException e) {
            log.error("Error fetching Kite holdings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Kite holdings: " + e.getMessage(), e);
        }
    }

    private void validateSession() {
        if (!kiteSession.isAuthenticated()) {
            log.error("Kite session is not authenticated");
            throw new RuntimeException("Not authenticated with Kite. Please login first.");
        }
    }
}
