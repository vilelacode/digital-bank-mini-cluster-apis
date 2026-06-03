package com.digitalbank.notificationservice.domain.event;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record NotificationEvent(
        @NotBlank String eventId,
        @NotBlank String aggregateId,
        @NotBlank String type,
        @Email String recipient,
        @NotNull BigDecimal amount,
        @NotNull Instant occurredAt
) {
}
