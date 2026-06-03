package com.digitalbank.backend_api.notification.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitProperties(
        String exchange,
        String routingKey
) {
}
