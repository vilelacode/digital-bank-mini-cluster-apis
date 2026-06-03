package com.digitalbank.backend_api.notification.dto;

import java.util.UUID;

public record AccountSummaryEvent(
        UUID accountId,
        String holderName,
        String email
) {
}
