package com.devops.urlshortener.service;

import com.devops.urlshortener.model.UrlMapping;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UrlService {
    private final Map<String, UrlMapping> urlStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Counter urlCreatedCounter;
    private final Counter urlAccessedCounter;

    @Value("${app.short-code-length:6}")
    private int shortCodeLength;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public UrlService(MeterRegistry meterRegistry) {
        this.urlCreatedCounter = Counter.builder("url.created")
                .description("Number of URLs shortened")
                .register(meterRegistry);
        this.urlAccessedCounter = Counter.builder("url.accessed")
                .description("Number of short URL accesses")
                .register(meterRegistry);
    }

    public UrlMapping shortenUrl(String originalUrl) {
        log.info("Creating short URL for: {}", originalUrl);


        String shortCode = generateShortCode();
        UrlMapping mapping = new UrlMapping(shortCode, originalUrl);
        urlStore.put(shortCode, mapping);

        urlCreatedCounter.increment();
        log.debug("Short code generated: {} -> {}", shortCode, originalUrl);

        return mapping;
    }

    public Optional<UrlMapping> getOriginalUrl(String shortCode) {
        log.debug("Looking up short code: {}", shortCode);
        return Optional.ofNullable(urlStore.get(shortCode));
    }

    public void recordClick(String shortCode, String userAgent, String ipAddress) {
        UrlMapping mapping = urlStore.get(shortCode);
        if (mapping != null) {
            mapping.recordClick(userAgent, ipAddress);
            urlAccessedCounter.increment();
            log.info("Click recorded for {} from IP: {}", shortCode, ipAddress);
        }
    }

    public Optional<UrlMapping> getStats(String shortCode) {
        return Optional.ofNullable(urlStore.get(shortCode));
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder(shortCodeLength);
        for (int i = 0; i < shortCodeLength; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public int getTotalUrls() {
        return urlStore.size();
    }
}