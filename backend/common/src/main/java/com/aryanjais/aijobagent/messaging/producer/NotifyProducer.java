package com.aryanjais.aijobagent.messaging.producer;

import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.aryanjais.aijobagent.messaging.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotifyProducer {

    private static final Logger log = LoggerFactory.getLogger(NotifyProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public void publishNotification(Long userId, String type, String subject, String content) {
        NotificationMessage message = NotificationMessage.builder()
                .userId(userId)
                .type(type)
                .subject(subject)
                .content(content)
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_NOTIFICATIONS,
                message);
        log.info("Published notification for user {}: {}", userId, type);
    }
}
