package com.digitalbank.notificationservice.idempotency;

import reactor.core.publisher.Mono;

public interface IdempotencyService {

    Mono<Boolean> acquire(String eventId);

    Mono<Void> markProcessed(String eventId);

    Mono<Void> release(String eventId);
}
