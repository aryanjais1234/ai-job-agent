package com.aryanjais.aijobagent.messaging.producer;

import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.aryanjais.aijobagent.messaging.dto.JobAnalyzeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes job IDs to the jobs.analyze queue for AI analysis.
 */
@Component
@RequiredArgsConstructor
public class JobAnalyzeProducer {

    private static final Logger log = LoggerFactory.getLogger(JobAnalyzeProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public void publishForAnalysis(Long jobId) {
        JobAnalyzeMessage message = JobAnalyzeMessage.builder().jobId(jobId).build();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_JOBS_ANALYZE,
                message);
        log.info("Published job {} for AI analysis", jobId);
    }
}
