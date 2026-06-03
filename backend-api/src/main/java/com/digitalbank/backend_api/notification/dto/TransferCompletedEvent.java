package com.digitalbank.backend_api.notification.dto;

import java.util.UUID;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferCompletedEvent(
        UUID eventId,
        String eventType,
        UUID transferId,
        AccountSummaryEvent fromAccount,
        AccountSummaryEvent toAccount,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
}
