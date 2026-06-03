package com.digitalbank.notificationservice.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransferCompletedEvent Tests")
class TransferCompletedEventTest {

    @Test
    @DisplayName("Should create valid TransferCompletedEvent")
    void shouldCreateValidTransferCompletedEvent() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID transferId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        AccountSummary fromAccount = new AccountSummary(fromAccountId, "John Doe", "john@example.com");
        AccountSummary toAccount = new AccountSummary(toAccountId, "Jane Smith", "jane@example.com");

        // Act
        TransferCompletedEvent event = new TransferCompletedEvent(
                eventId,
                NotificationEventType.TRANSFER_COMPLETED,
                transferId,
                fromAccount,
                toAccount,
                new BigDecimal("100.00"),
                "USD",
                Instant.now()
        );

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.eventType()).isEqualTo(NotificationEventType.TRANSFER_COMPLETED);
        assertThat(event.transferId()).isEqualTo(transferId);
        assertThat(event.fromAccount()).isEqualTo(fromAccount);
        assertThat(event.toAccount()).isEqualTo(toAccount);
        assertThat(event.amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(event.currency()).isEqualTo("USD");
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("Should TransferCompletedEvent be a record with all components")
    void shouldTransferCompletedEventBeRecord() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID transferId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AccountSummary account = new AccountSummary(accountId, "John Doe", "john@example.com");
        Instant now = Instant.now();

        TransferCompletedEvent event1 = new TransferCompletedEvent(
                eventId,
                NotificationEventType.TRANSFER_COMPLETED,
                transferId,
                account,
                account,
                new BigDecimal("100.00"),
                "USD",
                now
        );

        TransferCompletedEvent event2 = new TransferCompletedEvent(
                eventId,
                NotificationEventType.TRANSFER_COMPLETED,
                transferId,
                account,
                account,
                new BigDecimal("100.00"),
                "USD",
                now
        );

        // Act & Assert - records should have equals and hashCode
        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    @DisplayName("Should distinguish different TransferCompletedEvents")
    void shouldDistinguishDifferentTransferCompletedEvents() {
        // Arrange
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        AccountSummary account1 = new AccountSummary(accountId1, "John Doe", "john@example.com");
        AccountSummary account2 = new AccountSummary(accountId2, "Jane Smith", "jane@example.com");
        Instant now = Instant.now();

        TransferCompletedEvent event1 = new TransferCompletedEvent(
                UUID.randomUUID(),
                NotificationEventType.TRANSFER_COMPLETED,
                UUID.randomUUID(),
                account1,
                account2,
                new BigDecimal("100.00"),
                "USD",
                now
        );

        TransferCompletedEvent event2 = new TransferCompletedEvent(
                UUID.randomUUID(),
                NotificationEventType.TRANSFER_COMPLETED,
                UUID.randomUUID(),
                account1,
                account2,
                new BigDecimal("200.00"),
                "EUR",
                now
        );

        // Act & Assert
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    @DisplayName("Should support different currencies")
    void shouldSupportDifferentCurrencies() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AccountSummary account = new AccountSummary(accountId, "John Doe", "john@example.com");

        // Act & Assert for USD
        TransferCompletedEvent usdEvent = new TransferCompletedEvent(
                eventId,
                NotificationEventType.TRANSFER_COMPLETED,
                UUID.randomUUID(),
                account,
                account,
                new BigDecimal("100.00"),
                "USD",
                Instant.now()
        );
        assertThat(usdEvent.currency()).isEqualTo("USD");

        // Act & Assert for EUR
        TransferCompletedEvent eurEvent = new TransferCompletedEvent(
                eventId,
                NotificationEventType.TRANSFER_COMPLETED,
                UUID.randomUUID(),
                account,
                account,
                new BigDecimal("100.00"),
                "EUR",
                Instant.now()
        );
        assertThat(eurEvent.currency()).isEqualTo("EUR");

        // Act & Assert for BRL
        TransferCompletedEvent brlEvent = new TransferCompletedEvent(
                eventId,
                NotificationEventType.TRANSFER_COMPLETED,
                UUID.randomUUID(),
                account,
                account,
                new BigDecimal("100.00"),
                "BRL",
                Instant.now()
        );
        assertThat(brlEvent.currency()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Should handle different amounts")
    void shouldHandleDifferentAmounts() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        AccountSummary account = new AccountSummary(accountId, "John Doe", "john@example.com");

        // Act & Assert for large amount
        TransferCompletedEvent largeEvent = new TransferCompletedEvent(
                UUID.randomUUID(),
                NotificationEventType.TRANSFER_COMPLETED,
                UUID.randomUUID(),
                account,
                account,
                new BigDecimal("999999999.99"),
                "USD",
                Instant.now()
        );
        assertThat(largeEvent.amount()).isEqualTo(new BigDecimal("999999999.99"));

        // Act & Assert for small amount
        TransferCompletedEvent smallEvent = new TransferCompletedEvent(
                UUID.randomUUID(),
                NotificationEventType.TRANSFER_COMPLETED,
                UUID.randomUUID(),
                account,
                account,
                new BigDecimal("0.01"),
                "USD",
                Instant.now()
        );
        assertThat(smallEvent.amount()).isEqualTo(new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("Should TransferCompletedEvent have toString method")
    void shouldTransferCompletedEventHaveToString() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AccountSummary account = new AccountSummary(accountId, "John Doe", "john@example.com");

        // Act
        TransferCompletedEvent event = new TransferCompletedEvent(
                eventId,
                NotificationEventType.TRANSFER_COMPLETED,
                UUID.randomUUID(),
                account,
                account,
                new BigDecimal("100.00"),
                "USD",
                Instant.now()
        );

        String toString = event.toString();

        // Assert
        assertThat(toString).contains(eventId.toString())
                .contains("TRANSFER_COMPLETED")
                .contains("USD")
                .contains("100.00");
    }
}

