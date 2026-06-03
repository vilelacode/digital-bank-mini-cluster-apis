package com.digitalbank.backend_api.transfer.service;

import com.digitalbank.backend_api.account.model.AccountEntity;
import com.digitalbank.backend_api.account.repository.AccountRepository;
import com.digitalbank.backend_api.movement.enums.MovementType;
import com.digitalbank.backend_api.movement.model.MovementEntity;
import com.digitalbank.backend_api.movement.repository.MovementRepository;
import com.digitalbank.backend_api.notification.dto.AccountSummaryEvent;
import com.digitalbank.backend_api.notification.dto.TransferCompletedEvent;
import com.digitalbank.backend_api.notification.publisher.NotificationEventPublisher;
import com.digitalbank.backend_api.transfer.dto.TransferRequest;
import com.digitalbank.backend_api.transfer.dto.TransferResponse;
import com.digitalbank.backend_api.transfer.enums.TransferStatus;
import com.digitalbank.backend_api.transfer.model.TransferEntity;
import com.digitalbank.backend_api.transfer.repository.TransferRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final MovementRepository movementRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Transactional
    public TransferResponse transfer(String idempotencyKey, TransferRequest request) {
        log.info("Processing transfer request idempotencyKey={} from={} to={} amount={}",
                idempotencyKey, request.fromAccountId(), request.toAccountId(), request.amount());

        return transferRepository.findByIdempotencyKey(idempotencyKey)
                .map(existingTransfer -> {
                    log.info("Transfer request rejected - idempotency key already processed idempotencyKey={} transferId={} status={}",
                            idempotencyKey, existingTransfer.getId(), existingTransfer.getStatus());
                    return existingTransfer.toResponse();
                })
                .orElseGet(() -> createTransfer(idempotencyKey, request));
    }


    @Transactional
    public TransferResponse createTransfer(String idempotencyKey, TransferRequest request) {
        log.debug("Creating transfer idempotencyKey={}", idempotencyKey);

        if (request.fromAccountId().equals(request.toAccountId())) {
            log.warn("Transfer failed - same origin and destination: {}", request.fromAccountId());
            throw new IllegalArgumentException("Origin and destination accounts must be different");
        }

        var accounts = lockAccountsInStableOrder(
                request.fromAccountId(),
                request.toAccountId()
        );

        AccountEntity from = accounts.get(request.fromAccountId());
        AccountEntity to = accounts.get(request.toAccountId());

        try {
            from.debit(request.amount());
            to.credit(request.amount());
        } catch (RuntimeException ex) {
            log.error("Transfer failed during debit/credit for idempotencyKey={} from={} to={} amount={}. Error={}",
                    idempotencyKey, request.fromAccountId(), request.toAccountId(), request.amount(), ex.getMessage(), ex);
            throw ex;
        }

         TransferEntity transfer = transferRepository.save(
                TransferEntity.builder()
                        .id(UUID.randomUUID())
                        .idempotencyKey(idempotencyKey)
                        .fromAccountId(from.getId())
                        .toAccountId(to.getId())
                        .amount(request.amount())
                        .status(TransferStatus.COMPLETED)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        movementRepository.save(MovementEntity.builder()
                .id(UUID.randomUUID())
                .accountId(from.getId())
                .transferId(transfer.getId())
                .type(MovementType.DEBIT)
                .amount(request.amount())
                .createdAt(LocalDateTime.now())
                .build());

        movementRepository.save(MovementEntity.builder()
                .id(UUID.randomUUID())
                .accountId(to.getId())
                .transferId(transfer.getId())
                .type(MovementType.CREDIT)
                .amount(request.amount())
                .createdAt(LocalDateTime.now())
                .build());

        publishNotificationAfterCommit(from, to, transfer);

        log.info("Transfer completed transferId={} idempotencyKey={} amount={} from={} to={}",
                transfer.getId(), idempotencyKey, request.amount(), from.getId(), to.getId());

        return transfer.toResponse();
    }

    private Map<UUID, AccountEntity> lockAccountsInStableOrder(UUID firstId, UUID secondId) {
        return Stream.of(firstId, secondId)
                .sorted()
                .map(id -> accountRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new EntityNotFoundException("Account not found: " + id)))
                .collect(Collectors.toMap(AccountEntity::getId, Function.identity()));
    }

    private void publishNotificationAfterCommit(
            AccountEntity from,
            AccountEntity to,
            TransferEntity transfer
    ) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationEventPublisher.publishTransferCompleted(
                        new TransferCompletedEvent(
                                UUID.randomUUID(),
                                "TRANSFER_COMPLETED",
                                transfer.getId(),
                                new AccountSummaryEvent(from.getId(), from.getHolderName(), from.getEmail()),
                                new AccountSummaryEvent(to.getId(), to.getHolderName(), to.getEmail()),
                                transfer.getAmount(),
                                "BRL",
                                Instant.now()
                        )
                );
            }
        });
    }
}
