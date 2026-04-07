package com.aryanjais.aijobagent.messaging.consumer;

import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.aryanjais.aijobagent.messaging.dto.JobAnalyzeMessage;
import com.aryanjais.aijobagent.messaging.producer.JobMatchProducer;
import com.aryanjais.aijobagent.service.JobAnalysisService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for the jobs.analyze queue (T-3.2).
 * Performs AI-powered JD analysis and then publishes for match scoring.
 */
@Component
@RequiredArgsConstructor
public class JobAnalyzeConsumer {

    private static final Logger log = LoggerFactory.getLogger(JobAnalyzeConsumer.class);

    private final JobAnalysisService jobAnalysisService;
    private final JobMatchProducer jobMatchProducer;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_JOBS_ANALYZE)
    public void consumeAnalyzeJob(JobAnalyzeMessage message, Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("Received job {} for analysis", message.getJobId());

            jobAnalysisService.analyzeJob(message.getJobId());

            // After analysis, publish for match scoring
            jobMatchProducer.publishForMatching(message.getJobId());

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error analysing job {}: {}", message.getJobId(), e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackEx) {
                log.error("Failed to nack message: {}", nackEx.getMessage());
            }
        }
    }
}
