package com.devops.urlshortener;

import com.devops.urlshortener.model.UrlMapping;
import com.devops.urlshortener.service.UrlService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    private UrlService urlService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        urlService = new UrlService(meterRegistry);
        setShortCodeLength(urlService, 6);
    }

    @Test
    void shortenUrl_ValidUrl_ReturnsUrlMapping() {
        UrlMapping mapping = urlService.shortenUrl("https://example.com");

        assertNotNull(mapping);
        assertNotNull(mapping.getShortCode());
        assertEquals(6, mapping.getShortCode().length());
        assertEquals("https://example.com", mapping.getOriginalUrl());
        assertNotNull(mapping.getCreatedAt());
        assertEquals(0, mapping.getClickCount());
        assertNotNull(mapping.getClickHistory());
        assertTrue(mapping.getClickHistory().isEmpty());
    }

    @Test
    void shortenUrl_DuplicateUrl_ReturnsDifferentCodes() {
        String url = "https://example.com";
        UrlMapping mapping1 = urlService.shortenUrl(url);
        UrlMapping mapping2 = urlService.shortenUrl(url);

        assertNotEquals(mapping1.getShortCode(), mapping2.getShortCode());
    }

    @Test
    void getOriginalUrl_ValidCode_ReturnsMapping() {
        UrlMapping created = urlService.shortenUrl("https://example.com");
        Optional<UrlMapping> retrieved = urlService.getOriginalUrl(created.getShortCode());

        assertTrue(retrieved.isPresent());
        assertEquals(created.getOriginalUrl(), retrieved.get().getOriginalUrl());
    }

    @Test
    void getOriginalUrl_InvalidCode_ReturnsEmpty() {
        Optional<UrlMapping> result = urlService.getOriginalUrl("invalid");
        assertFalse(result.isPresent());
    }

    @Test
    void recordClick_ValidCode_IncrementsCount() {
        UrlMapping mapping = urlService.shortenUrl("https://example.com");
        int initialCount = mapping.getClickCount();

        urlService.recordClick(mapping.getShortCode(), "Test-Agent", "127.0.0.1");

        Optional<UrlMapping> updated = urlService.getOriginalUrl(mapping.getShortCode());
        assertTrue(updated.isPresent());
        assertEquals(initialCount + 1, updated.get().getClickCount());
        assertEquals(1, updated.get().getClickHistory().size());
    }

    @Test
    void recordClick_InvalidCode_DoesNothing() {
        // Should not throw exception for invalid code
        assertDoesNotThrow(() -> {
            urlService.recordClick("invalid", "Test-Agent", "127.0.0.1");
        });
    }

    @Test
    void getStats_ValidCode_ReturnsMapping() {
        UrlMapping mapping = urlService.shortenUrl("https://example.com");
        Optional<UrlMapping> stats = urlService.getStats(mapping.getShortCode());

        assertTrue(stats.isPresent());
        assertEquals(mapping.getShortCode(), stats.get().getShortCode());
    }

    @Test
    void getStats_InvalidCode_ReturnsEmpty() {
        Optional<UrlMapping> result = urlService.getStats("invalid");
        assertFalse(result.isPresent());
    }

    @Test
    void getTotalUrls_EmptyStore_ReturnsZero() {
        assertEquals(0, urlService.getTotalUrls());
    }

    @Test
    void getTotalUrls_AfterAddingUrls_ReturnsCount() {
        urlService.shortenUrl("https://example1.com");
        urlService.shortenUrl("https://example2.com");

        assertEquals(2, urlService.getTotalUrls());
    }

    @Test
    void metrics_IncrementCounters() {
        // Initial counters should be 0
        assertEquals(0, meterRegistry.find("url.created").counter().count());
        assertEquals(0, meterRegistry.find("url.accessed").counter().count());

        // Create a URL and access it
        UrlMapping mapping = urlService.shortenUrl("https://example.com");
        urlService.recordClick(mapping.getShortCode(), "Test-Agent", "127.0.0.1");

        // Verify counters incremented
        assertEquals(1, meterRegistry.find("url.created").counter().count());
        assertEquals(1, meterRegistry.find("url.accessed").counter().count());
    }

    @Test
    void shortCodeGeneration_AlwaysSixCharacters() {
        // Test with completely unique URLs to avoid duplicate detection
        for (int i = 0; i < 10; i++) {
            String uniqueUrl = String.format("https://example-%d.com/%d", System.currentTimeMillis(), i);
            UrlMapping mapping = urlService.shortenUrl(uniqueUrl);
            assertEquals(6, mapping.getShortCode().length(),
                    "Short code should be 6 characters for URL: " + uniqueUrl);
        }
    }

    @Test
    void shortCodeGeneration_OnlyValidCharacters() {
        String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        for (int i = 0; i < 50; i++) {
            UrlMapping mapping = urlService.shortenUrl("https://example.com/" + i);
            String shortCode = mapping.getShortCode();

            for (char c : shortCode.toCharArray()) {
                assertTrue(validChars.indexOf(c) >= 0,
                        "Invalid character in short code: " + c);
            }
        }
    }

    private void setShortCodeLength(UrlService service, int length) {
        try {
            Field field = UrlService.class.getDeclaredField("shortCodeLength");
            field.setAccessible(true);
            field.set(service, length);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set shortCodeLength", e);
        }
    }
}