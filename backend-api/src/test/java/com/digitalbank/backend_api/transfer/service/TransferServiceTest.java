package com.digitalbank.backend_api.transfer.service;

import com.digitalbank.backend_api.account.model.AccountEntity;
import com.digitalbank.backend_api.account.repository.AccountRepository;
import com.digitalbank.backend_api.movement.repository.MovementRepository;
import com.digitalbank.backend_api.notification.publisher.NotificationEventPublisher;
import com.digitalbank.backend_api.transfer.dto.TransferRequest;
import com.digitalbank.backend_api.transfer.dto.TransferResponse;
import com.digitalbank.backend_api.transfer.enums.TransferStatus;
import com.digitalbank.backend_api.transfer.model.TransferEntity;
import com.digitalbank.backend_api.transfer.repository.TransferRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
//@SpringBootTest
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @Autowired
    private TransferService transferService;

    @Captor
    private ArgumentCaptor<TransferEntity> transferCaptor;

    @Captor
    private ArgumentCaptor<com.digitalbank.backend_api.movement.model.MovementEntity> movementCaptor;

    private UUID fromId;
    private UUID toId;
    private TransferRequest request;
    private AccountEntity fromAccount;
    private AccountEntity toAccount;

    @BeforeEach
    void setUp() {
        fromId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        toId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        request = new TransferRequest(fromId, toId, new BigDecimal("125.50"));
        fromAccount = account(fromId, "Alice", "alice@bank.com", new BigDecimal("1000.00"));
        toAccount = account(toId, "Bob", "bob@bank.com", new BigDecimal("200.00"));
    }

    @Test
    void shouldCreateTransfer() {
        TransferResponse response =
                transferService.createTransfer("idem-2", request);
    }

    @Test
    void transferShouldReturnExistingTransferWhenIdempotencyKeyAlreadyExists() {
        TransferEntity existing = TransferEntity.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .idempotencyKey("idem-1")
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(new BigDecimal("125.50"))
                .status(TransferStatus.COMPLETED)
                .createdAt(LocalDateTime.of(2026, 6, 3, 10, 0))
                .build();

        when(transferRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(existing));

        TransferResponse response = transferService.transfer("idem-1", request);

        assertThat(response.transferId()).isEqualTo(existing.getId());
        assertThat(response.idempotencyKey()).isEqualTo("idem-1");
        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);
        verify(transferRepository).findByIdempotencyKey("idem-1");
        verifyNoInteractions(accountRepository, movementRepository, notificationEventPublisher);
    }

    @Test
    void createTransferShouldDebitCreditPersistMovementsAndScheduleNotification() {
        when(accountRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any(TransferEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> invocation.getArgument(0)).when(movementRepository).save(any());

        TransferResponse response = transferService.createTransfer("idem-2", request);

        assertThat(response.idempotencyKey()).isEqualTo("idem-2");
        assertThat(response.fromAccountId()).isEqualTo(fromId);
        assertThat(response.toAccountId()).isEqualTo(toId);
        assertThat(response.amount()).isEqualByComparingTo("125.50");
        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);

        assertThat(fromAccount.getBalance()).isEqualByComparingTo("874.50");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("325.50");

        verify(accountRepository).findByIdForUpdate(fromId);
        verify(accountRepository).findByIdForUpdate(toId);
        verify(transferRepository).save(transferCaptor.capture());
        verify(movementRepository, org.mockito.Mockito.times(2)).save(movementCaptor.capture());

        TransferEntity persistedTransfer = transferCaptor.getValue();
        assertThat(persistedTransfer.getIdempotencyKey()).isEqualTo("idem-2");
        assertThat(persistedTransfer.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(persistedTransfer.getFromAccountId()).isEqualTo(fromId);
        assertThat(persistedTransfer.getToAccountId()).isEqualTo(toId);

        List<com.digitalbank.backend_api.movement.model.MovementEntity> movements = movementCaptor.getAllValues();
        assertThat(movements).hasSize(2);
        assertThat(movements).extracting(com.digitalbank.backend_api.movement.model.MovementEntity::getType)
                .containsExactlyInAnyOrder(
                        com.digitalbank.backend_api.movement.enums.MovementType.DEBIT,
                        com.digitalbank.backend_api.movement.enums.MovementType.CREDIT
                );
        assertThat(movements).extracting(com.digitalbank.backend_api.movement.model.MovementEntity::getAmount)
                .allMatch(amount -> amount.compareTo(new BigDecimal("125.50")) == 0);
    }

    @Test
    void createTransferShouldRejectSameAccount() {
        TransferRequest invalid = new TransferRequest(fromId, fromId, new BigDecimal("10.00"));

        assertThatThrownBy(() -> transferService.createTransfer("idem-3", invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Origin and destination accounts must be different");

        verifyNoInteractions(accountRepository, transferRepository, movementRepository, notificationEventPublisher);
    }

    @Test
    void createTransferShouldFailWhenAccountDoesNotExist() {
        when(accountRepository.findByIdForUpdate(fromId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.createTransfer("idem-4", request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Account not found: " + fromId);

        verify(accountRepository).findByIdForUpdate(fromId);
        verify(accountRepository, never()).findByIdForUpdate(toId);
        verifyNoInteractions(transferRepository, movementRepository, notificationEventPublisher);
    }

    @Test
    void publishNotificationAfterCommitShouldRegisterAfterCommitHook() throws Exception {
        when(accountRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any(TransferEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> invocation.getArgument(0)).when(movementRepository).save(any());

        TransferResponse response = transferService.createTransfer("idem-5", request);
        assertThat(response).isNotNull();

        List<TransactionSynchronization> synchronizations = currentSynchronizations();
        assertThat(synchronizations).isNotEmpty();

        TransactionSynchronization synchronization = synchronizations.get(synchronizations.size() - 1);
        synchronization.afterCommit();

        verify(notificationEventPublisher).publishTransferCompleted(org.mockito.ArgumentMatchers.argThat(event ->
                event != null
                        && event.transferId().equals(response.transferId())
                        && event.fromAccount().accountId().equals(fromId)
                        && event.toAccount().accountId().equals(toId)
                        && event.amount().compareTo(new BigDecimal("125.50")) == 0
                        && "TRANSFER_COMPLETED".equals(event.eventType())
                        && "BRL".equals(event.currency())
        ));
    }

    private static AccountEntity account(UUID id, String holderName, String email, BigDecimal balance) {
        return AccountEntity.builder()
                .id(id)
                .holderName(holderName)
                .email(email)
                .balance(balance)
                .active(true)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static List<TransactionSynchronization> currentSynchronizations() throws Exception {
        Field field = TransactionSynchronizationManager.class.getDeclaredField("synchronizations");
        field.setAccessible(true);
        ThreadLocal<?> threadLocal = (ThreadLocal<?>) field.get(null);
        Object value = threadLocal.get();
        if (value == null) {
            return List.of();
        }
        return (List<TransactionSynchronization>) value;
    }
}

