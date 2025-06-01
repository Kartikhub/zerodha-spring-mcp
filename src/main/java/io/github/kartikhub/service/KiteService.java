package io.github.kartikhub.service;

import io.github.kartikhub.properties.KiteProperties;
import io.github.kartikhub.session.KiteSessionManager;
import io.github.kartikhub.exception.SessionNotFoundException;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Holding;
import com.zerodhatech.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class KiteService {
    private final KiteConnect kiteConnect;
    private final KiteProperties kiteProperties;
    private final KiteSessionManager sessionManager;

    public String getLoginUrl(String clientSessionId) {
        log.info("Generating Kite login URL for client session: {}", clientSessionId);
        sessionManager.createSession(clientSessionId, null, kiteProperties.getUserId());
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
            sessionManager.createSession(clientSessionId, user.accessToken, user.userId);
            sessionManager.setAuthenticated(clientSessionId, true);

            log.info("Successfully generated Kite session for user: {} with client session: {}", user.userId, clientSessionId);
        } catch (KiteException e) {
            handleKiteException("generate Kite session", clientSessionId, e);
        } catch (Exception e) {
            handleGenericException("generate Kite session", clientSessionId, e);
        }
    }


    public ArrayList<Holding> getHoldings(String clientSessionId) throws KiteException, IOException {
        return executeKiteApiCall(clientSessionId, "fetch holdings", kc -> {
            try {
                return new ArrayList<>(kc.getHoldings());
            } catch (KiteException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Helper method to execute authenticated Kite API calls with proper session handling
     */
    private <T> T executeKiteApiCall(String clientSessionId, String operation, Function<KiteConnect, T> apiCall) {
        validateSession(clientSessionId);
        try {
            log.debug("Performing Kite API operation '{}' for client session: {}", operation, clientSessionId);
            String accessToken = sessionManager.getAccessToken(clientSessionId);
            kiteConnect.setAccessToken(accessToken);
            return apiCall.apply(kiteConnect);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof KiteException) {
                handleKiteException(operation, clientSessionId, (KiteException) e.getCause());
            } else if (e.getCause() instanceof IOException) {
                handleIOException(operation, clientSessionId, (IOException) e.getCause());
            }
            handleGenericException(operation, clientSessionId, e);
            throw e; // This line won't be reached, but keeps the compiler happy
        }
    }

    private void validateSession(String clientSessionId) {
        if (!sessionManager.isAuthenticated(clientSessionId)) {
            String error = "Not authenticated with Kite. Please login first.";
            log.error("Session validation failed for client session {}: {}", clientSessionId, error);
            throw new SessionNotFoundException(error);
        }
    }

    private void handleKiteException(String operation, String clientSessionId, KiteException e) {
        String error = String.format("Failed to %s - KiteException (code: %d): %s", operation, e.code, e.getMessage());
        log.error("Error for session {}: {}", clientSessionId, error, e);
        throw new RuntimeException(error, e);
    }

    private void handleIOException(String operation, String clientSessionId, IOException e) {
        String error = String.format("Network error while trying to %s: %s", operation, e.getMessage());
        log.error("Error for session {}: {}", clientSessionId, error, e);
        throw new RuntimeException(error, e);
    }

    private void handleGenericException(String operation, String clientSessionId, Exception e) {
        String error = String.format("Failed to %s: %s", operation, e.getMessage());
        log.error("Error for session {}: {}", clientSessionId, error, e);
        throw new RuntimeException(error, e);
    }
}


