package com.devops.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlMapping {
    private String shortCode;
    private String originalUrl;
    private LocalDateTime createdAt;
    private int clickCount;
    private List<ClickEvent> clickHistory;

    public UrlMapping(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = LocalDateTime.now();
        this.clickCount = 0;
        this.clickHistory = new ArrayList<>();
    }

    public void recordClick(String userAgent, String ipAddress) {
        this.clickCount++;
        this.clickHistory.add(new ClickEvent(
                LocalDateTime.now(),
                userAgent,
                ipAddress
        ));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClickEvent {
        private LocalDateTime timestamp;
        private String userAgent;
        private String ipAddress;
    }
}