package com.digitalbank.notificationservice.idempotency;

import com.digitalbank.notificationservice.TestcontainersConfiguration;
import com.digitalbank.notificationservice.config.IdempotencyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "app.idempotency.key-prefix=notification:idempotency:integration:",
        "app.idempotency.ttl=1h"
})
@DisplayName("RedisIdempotencyService Integration Tests")
class RedisIdempotencyServiceIntegrationTest {

    @Autowired
    private RedisIdempotencyService idempotencyService;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    private IdempotencyProperties properties;

    private String testEventId;

    @BeforeEach
    void setUp() {
        testEventId = "integration-test-event-" + System.currentTimeMillis();
        // Clean up before each test
        redisTemplate.delete(properties.keyPrefix() + testEventId).block();
    }

    @Test
    @DisplayName("Should acquire lock for first time")
    void shouldAcquireLockForFirstTime() {
        // Act & Assert
        StepVerifier.create(idempotencyService.acquire(testEventId))
                .expectNext(true)
                .verifyComplete();

        // Verify in Redis
        String key = properties.keyPrefix() + testEventId;
        StepVerifier.create(redisTemplate.opsForValue().get(key))
                .expectNext("PROCESSING")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should not acquire lock for duplicate event")
    void shouldNotAcquireLockForDuplicate() {
        // Arrange - first acquisition
        idempotencyService.acquire(testEventId).block();

        // Act - second acquisition
        StepVerifier.create(idempotencyService.acquire(testEventId))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should mark event as processed")
    void shouldMarkEventAsProcessed() {
        // Arrange
        idempotencyService.acquire(testEventId).block();

        // Act
        idempotencyService.markProcessed(testEventId).block();

        // Assert
        String key = properties.keyPrefix() + testEventId;
        StepVerifier.create(redisTemplate.opsForValue().get(key))
                .expectNext("PROCESSED")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should release lock by deleting key")
    void shouldReleaseLockByDeletingKey() {
        // Arrange
        idempotencyService.acquire(testEventId).block();
        String key = properties.keyPrefix() + testEventId;

        // Act
        idempotencyService.release(testEventId).block();

        // Assert
        StepVerifier.create(redisTemplate.hasKey(key))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should complete full lifecycle: acquire -> process -> mark")
    void shouldCompleteFullLifecycle() {
        // Act & Assert - Acquire
        StepVerifier.create(idempotencyService.acquire(testEventId))
                .expectNext(true)
                .verifyComplete();

        // Act & Assert - Mark Processed
        StepVerifier.create(idempotencyService.markProcessed(testEventId))
                .verifyComplete();

        // Verify final state
        String key = properties.keyPrefix() + testEventId;
        StepVerifier.create(redisTemplate.opsForValue().get(key))
                .expectNext("PROCESSED")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should release and reacquire for error recovery")
    void shouldReleaseAndReacquireForErrorRecovery() {
        // Act & Assert - Acquire
        StepVerifier.create(idempotencyService.acquire(testEventId))
                .expectNext(true)
                .verifyComplete();

        // Act & Assert - Release
        StepVerifier.create(idempotencyService.release(testEventId))
                .verifyComplete();

        // Act & Assert - Re-acquire
        StepVerifier.create(idempotencyService.acquire(testEventId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should use correct TTL from properties")
    void shouldUseTtlFromProperties() {
        // Arrange
        String key = properties.keyPrefix() + testEventId;

        // Act
        idempotencyService.acquire(testEventId).block();

        // Assert - verify TTL is set
        StepVerifier.create(redisTemplate.getExpire(key))
                .assertNext(ttl -> {
                    // TTL should be close to the configured TTL (1h = 3600 seconds)
                    assertThat(ttl).isGreaterThan(Duration.ofSeconds(3595)) // Allow 5 seconds variance
                            .isLessThanOrEqualTo(Duration.ofSeconds(3600));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle multiple concurrent events")
    void shouldHandleMultipleConcurrentEvents() {
        // Arrange
        String eventId1 = "event-1-" + System.currentTimeMillis();
        String eventId2 = "event-2-" + System.currentTimeMillis();
        String eventId3 = "event-3-" + System.currentTimeMillis();

        // Act & Assert - acquire all
        StepVerifier.create(idempotencyService.acquire(eventId1))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(idempotencyService.acquire(eventId2))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(idempotencyService.acquire(eventId3))
                .expectNext(true)
                .verifyComplete();

        // Act & Assert - mark all processed
        StepVerifier.create(idempotencyService.markProcessed(eventId1))
                .verifyComplete();

        StepVerifier.create(idempotencyService.markProcessed(eventId2))
                .verifyComplete();

        StepVerifier.create(idempotencyService.markProcessed(eventId3))
                .verifyComplete();

        // Act & Assert - verify all in processed state
        String key1 = properties.keyPrefix() + eventId1;
        String key2 = properties.keyPrefix() + eventId2;
        String key3 = properties.keyPrefix() + eventId3;

        StepVerifier.create(redisTemplate.opsForValue().get(key1))
                .expectNext("PROCESSED")
                .verifyComplete();

        StepVerifier.create(redisTemplate.opsForValue().get(key2))
                .expectNext("PROCESSED")
                .verifyComplete();

        StepVerifier.create(redisTemplate.opsForValue().get(key3))
                .expectNext("PROCESSED")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should clean up after release")
    void shouldCleanUpAfterRelease() {
        // Arrange
        String key = properties.keyPrefix() + testEventId;
        idempotencyService.acquire(testEventId).block();

        assertThat(redisTemplate.hasKey(key).block()).isTrue();

        // Act
        idempotencyService.release(testEventId).block();

        // Assert
        assertThat(redisTemplate.hasKey(key).block()).isFalse();
    }
}

