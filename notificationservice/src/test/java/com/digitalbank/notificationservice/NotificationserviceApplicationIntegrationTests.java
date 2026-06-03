package com.digitalbank.notificationservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "app.idempotency.key-prefix=notification:idempotency:",
        "app.idempotency.ttl=24h",
        "app.rabbitmq.exchange=test-exchange",
        "app.rabbitmq.queue=test-queue",
        "app.rabbitmq.routing-key=test.routing.key",
        "app.rabbitmq.dead-letter-exchange=test-dlx",
        "app.rabbitmq.dead-letter-queue=test-dlq",
        "app.rabbitmq.dead-letter-routing-key=test.dlq.routing.key"
})
@DisplayName("NotificationService Integration Tests")
class NotificationserviceApplicationIntegrationTests {

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        // Verify that the application context loads without errors
    }

    @Test
    @DisplayName("Application should be runnable")
    void applicationIsRunnable() {
        // The Spring context should be loaded and all beans should be created
        // This test verifies that the application can start up properly with TestContainers
    }
}

