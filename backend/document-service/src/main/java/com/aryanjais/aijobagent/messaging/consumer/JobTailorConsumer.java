package com.aryanjais.aijobagent.messaging.consumer;

import com.aryanjais.aijobagent.config.RabbitMQConfig;
import com.aryanjais.aijobagent.entity.CoverLetter;
import com.aryanjais.aijobagent.entity.TailoredResume;
import com.aryanjais.aijobagent.messaging.dto.JobTailorMessage;
import com.aryanjais.aijobagent.messaging.producer.NotifyProducer;
import com.aryanjais.aijobagent.service.CoverLetterService;
import com.aryanjais.aijobagent.service.PdfGeneratorService;
import com.aryanjais.aijobagent.service.ResumeTailoringService;
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
public class JobTailorConsumer {

    private static final Logger log = LoggerFactory.getLogger(JobTailorConsumer.class);

    private final ResumeTailoringService resumeTailoringService;
    private final CoverLetterService coverLetterService;
    private final PdfGeneratorService pdfGeneratorService;
    private final NotifyProducer notifyProducer;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_JOBS_TAILOR)
    public void consumeTailorJob(JobTailorMessage message, Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("Received tailor request: user={}, job={}", message.getUserId(), message.getJobId());

            // Step 1: Tailor the resume
            TailoredResume tailored = resumeTailoringService.tailorResume(
                    message.getUserId(), message.getJobId());

            // Step 2: Generate cover letter
            CoverLetter coverLetter = coverLetterService.generateCoverLetter(
                    message.getUserId(), message.getJobId());

            // Step 3: Generate PDFs
            pdfGeneratorService.generateResumePdf(tailored);
            pdfGeneratorService.generateCoverLetterPdf(coverLetter);

            // Step 4: Send notification
            notifyProducer.publishNotification(
                    message.getUserId(),
                    "MATCH_ALERT",
                    "Your tailored resume is ready!",
                    String.format("Your resume has been tailored for %s at %s. ATS Score: %s",
                            tailored.getJob().getTitle(),
                            tailored.getJob().getCompany(),
                            tailored.getAtsScore()));

            channel.basicAck(deliveryTag, false);
            log.info("Tailoring completed: user={}, job={}", message.getUserId(), message.getJobId());

        } catch (Exception e) {
            log.error("Error tailoring for user {} job {}: {}",
                    message.getUserId(), message.getJobId(), e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackEx) {
                log.error("Failed to nack message: {}", nackEx.getMessage());
            }
        }
    }
}
