package com.digitalbank.backend_api.transfer.model;

import com.digitalbank.backend_api.transfer.dto.TransferResponse;
import com.digitalbank.backend_api.transfer.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "from_account_id", nullable = false, length = 36)
    private UUID fromAccountId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "to_account_id", nullable = false, length = 36)
    private UUID toAccountId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public TransferResponse toResponse() {
        return new TransferResponse(
                this.id,
                this.idempotencyKey,
                this.fromAccountId,
                this.toAccountId,
                this.amount,
                this.status,
                this.createdAt
        );
    }
}
