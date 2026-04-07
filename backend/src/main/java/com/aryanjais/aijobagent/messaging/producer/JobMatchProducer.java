package com.aryanjais.aijobagent.messaging.producer;

import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.aryanjais.aijobagent.messaging.dto.JobMatchMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes job IDs to the jobs.match queue for match scoring.
 */
@Component
@RequiredArgsConstructor
public class JobMatchProducer {

    private static final Logger log = LoggerFactory.getLogger(JobMatchProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public void publishForMatching(Long jobId) {
        JobMatchMessage message = JobMatchMessage.builder().jobId(jobId).build();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_JOBS_MATCH,
                message);
        log.info("Published job {} for match scoring", jobId);
    }
}
