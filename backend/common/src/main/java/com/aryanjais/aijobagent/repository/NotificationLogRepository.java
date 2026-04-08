package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Page<NotificationLog> findByUserIdOrderBySentAtDesc(Long userId, Pageable pageable);

    void deleteByUserId(Long userId);
}
