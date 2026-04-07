package com.aryanjais.aijobagent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's @Scheduled annotation support for cron-based task scheduling.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
