package com.devops.urlshortener.controller;

import com.devops.urlshortener.model.UrlMapping;
import com.devops.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        log.info("Received shorten request for URL: {}", request.getUrl());

        if (!isValidUrl(request.getUrl())) {
            log.warn("Invalid URL format: {}", request.getUrl());
            return ResponseEntity.badRequest().build();
        }

        UrlMapping mapping = urlService.shortenUrl(request.getUrl());
        String shortUrl = baseUrl + "/" + mapping.getShortCode();

        ShortenResponse response = new ShortenResponse(
                mapping.getShortCode(),
                shortUrl,
                mapping.getOriginalUrl(),
                mapping.getCreatedAt().toString()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirect(@PathVariable String shortCode, HttpServletRequest request) {
        log.info("Redirect request for short code: {}", shortCode);

        return urlService.getOriginalUrl(shortCode)
                .map(mapping -> {
                    String userAgent = request.getHeader("User-Agent");
                    String ipAddress = getClientIp(request);

                    urlService.recordClick(shortCode, userAgent, ipAddress);
                    log.info("Redirecting {} to {}", shortCode, mapping.getOriginalUrl());

                    return new RedirectView(mapping.getOriginalUrl());
                })
                .orElseGet(() -> {
                    log.warn("Short code not found: {}", shortCode);
                    return new RedirectView("/api/error");
                });
    }

    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<StatsResponse> getStats(@PathVariable String shortCode) {
        log.info("Stats request for short code: {}", shortCode);

        return urlService.getStats(shortCode)
                .map(mapping -> {
                    StatsResponse response = new StatsResponse(
                            mapping.getShortCode(),
                            mapping.getOriginalUrl(),
                            mapping.getClickCount(),
                            mapping.getCreatedAt().toString(),
                            mapping.getClickHistory()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("totalUrls", urlService.getTotalUrls());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/api/error")
    public ResponseEntity<Map<String, String>> error() {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Short URL not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    private boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (Exception e) {
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Data
    public static class ShortenRequest {
        @NotBlank(message = "URL cannot be blank")
        private String url;
    }

    @Data
    @RequiredArgsConstructor
    public static class ShortenResponse {
        private final String shortCode;
        private final String shortUrl;
        private final String originalUrl;
        private final String createdAt;
    }

    @Data
    @RequiredArgsConstructor
    public static class StatsResponse {
        private final String shortCode;
        private final String originalUrl;
        private final int clickCount;
        private final String createdAt;
        private final Object clickHistory;
    }
}