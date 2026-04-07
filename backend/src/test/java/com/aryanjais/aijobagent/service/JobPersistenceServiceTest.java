package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.entity.enums.JobType;
import com.aryanjais.aijobagent.entity.enums.SourcePlatform;
import com.aryanjais.aijobagent.messaging.dto.JobRawMessage;
import com.aryanjais.aijobagent.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPersistenceServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobPersistenceService jobPersistenceService;

    @Test
    void persistJob_newJob_savesAndReturnsJob() {
        JobRawMessage message = buildMessage("INDEED", "https://indeed.com/job/123");
        when(jobRepository.findBySourcePlatformAndSourceUrl(SourcePlatform.INDEED, "https://indeed.com/job/123"))
                .thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(1L);
            return j;
        });

        Job result = jobPersistenceService.persistJob(message);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void persistJob_duplicateJob_returnsNull() {
        JobRawMessage message = buildMessage("INDEED", "https://indeed.com/job/123");
        when(jobRepository.findBySourcePlatformAndSourceUrl(SourcePlatform.INDEED, "https://indeed.com/job/123"))
                .thenReturn(Optional.of(new Job()));

        Job result = jobPersistenceService.persistJob(message);

        assertNull(result);
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void persistJob_savesCorrectFields() {
        JobRawMessage message = JobRawMessage.builder()
                .title("Senior Java Developer")
                .company("Infosys")
                .location("Bangalore")
                .jobType("FULL_TIME")
                .salaryMin(new BigDecimal("1500000"))
                .salaryMax(new BigDecimal("2200000"))
                .description("Build microservices")
                .requirements("3+ years Java")
                .sourcePlatform("LINKEDIN")
                .sourceUrl("https://linkedin.com/jobs/view/123")
                .sourceJobId("LI-123")
                .scrapedAt("2024-01-15T06:00:00")
                .build();

        when(jobRepository.findBySourcePlatformAndSourceUrl(any(), any())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        jobPersistenceService.persistJob(message);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        Job saved = captor.getValue();

        assertEquals("Senior Java Developer", saved.getTitle());
        assertEquals("Infosys", saved.getCompany());
        assertEquals("Bangalore", saved.getLocation());
        assertEquals(JobType.FULL_TIME, saved.getJobType());
        assertEquals(new BigDecimal("1500000"), saved.getSalaryMin());
        assertEquals(SourcePlatform.LINKEDIN, saved.getSourcePlatform());
        assertEquals("LI-123", saved.getSourceJobId());
        assertTrue(saved.getIsActive());
        assertNotNull(saved.getScrapedAt());
    }

    @Test
    void persistJob_invalidPlatform_throwsException() {
        JobRawMessage message = buildMessage("INVALID_PLATFORM", "https://example.com/job/1");

        assertThrows(IllegalArgumentException.class, () -> jobPersistenceService.persistJob(message));
    }

    @Test
    void persistJob_nullPlatform_throwsException() {
        JobRawMessage message = buildMessage(null, "https://example.com/job/1");

        assertThrows(IllegalArgumentException.class, () -> jobPersistenceService.persistJob(message));
    }

    @Test
    void persistJob_unknownJobType_setsNull() {
        JobRawMessage message = buildMessage("NAUKRI", "https://naukri.com/job/123");
        message.setJobType("UNKNOWN_TYPE");

        when(jobRepository.findBySourcePlatformAndSourceUrl(any(), any())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        jobPersistenceService.persistJob(message);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertNull(captor.getValue().getJobType());
    }

    @Test
    void persistJob_nullScrapedAt_usesCurrentTime() {
        JobRawMessage message = buildMessage("INDEED", "https://indeed.com/job/999");
        message.setScrapedAt(null);

        when(jobRepository.findBySourcePlatformAndSourceUrl(any(), any())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        jobPersistenceService.persistJob(message);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertNotNull(captor.getValue().getScrapedAt());
    }

    @Test
    void persistJob_longTitle_isTruncated() {
        String longTitle = "A".repeat(300);
        JobRawMessage message = buildMessage("INDEED", "https://indeed.com/job/long");
        message.setTitle(longTitle);

        when(jobRepository.findBySourcePlatformAndSourceUrl(any(), any())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        jobPersistenceService.persistJob(message);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertEquals(255, captor.getValue().getTitle().length());
    }

    private JobRawMessage buildMessage(String platform, String sourceUrl) {
        return JobRawMessage.builder()
                .title("Test Developer")
                .company("Test Corp")
                .location("Remote")
                .sourcePlatform(platform)
                .sourceUrl(sourceUrl)
                .scrapedAt("2024-01-15T06:00:00")
                .build();
    }
}
