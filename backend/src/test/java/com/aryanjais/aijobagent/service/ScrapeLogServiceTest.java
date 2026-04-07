package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.response.ScrapeLogResponse;
import com.aryanjais.aijobagent.entity.ScrapeLog;
import com.aryanjais.aijobagent.entity.enums.ScrapePlatform;
import com.aryanjais.aijobagent.entity.enums.ScrapeStatus;
import com.aryanjais.aijobagent.repository.ScrapeLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrapeLogServiceTest {

    @Mock
    private ScrapeLogRepository scrapeLogRepository;

    @InjectMocks
    private ScrapeLogService scrapeLogService;

    @Test
    void startScrapeLog_createsRunningEntry() {
        when(scrapeLogRepository.save(any(ScrapeLog.class)))
                .thenAnswer(inv -> {
                    ScrapeLog saved = inv.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        ScrapeLog result = scrapeLogService.startScrapeLog(ScrapePlatform.ALL);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(ScrapePlatform.ALL, result.getPlatform());
        assertEquals(ScrapeStatus.RUNNING, result.getStatus());
        assertEquals(0, result.getJobsFound());
        assertEquals(0, result.getJobsNew());
        assertNotNull(result.getStartedAt());
    }

    @Test
    void incrementCounters_newJob_incrementsNewCount() {
        ScrapeLog existing = ScrapeLog.builder()
                .id(1L).jobsFound(5).jobsNew(3).jobsUpdated(2).build();
        when(scrapeLogRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(scrapeLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scrapeLogService.incrementCounters(1L, true);

        assertEquals(6, existing.getJobsFound());
        assertEquals(4, existing.getJobsNew());
        assertEquals(2, existing.getJobsUpdated());
    }

    @Test
    void incrementCounters_duplicateJob_incrementsUpdatedCount() {
        ScrapeLog existing = ScrapeLog.builder()
                .id(1L).jobsFound(5).jobsNew(3).jobsUpdated(2).build();
        when(scrapeLogRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(scrapeLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scrapeLogService.incrementCounters(1L, false);

        assertEquals(6, existing.getJobsFound());
        assertEquals(3, existing.getJobsNew());
        assertEquals(3, existing.getJobsUpdated());
    }

    @Test
    void completeScrapeLog_setsCompletedStatus() {
        ScrapeLog existing = ScrapeLog.builder()
                .id(1L).status(ScrapeStatus.RUNNING).build();
        when(scrapeLogRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(scrapeLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scrapeLogService.completeScrapeLog(1L);

        assertEquals(ScrapeStatus.COMPLETED, existing.getStatus());
        assertNotNull(existing.getCompletedAt());
    }

    @Test
    void failScrapeLog_setsFailedStatusWithMessage() {
        ScrapeLog existing = ScrapeLog.builder()
                .id(1L).status(ScrapeStatus.RUNNING).build();
        when(scrapeLogRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(scrapeLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scrapeLogService.failScrapeLog(1L, "Connection timeout");

        assertEquals(ScrapeStatus.FAILED, existing.getStatus());
        assertEquals("Connection timeout", existing.getErrorMessage());
        assertNotNull(existing.getCompletedAt());
    }

    @Test
    void getScrapeLogsPage_returnsPageOfResponses() {
        ScrapeLog log1 = ScrapeLog.builder()
                .id(1L).platform(ScrapePlatform.ALL).status(ScrapeStatus.COMPLETED)
                .jobsFound(50).jobsNew(45).jobsUpdated(5)
                .startedAt(LocalDateTime.now().minusHours(1))
                .completedAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(scrapeLogRepository.findAllByOrderByStartedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(log1), pageable, 1));

        Page<ScrapeLogResponse> result = scrapeLogService.getScrapeLogsPage(pageable);

        assertEquals(1, result.getTotalElements());
        ScrapeLogResponse response = result.getContent().get(0);
        assertEquals("ALL", response.getPlatform());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(50, response.getJobsFound());
        assertEquals(45, response.getJobsNew());
    }
}
