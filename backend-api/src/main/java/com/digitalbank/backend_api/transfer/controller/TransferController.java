package com.digitalbank.backend_api.transfer.controller;

import com.digitalbank.backend_api.transfer.dto.TransferRequest;
import com.digitalbank.backend_api.transfer.dto.TransferResponse;
import com.digitalbank.backend_api.transfer.service.TransferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<@NonNull TransferResponse> transfer(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @RequestBody @Valid TransferRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferService.transfer(idempotencyKey, request));
    }
}
