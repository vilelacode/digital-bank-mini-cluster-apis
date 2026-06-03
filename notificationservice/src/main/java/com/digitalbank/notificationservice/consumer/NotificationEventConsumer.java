package com.digitalbank.notificationservice.consumer;

import com.digitalbank.notificationservice.domain.event.NotificationEvent;
import com.digitalbank.notificationservice.idempotency.IdempotencyService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final IdempotencyService idempotencyService;

    @RabbitListener(
            queues = "${app.rabbitmq.queue}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void consume(
            NotificationEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        process(event)
                .doOnSuccess(ignored -> ack(channel, deliveryTag))
                .doOnError(error -> {
                    log.error("Failed to process notification eventId={}", event.eventId(), error);
                    nack(channel, deliveryTag);
                })
                .subscribe();
    }

    private Mono<Void> process(NotificationEvent event) {
        return idempotencyService.acquire(event.eventId())
                .flatMap(acquired -> {
                    if (!acquired) {
                        log.info("Duplicated notification ignored eventId={}", event.eventId());
                        return Mono.empty();
                    }

                    return sendNotification(event)
                            .then(idempotencyService.markProcessed(event.eventId()))
                            .onErrorResume(error ->
                                    idempotencyService.release(event.eventId())
                                            .then(Mono.error(error))
                            );
                });
    }

    private Mono<Void> sendNotification(NotificationEvent event) {
        return Mono.fromRunnable(() ->
                log.info(
                        "Notification sent type={} recipient={} aggregateId={} amount={}",
                        event.type(),
                        event.recipient(),
                        event.aggregateId(),
                        event.amount()
                )
        );
    }

    private void ack(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException exception) {
            log.error("Failed to ack message deliveryTag={}", deliveryTag, exception);
        }
    }

    private void nack(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException exception) {
            log.error("Failed to nack message deliveryTag={}", deliveryTag, exception);
        }
    }
}
