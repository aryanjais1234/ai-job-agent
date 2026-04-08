package com.aryanjais.aijobagent.messaging.consumer;

import com.aryanjais.aijobagent.entity.JobAnalysis;
import com.aryanjais.aijobagent.messaging.dto.JobAnalyzeMessage;
import com.aryanjais.aijobagent.messaging.producer.JobMatchProducer;
import com.aryanjais.aijobagent.service.JobAnalysisService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobAnalyzeConsumerTest {

    @Mock private JobAnalysisService jobAnalysisService;
    @Mock private JobMatchProducer jobMatchProducer;
    @Mock private Channel channel;

    @InjectMocks
    private JobAnalyzeConsumer jobAnalyzeConsumer;

    @Test
    void consumeAnalyzeJob_success_publishesForMatching() throws Exception {
        JobAnalyzeMessage message = JobAnalyzeMessage.builder().jobId(1L).build();
        when(jobAnalysisService.analyzeJob(1L)).thenReturn(JobAnalysis.builder().id(100L).build());

        jobAnalyzeConsumer.consumeAnalyzeJob(message, channel, 1L);

        verify(jobAnalysisService).analyzeJob(1L);
        verify(jobMatchProducer).publishForMatching(1L);
        verify(channel).basicAck(1L, false);
    }

    @Test
    void consumeAnalyzeJob_failure_nacksMessage() throws Exception {
        JobAnalyzeMessage message = JobAnalyzeMessage.builder().jobId(1L).build();
        when(jobAnalysisService.analyzeJob(1L)).thenThrow(new RuntimeException("AI error"));

        jobAnalyzeConsumer.consumeAnalyzeJob(message, channel, 1L);

        verify(channel).basicNack(1L, false, false);
    }
}
