package com.aryanjais.aijobagent.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNotificationPreferencesRequest {
    private Boolean emailEnabled;
    private Boolean dailyDigest;
    private Integer matchThreshold;
    private Boolean newJobAlerts;
    private Boolean applicationUpdates;
    private LocalTime digestTime;
}
