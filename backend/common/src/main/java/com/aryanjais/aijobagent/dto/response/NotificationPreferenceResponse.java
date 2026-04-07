package com.aryanjais.aijobagent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceResponse {
    private boolean emailEnabled;
    private boolean dailyDigest;
    private int matchThreshold;
    private boolean newJobAlerts;
    private boolean applicationUpdates;
    private LocalTime digestTime;
}
