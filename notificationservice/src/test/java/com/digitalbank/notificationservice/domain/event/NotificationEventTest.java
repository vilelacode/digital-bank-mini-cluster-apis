package com.digitalbank.notificationservice.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationEvent Tests")
class NotificationEventTest {

    @Test
    @DisplayName("Should create valid NotificationEvent")
    void shouldCreateValidNotificationEvent() {
        // Act
        NotificationEvent event = new NotificationEvent(
                "event-123",
                "aggregate-456",
                NotificationEventType.TRANSFER_COMPLETED.name(),
                "user@example.com",
                new BigDecimal("100.00"),
                Instant.now()
        );

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.eventId()).isEqualTo("event-123");
        assertThat(event.aggregateId()).isEqualTo("aggregate-456");
        assertThat(event.type()).isEqualTo("TRANSFER_COMPLETED");
        assertThat(event.recipient()).isEqualTo("user@example.com");
        assertThat(event.amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("Should NotificationEvent be a record with all components")
    void shouldNotificationEventBeRecord() {
        // Arrange
        Instant now = Instant.now();
        NotificationEvent event1 = new NotificationEvent(
                "event-123",
                "aggregate-456",
                "TRANSFER_COMPLETED",
                "user@example.com",
                new BigDecimal("100.00"),
                now
        );
        NotificationEvent event2 = new NotificationEvent(
                "event-123",
                "aggregate-456",
                "TRANSFER_COMPLETED",
                "user@example.com",
                new BigDecimal("100.00"),
                now
        );

        // Act & Assert - records should have equals and hashCode
        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    @DisplayName("Should distinguish different NotificationEvents")
    void shouldDistinguishDifferentNotificationEvents() {
        // Arrange
        Instant now = Instant.now();
        NotificationEvent event1 = new NotificationEvent(
                "event-123",
                "aggregate-456",
                "TRANSFER_COMPLETED",
                "user1@example.com",
                new BigDecimal("100.00"),
                now
        );
        NotificationEvent event2 = new NotificationEvent(
                "event-124",
                "aggregate-456",
                "TRANSFER_COMPLETED",
                "user2@example.com",
                new BigDecimal("200.00"),
                now
        );

        // Act & Assert
        assertThat(event1).isNotEqualTo(event2);
        assertThat(event1.hashCode()).isNotEqualTo(event2.hashCode());
    }

    @Test
    @DisplayName("Should handle large amounts")
    void shouldHandleLargeAmounts() {
        // Act
        NotificationEvent event = new NotificationEvent(
                "event-123",
                "aggregate-456",
                "TRANSFER_COMPLETED",
                "user@example.com",
                new BigDecimal("999999999.99"),
                Instant.now()
        );

        // Assert
        assertThat(event.amount()).isEqualTo(new BigDecimal("999999999.99"));
    }

    @Test
    @DisplayName("Should handle small amounts")
    void shouldHandleSmallAmounts() {
        // Act
        NotificationEvent event = new NotificationEvent(
                "event-123",
                "aggregate-456",
                "TRANSFER_COMPLETED",
                "user@example.com",
                new BigDecimal("0.01"),
                Instant.now()
        );

        // Assert
        assertThat(event.amount()).isEqualTo(new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("Should handle zero amount")
    void shouldHandleZeroAmount() {
        // Act
        NotificationEvent event = new NotificationEvent(
                "event-123",
                "aggregate-456",
                "TRANSFER_COMPLETED",
                "user@example.com",
                BigDecimal.ZERO,
                Instant.now()
        );

        // Assert
        assertThat(event.amount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should NotificationEvent have toString method")
    void shouldNotificationEventHaveToString() {
        // Act
        NotificationEvent event = new NotificationEvent(
                "event-123",
                "aggregate-456",
                "TRANSFER_COMPLETED",
                "user@example.com",
                new BigDecimal("100.00"),
                Instant.now()
        );

        String toString = event.toString();

        // Assert
        assertThat(toString).contains("event-123")
                .contains("aggregate-456")
                .contains("TRANSFER_COMPLETED")
                .contains("user@example.com");
    }
}

