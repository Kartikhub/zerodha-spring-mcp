package com.zerodha.mcp.session;

import lombok.Data;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Data
@Component
//@SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@SessionScope
public class KiteSession {
    private String sessionId;
    private String accessToken;
    private String userId;
    private boolean authenticated;
}
