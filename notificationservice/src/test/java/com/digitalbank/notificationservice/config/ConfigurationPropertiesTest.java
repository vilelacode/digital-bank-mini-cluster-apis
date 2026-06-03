package com.digitalbank.notificationservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.idempotency.key-prefix=notification:idempotency:",
        "app.idempotency.ttl=24h",
        "app.rabbitmq.exchange=digital-bank.notification.exchange",
        "app.rabbitmq.queue=notification-service.queue",
        "app.rabbitmq.routing-key=notification.requested",
        "app.rabbitmq.dead-letter-exchange=digital-bank.notification.dlx",
        "app.rabbitmq.dead-letter-queue=notification-service.dlq",
        "app.rabbitmq.dead-letter-routing-key=notification.failed"
})
@DisplayName("Configuration Properties Tests")
class ConfigurationPropertiesTest {

    @Test
    @DisplayName("Should load IdempotencyProperties correctly")
    void shouldLoadIdempotencyProperties(IdempotencyProperties properties) {
        // Assert
        assertThat(properties).isNotNull();
        assertThat(properties.keyPrefix()).isEqualTo("notification:idempotency:");
        assertThat(properties.ttl()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    @DisplayName("Should load MessagingRabbitProperties correctly")
    void shouldLoadMessagingRabbitProperties(MessagingRabbitProperties properties) {
        // Assert
        assertThat(properties).isNotNull();
        assertThat(properties.exchange()).isEqualTo("digital-bank.notification.exchange");
        assertThat(properties.queue()).isEqualTo("notification-service.queue");
        assertThat(properties.routingKey()).isEqualTo("notification.requested");
        assertThat(properties.deadLetterExchange()).isEqualTo("digital-bank.notification.dlx");
        assertThat(properties.deadLetterQueue()).isEqualTo("notification-service.dlq");
        assertThat(properties.deadLetterRoutingKey()).isEqualTo("notification.failed");
    }

    @Test
    @DisplayName("Should create IdempotencyProperties with valid values")
    void shouldCreateIdempotencyPropertiesWithValidValues() {
        // Act
        IdempotencyProperties properties = new IdempotencyProperties(
                "test:prefix:",
                Duration.ofHours(12)
        );

        // Assert
        assertThat(properties.keyPrefix()).isEqualTo("test:prefix:");
        assertThat(properties.ttl()).isEqualTo(Duration.ofHours(12));
    }

    @Test
    @DisplayName("Should create MessagingRabbitProperties with valid values")
    void shouldCreateMessagingRabbitPropertiesWithValidValues() {
        // Act
        MessagingRabbitProperties properties = new MessagingRabbitProperties(
                "test-exchange",
                "test-queue",
                "test.routing.key",
                "test-dlx",
                "test-dlq",
                "test.dlq.routing.key"
        );

        // Assert
        assertThat(properties.exchange()).isEqualTo("test-exchange");
        assertThat(properties.queue()).isEqualTo("test-queue");
        assertThat(properties.routingKey()).isEqualTo("test.routing.key");
        assertThat(properties.deadLetterExchange()).isEqualTo("test-dlx");
        assertThat(properties.deadLetterQueue()).isEqualTo("test-dlq");
        assertThat(properties.deadLetterRoutingKey()).isEqualTo("test.dlq.routing.key");
    }

    @Test
    @DisplayName("Should IdempotencyProperties be a record with all components")
    void shouldIdempotencyPropertiesBeRecord() {
        // Arrange
        IdempotencyProperties properties1 = new IdempotencyProperties("prefix1:", Duration.ofHours(12));
        IdempotencyProperties properties2 = new IdempotencyProperties("prefix1:", Duration.ofHours(12));

        // Act & Assert - records should have equals and hashCode
        assertThat(properties1).isEqualTo(properties2);
        assertThat(properties1.hashCode()).isEqualTo(properties2.hashCode());
    }

    @Test
    @DisplayName("Should MessagingRabbitProperties be a record with all components")
    void shouldMessagingRabbitPropertiesBeRecord() {
        // Arrange
        MessagingRabbitProperties properties1 = new MessagingRabbitProperties(
                "exchange", "queue", "routing.key", "dlx", "dlq", "dlq.routing"
        );
        MessagingRabbitProperties properties2 = new MessagingRabbitProperties(
                "exchange", "queue", "routing.key", "dlx", "dlq", "dlq.routing"
        );

        // Act & Assert - records should have equals and hashCode
        assertThat(properties1).isEqualTo(properties2);
        assertThat(properties1.hashCode()).isEqualTo(properties2.hashCode());
    }

    @Test
    @DisplayName("Should distinguish different IdempotencyProperties")
    void shouldDistinguishDifferentIdempotencyProperties() {
        // Arrange
        IdempotencyProperties properties1 = new IdempotencyProperties("prefix1:", Duration.ofHours(12));
        IdempotencyProperties properties2 = new IdempotencyProperties("prefix2:", Duration.ofHours(12));

        // Act & Assert
        assertThat(properties1).isNotEqualTo(properties2);
        assertThat(properties1.hashCode()).isNotEqualTo(properties2.hashCode());
    }

    @Test
    @DisplayName("Should distinguish different MessagingRabbitProperties")
    void shouldDistinguishDifferentMessagingRabbitProperties() {
        // Arrange
        MessagingRabbitProperties properties1 = new MessagingRabbitProperties(
                "exchange1", "queue1", "routing.key", "dlx", "dlq", "dlq.routing"
        );
        MessagingRabbitProperties properties2 = new MessagingRabbitProperties(
                "exchange2", "queue2", "routing.key", "dlx", "dlq", "dlq.routing"
        );

        // Act & Assert
        assertThat(properties1).isNotEqualTo(properties2);
    }
}

