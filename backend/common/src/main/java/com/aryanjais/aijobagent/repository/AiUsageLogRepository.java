package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
}
