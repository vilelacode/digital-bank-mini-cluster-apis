package com.digitalbank.backend_api.account.repository;

import com.digitalbank.backend_api.account.model.AccountEntity;
import jakarta.persistence.LockModeType;
import java.util.UUID;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<@NonNull AccountEntity,@NonNull UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.id = :id")
    Optional<AccountEntity> findByIdForUpdate(UUID id);
}
