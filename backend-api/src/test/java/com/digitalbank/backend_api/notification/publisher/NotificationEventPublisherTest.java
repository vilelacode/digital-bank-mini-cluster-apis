package com.digitalbank.backend_api.notification.publisher;

import com.digitalbank.backend_api.notification.dto.AccountSummaryEvent;
import com.digitalbank.backend_api.notification.dto.RabbitProperties;
import com.digitalbank.backend_api.notification.dto.TransferCompletedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
class NotificationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitProperties properties;

    @Test
    void shouldSendEventToConfiguredExchangeAndRoutingKey() {
        NotificationEventPublisher publisher = new NotificationEventPublisher(
                rabbitTemplate,
                new RabbitProperties("bank.exchange", "transfer.completed")
        );

        TransferCompletedEvent event = new TransferCompletedEvent(
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                "TRANSFER_COMPLETED",
                UUID.fromString("66666666-6666-6666-6666-666666666666"),
                new AccountSummaryEvent(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Alice", "alice@bank.com"),
                new AccountSummaryEvent(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Bob", "bob@bank.com"),
                new BigDecimal("125.50"),
                "BRL",
                Instant.parse("2026-06-03T13:00:00Z")
        );

        publisher.publishTransferCompleted(event);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("bank.exchange"), eq("transfer.completed"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isEqualTo(event);
    }
}

