package com.zerodha.mcp.config;

import com.zerodha.mcp.properties.KiteProperties;

import com.zerodhatech.kiteconnect.KiteConnect;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KiteProperties.class)
public class KiteConfig {

    @Bean
    public KiteConnect kiteConnect(KiteProperties kiteProperties) {
        KiteConnect kiteConnect = new KiteConnect(kiteProperties.getApiKey());
        kiteConnect.setUserId(kiteProperties.getUserId());
        return kiteConnect;
    }
}
