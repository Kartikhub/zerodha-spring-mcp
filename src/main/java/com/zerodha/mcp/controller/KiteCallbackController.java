package com.zerodha.mcp.controller;

import com.zerodha.mcp.service.KiteService;
import com.zerodha.mcp.session.TokenSessionMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KiteCallbackController {
    private final KiteService kiteService;
    private final TokenSessionMapping tokenSessionMapping;

    @GetMapping("/callback")
    public String handleCallback(
        @RequestParam("request_token") String requestToken,
        @RequestParam("client_session") String clientSessionId,
        @RequestParam("status") String status,
        @RequestParam(value = "type", defaultValue = "login") String type,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (!"success".equals(status)) {
                log.error("Login failed with status: {}", status);
                return "redirect:/error";
            }

            log.debug("Received callback with requestToken: {}, clientSessionId: {}, type: {}, status: {}",
                     requestToken, clientSessionId, type, status);

            // Update the session mapping with the request token
            tokenSessionMapping.storeMapping(requestToken, clientSessionId);
            
            // Generate the session
            kiteService.generateSession(clientSessionId, requestToken);
            
            return "redirect:/success.html";
        } catch (Exception e) {
            log.error("Error in callback: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to authenticate: " + e.getMessage());
            return "redirect:/error.html";
        }
    }
}
