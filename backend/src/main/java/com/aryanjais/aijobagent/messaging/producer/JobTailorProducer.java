package com.aryanjais.aijobagent.messaging.producer;

import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.aryanjais.aijobagent.messaging.dto.JobTailorMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobTailorProducer {

    private static final Logger log = LoggerFactory.getLogger(JobTailorProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public void publishForTailoring(Long userId, Long jobId) {
        JobTailorMessage message = JobTailorMessage.builder()
                .userId(userId)
                .jobId(jobId)
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_JOBS_TAILOR,
                message);
        log.info("Published job {} for user {} tailoring", jobId, userId);
    }
}
