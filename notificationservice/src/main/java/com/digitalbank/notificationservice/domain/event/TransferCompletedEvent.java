package com.digitalbank.notificationservice.domain.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompletedEvent(
        @NotNull UUID eventId,
        @NotNull NotificationEventType eventType,
        @NotNull UUID transferId,
        @Valid @NotNull AccountSummary fromAccount,
        @Valid @NotNull AccountSummary toAccount,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String currency,
        @NotNull Instant occurredAt
) {
}
