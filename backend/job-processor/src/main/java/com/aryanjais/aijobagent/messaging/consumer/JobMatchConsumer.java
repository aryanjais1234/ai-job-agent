package com.aryanjais.aijobagent.messaging.consumer;

import com.aryanjais.aijobagent.config.MatchingConfig;
import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.aryanjais.aijobagent.entity.JobMatch;
import com.aryanjais.aijobagent.messaging.dto.JobMatchMessage;
import com.aryanjais.aijobagent.messaging.producer.JobTailorProducer;
import com.aryanjais.aijobagent.service.MatchScoreService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for the jobs.match queue (T-3.6).
 * Computes match scores for all users against the given job.
 * Auto-triggers tailoring for matches above the threshold (T-4.1).
 */
@Component
@RequiredArgsConstructor
public class JobMatchConsumer {

    private static final Logger log = LoggerFactory.getLogger(JobMatchConsumer.class);

    private final MatchScoreService matchScoreService;
    private final MatchingConfig matchingConfig;
    private final JobTailorProducer jobTailorProducer;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_JOBS_MATCH)
    public void consumeMatchJob(JobMatchMessage message, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("Received job {} for match scoring", message.getJobId());

            var matches = matchScoreService.computeMatchesForJob(message.getJobId());
            log.info("Computed {} matches for job {}", matches.size(), message.getJobId());

            // Auto-trigger tailoring for high-scoring matches
            for (JobMatch match : matches) {
                if (match.getOverallScore().doubleValue() >= matchingConfig.getAutoTailorThreshold()) {
                    jobTailorProducer.publishForTailoring(
                            match.getUser().getId(), match.getJob().getId());
                    log.info("Auto-tailoring triggered: user={}, job={}, score={}",
                            match.getUser().getId(), match.getJob().getId(), match.getOverallScore());
                }
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error computing matches for job {}: {}", message.getJobId(), e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackEx) {
                log.error("Failed to nack message: {}", nackEx.getMessage());
            }
        }
    }
}
