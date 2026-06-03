package com.digitalbank.notificationservice.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.idempotency")
public record IdempotencyProperties(
        String keyPrefix,
        Duration ttl
) {
}
