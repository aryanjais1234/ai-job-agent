package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.Application;
import com.aryanjais.aijobagent.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Page<Application> findByUserId(Long userId, Pageable pageable);

    Page<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status, Pageable pageable);

    void deleteByUserId(Long userId);
}
