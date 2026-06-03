package com.digitalbank.backend_api.transfer.dto;

import com.digitalbank.backend_api.transfer.enums.TransferStatus;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        UUID transferId,
        String idempotencyKey,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        TransferStatus status,
        LocalDateTime createdAt
) {
}
