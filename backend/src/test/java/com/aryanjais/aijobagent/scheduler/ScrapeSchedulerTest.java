package com.aryanjais.aijobagent.scheduler;

import com.aryanjais.aijobagent.entity.ScrapeLog;
import com.aryanjais.aijobagent.entity.enums.ScrapePlatform;
import com.aryanjais.aijobagent.entity.enums.ScrapeStatus;
import com.aryanjais.aijobagent.service.ScraperClient;
import com.aryanjais.aijobagent.service.ScrapeLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrapeSchedulerTest {

    @Mock
    private ScraperClient scraperClient;

    @Mock
    private ScrapeLogService scrapeLogService;

    @InjectMocks
    private ScrapeScheduler scrapeScheduler;

    @Test
    void triggerDailyScrape_success_completesLog() {
        ScrapeLog scrapeLog = ScrapeLog.builder().id(1L).build();
        when(scrapeLogService.startScrapeLog(ScrapePlatform.ALL)).thenReturn(scrapeLog);

        scrapeScheduler.triggerDailyScrape();

        verify(scrapeLogService).startScrapeLog(ScrapePlatform.ALL);
        verify(scraperClient).triggerScrapeAll(any());
        verify(scrapeLogService).completeScrapeLog(1L);
    }

    @Test
    void triggerDailyScrape_failure_failsLog() {
        ScrapeLog scrapeLog = ScrapeLog.builder().id(2L).build();
        when(scrapeLogService.startScrapeLog(ScrapePlatform.ALL)).thenReturn(scrapeLog);
        doThrow(new RuntimeException("Connection refused"))
                .when(scraperClient).triggerScrapeAll(any());

        scrapeScheduler.triggerDailyScrape();

        verify(scrapeLogService).startScrapeLog(ScrapePlatform.ALL);
        verify(scrapeLogService).failScrapeLog(eq(2L), eq("Connection refused"));
    }
}
