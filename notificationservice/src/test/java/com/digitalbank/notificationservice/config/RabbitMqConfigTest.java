package com.digitalbank.notificationservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@TestPropertySource(properties = {
        "app.rabbitmq.exchange=test-exchange",
        "app.rabbitmq.queue=test-queue",
        "app.rabbitmq.routing-key=test.routing.key",
        "app.rabbitmq.dead-letter-exchange=test-dlx",
        "app.rabbitmq.dead-letter-queue=test-dlq",
        "app.rabbitmq.dead-letter-routing-key=test.dlq.routing.key"
})
@DisplayName("RabbitMqConfig Tests")
class RabbitMqConfigTest {

    private MessagingRabbitProperties properties;
    private RabbitMqConfig config;

    @BeforeEach
    void setUp() {
        properties = new MessagingRabbitProperties(
                "test-exchange",
                "test-queue",
                "test.routing.key",
                "test-dlx",
                "test-dlq",
                "test.dlq.routing.key"
        );
        config = new RabbitMqConfig();
    }

    @Test
    @DisplayName("Should create DirectExchange with correct properties")
    void shouldCreateDirectExchange() {
        // Act
        DirectExchange exchange = config.notificationExchange(properties);

        // Assert
        assertThat(exchange.getName()).isEqualTo("test-exchange");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    @DisplayName("Should create dead letter DirectExchange with correct properties")
    void shouldCreateDeadLetterDirectExchange() {
        // Act
        DirectExchange exchange = config.notificationDeadLetterExchange(properties);

        // Assert
        assertThat(exchange.getName()).isEqualTo("test-dlx");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    @DisplayName("Should create notification queue with dead letter configuration")
    void shouldCreateNotificationQueueWithDeadLetterConfig() {
        // Act
        Queue queue = config.notificationQueue(properties);

        // Assert
        assertThat(queue.getName()).isEqualTo("test-queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", "test-dlx")
                .containsEntry("x-dead-letter-routing-key", "test.dlq.routing.key");
    }

    @Test
    @DisplayName("Should create dead letter queue")
    void shouldCreateDeadLetterQueue() {
        // Act
        Queue queue = config.notificationDeadLetterQueue(properties);

        // Assert
        assertThat(queue.getName()).isEqualTo("test-dlq");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("Should create notification binding")
    void shouldCreateNotificationBinding() {
        // Arrange
        Queue queue = config.notificationQueue(properties);
        DirectExchange exchange = config.notificationExchange(properties);

        // Act
        Binding binding = config.notificationBinding(queue, exchange, properties);

        // Assert
        assertThat(binding.getDestination()).isEqualTo("test-queue");
        assertThat(binding.getExchange()).isEqualTo("test-exchange");
        assertThat(binding.getRoutingKey()).isEqualTo("test.routing.key");
    }

    @Test
    @DisplayName("Should create dead letter binding")
    void shouldCreateDeadLetterBinding() {
        // Arrange
        Queue dlQueue = config.notificationDeadLetterQueue(properties);
        DirectExchange dlExchange = config.notificationDeadLetterExchange(properties);

        // Act
        Binding binding = config.notificationDeadLetterBinding(dlQueue, dlExchange, properties);

        // Assert
        assertThat(binding.getDestination()).isEqualTo("test-dlq");
        assertThat(binding.getExchange()).isEqualTo("test-dlx");
        assertThat(binding.getRoutingKey()).isEqualTo("test.dlq.routing.key");
    }

    @Test
    @DisplayName("Should create Jackson JSON message converter")
    void shouldCreateJsonMessageConverter() {
        // Act
        MessageConverter converter = config.jsonMessageConverter();

        // Assert
        assertThat(converter).isNotNull();
        assertThat(converter.getClass().getSimpleName()).isEqualTo("JacksonJsonMessageConverter");
    }

    @Test
    @DisplayName("Should create RabbitListenerContainerFactory")
    void shouldCreateRabbitListenerContainerFactory() {
        // Arrange
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MessageConverter messageConverter = config.jsonMessageConverter();

        // Act
        var factory = config.rabbitListenerContainerFactory(connectionFactory, messageConverter);

        // Assert
        assertThat(factory).isNotNull();
        Object actualConnectionFactory = ReflectionTestUtils.getField(factory, "connectionFactory");
        assertThat(actualConnectionFactory).isEqualTo(connectionFactory);
    }

    @Test
    @DisplayName("Should use correct queue name from properties")
    void shouldUseCorrectQueueNameFromProperties() {
        // Arrange
        MessagingRabbitProperties customProperties = new MessagingRabbitProperties(
                "custom-exchange",
                "custom-queue",
                "custom.routing.key",
                "custom-dlx",
                "custom-dlq",
                "custom.dlq.routing.key"
        );

        // Act
        Queue queue = config.notificationQueue(customProperties);

        // Assert
        assertThat(queue.getName()).isEqualTo("custom-queue");
    }

    @Test
    @DisplayName("Should use correct exchange name from properties")
    void shouldUseCorrectExchangeNameFromProperties() {
        // Arrange
        MessagingRabbitProperties customProperties = new MessagingRabbitProperties(
                "custom-exchange",
                "custom-queue",
                "custom.routing.key",
                "custom-dlx",
                "custom-dlq",
                "custom.dlq.routing.key"
        );

        // Act
        DirectExchange exchange = config.notificationExchange(customProperties);

        // Assert
        assertThat(exchange.getName()).isEqualTo("custom-exchange");
    }

    @Test
    @DisplayName("Should queue be durable")
    void shouldQueueBeDurable() {
        // Act
        Queue queue = config.notificationQueue(properties);

        // Assert
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.isAutoDelete()).isFalse();
        assertThat(queue.isExclusive()).isFalse();
    }

    @Test
    @DisplayName("Should exchange be durable")
    void shouldExchangeBeDurable() {
        // Act
        DirectExchange exchange = config.notificationExchange(properties);

        // Assert
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }
}

