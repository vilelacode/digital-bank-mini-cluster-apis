package com.digitalbank.backend_api.movement.repository;

import com.digitalbank.backend_api.movement.model.MovementEntity;
import lombok.NonNull;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementRepository extends JpaRepository< @NonNull MovementEntity, @NonNull UUID> {

    List<MovementEntity> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
