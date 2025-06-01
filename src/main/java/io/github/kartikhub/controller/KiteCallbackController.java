package io.github.kartikhub.controller;

import io.github.kartikhub.service.KiteService;
import io.github.kartikhub.session.KiteSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KiteCallbackController {
    private final KiteService kiteService;
    private final KiteSessionManager sessionManager;

    @GetMapping("/callback")
    public String handleCallback(
        @RequestParam("request_token") String requestToken,
        @RequestParam("client_session") String clientSessionId,
        @RequestParam("status") String status,
        @RequestParam(value = "type", defaultValue = "login") String type
    ) {
        try {
            if (!"success".equals(status)) {
                log.error("Login failed with status: {}", status);
                return "redirect:/error.html";
            }

            log.debug("Received callback with requestToken: {}, clientSessionId: {}, type: {}, status: {}",
                     requestToken, clientSessionId, type, status);

            // Update the session mapping with the request token
            sessionManager.storeTokenMapping(requestToken, clientSessionId);

            // Generate the session
            kiteService.generateSession(clientSessionId, requestToken);
            
            return "redirect:/success.html";
        } catch (Exception e) {
            log.error("Error in callback: {}", e.getMessage(), e);
            return "redirect:/error.html";
        }
    }
}
