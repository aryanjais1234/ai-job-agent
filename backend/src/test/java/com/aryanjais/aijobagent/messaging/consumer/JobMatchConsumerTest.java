package com.aryanjais.aijobagent.messaging.consumer;

import com.aryanjais.aijobagent.messaging.dto.JobMatchMessage;
import com.aryanjais.aijobagent.service.MatchScoreService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobMatchConsumerTest {

    @Mock private MatchScoreService matchScoreService;
    @Mock private Channel channel;

    @InjectMocks
    private JobMatchConsumer jobMatchConsumer;

    @Test
    void consumeMatchJob_success_acksMessage() throws Exception {
        JobMatchMessage message = JobMatchMessage.builder().jobId(1L).build();
        when(matchScoreService.computeMatchesForJob(1L)).thenReturn(List.of());

        jobMatchConsumer.consumeMatchJob(message, channel, 1L);

        verify(matchScoreService).computeMatchesForJob(1L);
        verify(channel).basicAck(1L, false);
    }

    @Test
    void consumeMatchJob_failure_nacksMessage() throws Exception {
        JobMatchMessage message = JobMatchMessage.builder().jobId(1L).build();
        when(matchScoreService.computeMatchesForJob(1L)).thenThrow(new RuntimeException("Match error"));

        jobMatchConsumer.consumeMatchJob(message, channel, 1L);

        verify(channel).basicNack(1L, false, false);
    }
}
