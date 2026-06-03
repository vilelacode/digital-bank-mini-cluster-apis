package com.digitalbank.notificationservice.config;

import com.digitalbank.notificationservice.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "app.rabbitmq.exchange=integration-test-exchange",
        "app.rabbitmq.queue=integration-test-queue",
        "app.rabbitmq.routing-key=integration.test.routing.key",
        "app.rabbitmq.dead-letter-exchange=integration-test-dlx",
        "app.rabbitmq.dead-letter-queue=integration-test-dlq",
        "app.rabbitmq.dead-letter-routing-key=integration.test.dlq.routing.key"
})
@DisplayName("RabbitMQ Configuration Integration Tests")
class RabbitMqConfigIntegrationTest {

    @Test
    @DisplayName("Should create notification exchange bean")
    void shouldCreateNotificationExchange(
            @org.springframework.beans.factory.annotation.Qualifier("notificationExchange")
            DirectExchange exchange
    ) {
        // Assert
        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo("integration-test-exchange");
        assertThat(exchange.isDurable()).isTrue();
    }

    @Test
    @DisplayName("Should create dead letter exchange bean")
    void shouldCreateDeadLetterExchange(
            @org.springframework.beans.factory.annotation.Qualifier("notificationDeadLetterExchange")
            DirectExchange exchange
    ) {
        // Assert
        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo("integration-test-dlx");
        assertThat(exchange.isDurable()).isTrue();
    }

    @Test
    @DisplayName("Should create notification queue bean")
    void shouldCreateNotificationQueue(
            @org.springframework.beans.factory.annotation.Qualifier("notificationQueue")
            Queue queue
    ) {
        // Assert
        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo("integration-test-queue");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("Should create dead letter queue bean")
    void shouldCreateDeadLetterQueue(
            @org.springframework.beans.factory.annotation.Qualifier("notificationDeadLetterQueue")
            Queue queue
    ) {
        // Assert
        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo("integration-test-dlq");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("Should create notification binding bean")
    void shouldCreateNotificationBinding(
            @org.springframework.beans.factory.annotation.Qualifier("notificationBinding")
            Binding binding
    ) {
        // Assert
        assertThat(binding).isNotNull();
        assertThat(binding.getDestination()).isEqualTo("integration-test-queue");
        assertThat(binding.getExchange()).isEqualTo("integration-test-exchange");
        assertThat(binding.getRoutingKey()).isEqualTo("integration.test.routing.key");
    }

    @Test
    @DisplayName("Should create dead letter binding bean")
    void shouldCreateDeadLetterBinding(
            @org.springframework.beans.factory.annotation.Qualifier("notificationDeadLetterBinding")
            Binding binding
    ) {
        // Assert
        assertThat(binding).isNotNull();
        assertThat(binding.getDestination()).isEqualTo("integration-test-dlq");
        assertThat(binding.getExchange()).isEqualTo("integration-test-dlx");
        assertThat(binding.getRoutingKey()).isEqualTo("integration.test.dlq.routing.key");
    }

    @Test
    @DisplayName("Should have connection factory available")
    void shouldHaveConnectionFactory(ConnectionFactory connectionFactory) {
        // Assert
        assertThat(connectionFactory).isNotNull();
    }
}

