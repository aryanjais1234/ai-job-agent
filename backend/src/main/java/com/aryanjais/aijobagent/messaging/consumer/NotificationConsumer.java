package com.aryanjais.aijobagent.messaging.consumer;

import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.aryanjais.aijobagent.entity.enums.NotificationType;
import com.aryanjais.aijobagent.messaging.dto.NotificationMessage;
import com.aryanjais.aijobagent.service.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATIONS)
    public void consumeNotification(NotificationMessage message, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("Received notification for user {}: {}", message.getUserId(), message.getType());

            NotificationType type;
            try {
                type = NotificationType.valueOf(message.getType());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown notification type '{}', falling back to MATCH_ALERT", message.getType());
                type = NotificationType.MATCH_ALERT;
            }

            notificationService.sendNotification(
                    message.getUserId(),
                    type,
                    message.getSubject(),
                    message.getContent());

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error processing notification for user {}: {}",
                    message.getUserId(), e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackEx) {
                log.error("Failed to nack message: {}", nackEx.getMessage());
            }
        }
    }
}
