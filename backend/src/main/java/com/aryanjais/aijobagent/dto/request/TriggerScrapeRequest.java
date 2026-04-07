package com.aryanjais.aijobagent.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerScrapeRequest {

    private String keywords;

    private String location;

    @Builder.Default
    private Integer maxResults = 25;
}
