package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.ScrapeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapeLogRepository extends JpaRepository<ScrapeLog, Long> {
}
