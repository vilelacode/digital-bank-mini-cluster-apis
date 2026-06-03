package com.digitalbank.backend_api.notification.publisher;

import com.digitalbank.backend_api.notification.dto.RabbitProperties;
import com.digitalbank.backend_api.notification.dto.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher  {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties properties;

    public void publishTransferCompleted(TransferCompletedEvent event) {
        log.info("Publishing TransferCompletedEvent eventId={} transferId={}", event.eventId(), event.transferId());
        try {
            rabbitTemplate.convertAndSend(
                    properties.exchange(),
                    properties.routingKey(),
                    event
            );
            log.debug("Published TransferCompletedEvent eventId={}", event.eventId());
        } catch (AmqpException ex) {
            log.error("Failed to publish TransferCompletedEvent eventId={} transferId={} error={}",
                    event.eventId(), event.transferId(), ex.getMessage(), ex);
        }
    }
}
