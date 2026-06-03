package com.digitalbank.notificationservice.domain.event;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AccountSummary(
        @NotNull UUID accountId,
        @NotBlank String holderName,
        @Email String email
) {
}
