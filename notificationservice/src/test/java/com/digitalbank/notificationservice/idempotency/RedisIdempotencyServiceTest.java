package com.digitalbank.notificationservice.idempotency;

import com.digitalbank.notificationservice.config.IdempotencyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RedisIdempotencyService Tests")
class RedisIdempotencyServiceTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    private IdempotencyProperties properties;
    private RedisIdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        properties = new IdempotencyProperties("notification:idempotency:", Duration.ofHours(24));
        idempotencyService = new RedisIdempotencyService(redisTemplate, properties);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should acquire lock for new event")
    void shouldAcquireLockForNewEvent() {
        // Arrange
        String eventId = "event-123";
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(idempotencyService.acquire(eventId))
                .expectNext(true)
                .verifyComplete();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(valueOperations).setIfAbsent(keyCaptor.capture(), valueCaptor.capture(), ttlCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo("notification:idempotency:event-123");
        assertThat(valueCaptor.getValue()).isEqualTo("PROCESSING");
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    @DisplayName("Should return false when trying to acquire duplicate event")
    void shouldReturnFalseForDuplicateEvent() {
        // Arrange
        String eventId = "event-456";
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(idempotencyService.acquire(eventId))
                .expectNext(false)
                .verifyComplete();

        verify(valueOperations).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Should mark event as processed")
    void shouldMarkEventAsProcessed() {
        // Arrange
        String eventId = "event-789";
        when(valueOperations.set(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(idempotencyService.markProcessed(eventId))
                .verifyComplete();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), ttlCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo("notification:idempotency:event-789");
        assertThat(valueCaptor.getValue()).isEqualTo("PROCESSED");
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    @DisplayName("Should release lock by deleting key")
    void shouldReleaseLockByDeletingKey() {
        // Arrange
        String eventId = "event-999";
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));

        // Act & Assert
        StepVerifier.create(idempotencyService.release(eventId))
                .verifyComplete();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).delete(keyCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo("notification:idempotency:event-999");
    }

    @Test
    @DisplayName("Should use correct key prefix from properties")
    void shouldUseCorrectKeyPrefix() {
        // Arrange
        IdempotencyProperties customProperties = new IdempotencyProperties(
                "custom:prefix:",
                Duration.ofHours(12)
        );
        RedisIdempotencyService customService = new RedisIdempotencyService(redisTemplate, customProperties);
        String eventId = "event-custom";

        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        // Act
        customService.acquire(eventId);

        // Assert
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).setIfAbsent(keyCaptor.capture(), anyString(), any(Duration.class));

        assertThat(keyCaptor.getValue()).isEqualTo("custom:prefix:event-custom");
    }

    @Test
    @DisplayName("Should handle Redis errors gracefully in acquire")
    void shouldHandleRedisErrorInAcquire() {
        // Arrange
        String eventId = "event-error";
        RuntimeException redisError = new RuntimeException("Redis connection failed");
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.error(redisError));

        // Act & Assert
        StepVerifier.create(idempotencyService.acquire(eventId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should handle Redis errors gracefully in markProcessed")
    void shouldHandleRedisErrorInMarkProcessed() {
        // Arrange
        String eventId = "event-error-processed";
        RuntimeException redisError = new RuntimeException("Redis connection failed");
        when(valueOperations.set(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.error(redisError));

        // Act & Assert
        StepVerifier.create(idempotencyService.markProcessed(eventId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should handle Redis errors gracefully in release")
    void shouldHandleRedisErrorInRelease() {
        // Arrange
        String eventId = "event-error-release";
        RuntimeException redisError = new RuntimeException("Redis connection failed");
        when(redisTemplate.delete(anyString()))
                .thenReturn(Mono.error(redisError));

        // Act & Assert
        StepVerifier.create(idempotencyService.release(eventId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should use correct TTL from properties")
    void shouldUseCorrectTtlFromProperties() {
        // Arrange
        Duration customTtl = Duration.ofMinutes(30);
        IdempotencyProperties customProperties = new IdempotencyProperties(
                "notification:idempotency:",
                customTtl
        );
        RedisIdempotencyService customService = new RedisIdempotencyService(redisTemplate, customProperties);
        String eventId = "event-ttl";

        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        // Act
        customService.acquire(eventId);

        // Assert
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).setIfAbsent(anyString(), anyString(), ttlCaptor.capture());

        assertThat(ttlCaptor.getValue()).isEqualTo(customTtl);
    }
}

