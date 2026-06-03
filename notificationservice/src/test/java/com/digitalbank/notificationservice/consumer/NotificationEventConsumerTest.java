package com.digitalbank.notificationservice.consumer;

import com.digitalbank.notificationservice.domain.event.NotificationEvent;
import com.digitalbank.notificationservice.domain.event.NotificationEventType;
import com.digitalbank.notificationservice.idempotency.IdempotencyService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventConsumer Tests")
class NotificationEventConsumerTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private Channel channel;

    private NotificationEventConsumer consumer;

    private NotificationEvent event;

    @BeforeEach
    void setUp() {
        consumer = new NotificationEventConsumer(idempotencyService);
        event = createTestEvent();
    }

    @Test
    @DisplayName("Should successfully consume and process notification event when acquired")
    void shouldConsumeAndProcessEventWhenAcquired() throws IOException {
        // Arrange
        long deliveryTag = 1L;
        when(idempotencyService.acquire(event.eventId())).thenReturn(Mono.just(true));
        when(idempotencyService.markProcessed(event.eventId())).thenReturn(Mono.empty());

        // Act
        consumer.consume(event, channel, deliveryTag);

        // Assert
        verify(idempotencyService, timeout(1000)).acquire(event.eventId());
        verify(idempotencyService, timeout(1000)).markProcessed(event.eventId());
        verify(channel, timeout(1000)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should ignore duplicate events")
    void shouldIgnoreDuplicateEvents() throws IOException {
        // Arrange
        long deliveryTag = 2L;
        when(idempotencyService.acquire(event.eventId())).thenReturn(Mono.just(false));

        // Act
        consumer.consume(event, channel, deliveryTag);

        // Assert
        verify(idempotencyService, timeout(1000)).acquire(event.eventId());
        verify(idempotencyService, never()).markProcessed(any());
        verify(channel, timeout(1000)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should nack message when processing fails")
    void shouldNackMessageWhenProcessingFails() throws IOException {
        // Arrange
        long deliveryTag = 3L;
        RuntimeException testException = new RuntimeException("Processing error");
        when(idempotencyService.acquire(event.eventId())).thenReturn(Mono.just(true));
        when(idempotencyService.markProcessed(event.eventId())).thenReturn(Mono.error(testException));
        when(idempotencyService.release(event.eventId())).thenReturn(Mono.empty());

        // Act
        consumer.consume(event, channel, deliveryTag);

        // Assert
        verify(idempotencyService, timeout(1000)).acquire(event.eventId());
        verify(idempotencyService, timeout(1000)).markProcessed(event.eventId());
        verify(idempotencyService, timeout(1000)).release(event.eventId());
        verify(channel, timeout(1000)).basicNack(deliveryTag, false, false);
    }

    @Test
    @DisplayName("Should handle channel ack IOException gracefully")
    void shouldHandleAckIOException() throws IOException {
        // Arrange
        long deliveryTag = 4L;
        when(idempotencyService.acquire(event.eventId())).thenReturn(Mono.just(true));
        when(idempotencyService.markProcessed(event.eventId())).thenReturn(Mono.empty());
        doThrow(new IOException("Channel error")).when(channel).basicAck(deliveryTag, false);

        // Act
        consumer.consume(event, channel, deliveryTag);

        // Assert - should not throw exception
        verify(idempotencyService, timeout(1000)).acquire(event.eventId());
    }

    @Test
    @DisplayName("Should handle channel nack IOException gracefully")
    void shouldHandleNackIOException() throws IOException {
        // Arrange
        long deliveryTag = 5L;
        RuntimeException testException = new RuntimeException("Test error");
        when(idempotencyService.acquire(event.eventId())).thenReturn(Mono.just(true));
        when(idempotencyService.markProcessed(event.eventId())).thenReturn(Mono.error(testException));
        when(idempotencyService.release(event.eventId())).thenReturn(Mono.empty());
        doThrow(new IOException("Nack error")).when(channel).basicNack(deliveryTag, false, false);

        // Act
        consumer.consume(event, channel, deliveryTag);

        // Assert - should not throw exception
        verify(idempotencyService, timeout(1000)).acquire(event.eventId());
    }

    @Test
    @DisplayName("Should release lock on processing error")
    void shouldReleaseLockOnProcessingError() throws IOException {
        // Arrange
        long deliveryTag = 6L;
        RuntimeException testException = new RuntimeException("Send failed");
        when(idempotencyService.acquire(event.eventId())).thenReturn(Mono.just(true));
        when(idempotencyService.markProcessed(event.eventId())).thenReturn(Mono.error(testException));
        when(idempotencyService.release(event.eventId())).thenReturn(Mono.empty());

        // Act
        consumer.consume(event, channel, deliveryTag);

        // Assert
        verify(idempotencyService, timeout(1000)).release(event.eventId());
        verify(channel, timeout(1000)).basicNack(deliveryTag, false, false);
    }

    private NotificationEvent createTestEvent() {
        return new NotificationEvent(
                "event-123",
                "aggregate-456",
                NotificationEventType.TRANSFER_COMPLETED.name(),
                "user@example.com",
                new BigDecimal("100.00"),
                Instant.now()
        );
    }
}

