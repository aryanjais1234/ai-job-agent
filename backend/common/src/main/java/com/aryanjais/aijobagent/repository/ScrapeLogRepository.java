package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.ScrapeLog;
import com.aryanjais.aijobagent.entity.enums.ScrapePlatform;
import com.aryanjais.aijobagent.entity.enums.ScrapeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapeLogRepository extends JpaRepository<ScrapeLog, Long> {

    Page<ScrapeLog> findAllByOrderByStartedAtDesc(Pageable pageable);

    Page<ScrapeLog> findByStatusOrderByStartedAtDesc(ScrapeStatus status, Pageable pageable);

    Page<ScrapeLog> findByPlatformOrderByStartedAtDesc(ScrapePlatform platform, Pageable pageable);

    Page<ScrapeLog> findByStatusAndPlatformOrderByStartedAtDesc(
            ScrapeStatus status, ScrapePlatform platform, Pageable pageable);
}
