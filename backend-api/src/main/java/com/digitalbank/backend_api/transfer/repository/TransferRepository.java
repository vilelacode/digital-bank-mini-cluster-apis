package com.digitalbank.backend_api.transfer.repository;

import com.digitalbank.backend_api.transfer.model.TransferEntity;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<@NonNull TransferEntity, @NonNull UUID> {

    Optional<TransferEntity> findByIdempotencyKey(String idempotencyKey);
}
