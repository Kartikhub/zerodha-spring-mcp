package io.github.kartikhub.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kite")
public class KiteProperties {
    private String apiKey;
    private String apiSecret;
    private String userId;
    private String callbackUrl;
}
