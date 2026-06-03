package com.digitalbank.notificationservice.idempotency;

import com.digitalbank.notificationservice.config.IdempotencyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisIdempotencyService implements IdempotencyService {

    private static final String PROCESSING = "PROCESSING";
    private static final String PROCESSED = "PROCESSED";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final IdempotencyProperties properties;

    @Override
    public Mono<Boolean> acquire(String eventId) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(key(eventId), PROCESSING, properties.ttl())
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> markProcessed(String eventId) {
        return redisTemplate
                .opsForValue()
                .set(key(eventId), PROCESSED, properties.ttl())
                .then();
    }

    @Override
    public Mono<Void> release(String eventId) {
        return redisTemplate
                .delete(key(eventId))
                .then();
    }

    private String key(String eventId) {
        return properties.keyPrefix() + eventId;
    }
}
