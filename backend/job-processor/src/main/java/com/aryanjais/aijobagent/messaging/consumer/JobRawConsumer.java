package com.aryanjais.aijobagent.messaging.consumer;

import com.aryanjais.aijobagent.messaging.dto.JobRawMessage;
import com.aryanjais.aijobagent.messaging.producer.JobAnalyzeProducer;
import com.aryanjais.aijobagent.service.JobPersistenceService;
import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for the jobs.raw queue (T-2.6).
 * Consumes scraped job messages, deduplicates, and persists to the database.
 * Uses manual acknowledgment for reliability.
 */
@Component
@RequiredArgsConstructor
public class JobRawConsumer {

    private static final Logger log = LoggerFactory.getLogger(JobRawConsumer.class);

    private final JobPersistenceService jobPersistenceService;
    private final JobAnalyzeProducer jobAnalyzeProducer;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_JOBS_RAW)
    public void consumeRawJob(JobRawMessage message, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.debug("Received raw job message: {} @ {}", message.getTitle(), message.getCompany());

            var persistedJob = jobPersistenceService.persistJob(message);

            if (persistedJob != null) {
                log.info("New job persisted: {} @ {} [{}]",
                        message.getTitle(), message.getCompany(), message.getSourcePlatform());
                // Publish for AI analysis (Phase 3 pipeline)
                jobAnalyzeProducer.publishForAnalysis(persistedJob.getId());
            } else {
                log.debug("Duplicate job skipped: {} @ {} [{}]",
                        message.getTitle(), message.getCompany(), message.getSourcePlatform());
            }

            channel.basicAck(deliveryTag, false);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid job message rejected: {}", e.getMessage());
            try {
                channel.basicReject(deliveryTag, false);
            } catch (Exception rejectEx) {
                log.error("Failed to reject message: {}", rejectEx.getMessage());
            }
        } catch (Exception e) {
            log.error("Error processing raw job message: {}", e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception nackEx) {
                log.error("Failed to nack message: {}", nackEx.getMessage());
            }
        }
    }
}
