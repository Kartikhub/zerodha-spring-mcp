package com.zerodha.mcp.controller;

import com.zerodha.mcp.service.KiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class KiteCallbackController {
    private final KiteService kiteService;

    @GetMapping("/callback")
    public String handleCallback(@RequestParam("request_token") String requestToken) {
        kiteService.generateSession(requestToken);
        return "redirect:/success.html";
    }
}
