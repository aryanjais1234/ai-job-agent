package com.aryanjais.aijobagent.messaging.consumer;

import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.messaging.dto.JobRawMessage;
import com.aryanjais.aijobagent.messaging.producer.JobAnalyzeProducer;
import com.aryanjais.aijobagent.service.JobPersistenceService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobRawConsumerTest {

    @Mock
    private JobPersistenceService jobPersistenceService;

    @Mock
    private JobAnalyzeProducer jobAnalyzeProducer;

    @Mock
    private Channel channel;

    @InjectMocks
    private JobRawConsumer jobRawConsumer;

    @Test
    void consumeRawJob_newJob_acksAndPublishesForAnalysis() throws Exception {
        JobRawMessage message = buildMessage();
        Job persistedJob = Job.builder().id(42L).build();
        when(jobPersistenceService.persistJob(message)).thenReturn(persistedJob);

        jobRawConsumer.consumeRawJob(message, channel, 1L);

        verify(jobPersistenceService).persistJob(message);
        verify(jobAnalyzeProducer).publishForAnalysis(42L);
        verify(channel).basicAck(1L, false);
    }

    @Test
    void consumeRawJob_duplicateJob_acksWithoutPublishing() throws Exception {
        JobRawMessage message = buildMessage();
        when(jobPersistenceService.persistJob(message)).thenReturn(null);

        jobRawConsumer.consumeRawJob(message, channel, 2L);

        verify(jobPersistenceService).persistJob(message);
        verify(jobAnalyzeProducer, never()).publishForAnalysis(any());
        verify(channel).basicAck(2L, false);
    }

    @Test
    void consumeRawJob_illegalArgument_rejectsMessage() throws Exception {
        JobRawMessage message = buildMessage();
        when(jobPersistenceService.persistJob(message))
                .thenThrow(new IllegalArgumentException("Invalid platform"));

        jobRawConsumer.consumeRawJob(message, channel, 3L);

        verify(channel).basicReject(3L, false);
    }

    @Test
    void consumeRawJob_unexpectedError_nacksForRequeue() throws Exception {
        JobRawMessage message = buildMessage();
        when(jobPersistenceService.persistJob(message))
                .thenThrow(new RuntimeException("Database connection error"));

        jobRawConsumer.consumeRawJob(message, channel, 4L);

        verify(channel).basicNack(4L, false, true);
    }

    private JobRawMessage buildMessage() {
        return JobRawMessage.builder()
                .title("Test Developer")
                .company("Test Corp")
                .location("Remote")
                .sourcePlatform("INDEED")
                .sourceUrl("https://indeed.com/job/test-123")
                .scrapedAt("2024-01-15T06:00:00")
                .build();
    }
}
