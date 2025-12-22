package com.devops.urlshortener;

import com.devops.urlshortener.controller.UrlController;
import com.devops.urlshortener.model.UrlMapping;
import com.devops.urlshortener.service.UrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlService urlService;

    @Autowired
    private ObjectMapper objectMapper;

    private UrlController.ShortenRequest shortenRequest;
    private UrlMapping testUrlMapping;

    @BeforeEach
    void setUp() {
        shortenRequest = new UrlController.ShortenRequest();
        shortenRequest.setUrl("https://example.com");

        testUrlMapping = new UrlMapping("abc123", "https://example.com");
    }

    @Test
    void shortenUrl_ValidUrl_ReturnsCreated() throws Exception {
        when(urlService.shortenUrl("https://example.com")).thenReturn(testUrlMapping);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shortenUrl_EmptyUrl_ReturnsBadRequest() throws Exception {
        shortenRequest.setUrl("");

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenUrl_InvalidUrlFormat_ReturnsBadRequest() throws Exception {
        // Test URL without scheme
        shortenRequest.setUrl("example.com");

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isBadRequest());

        // Test URL with wrong scheme
        shortenRequest.setUrl("ftp://example.com");

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirect_ValidShortCode_ReturnsRedirect() throws Exception {
        when(urlService.getOriginalUrl("abc123")).thenReturn(Optional.of(testUrlMapping));
        doNothing().when(urlService).recordClick(eq("abc123"), anyString(), anyString());

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://example.com"));
    }

    @Test
    void redirect_InvalidShortCode_RedirectsToError() throws Exception {
        when(urlService.getOriginalUrl("invalid")).thenReturn(Optional.empty());

        mockMvc.perform(get("/invalid"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/api/error"));
    }

    @Test
    void getStats_ValidShortCode_ReturnsStats() throws Exception {

        // Create a UrlMapping with clickHistory initialized
        UrlMapping mockMapping = new UrlMapping();
        mockMapping.setShortCode("abc123");
        mockMapping.setOriginalUrl("https://example.com");
        mockMapping.setClickCount(5);
        mockMapping.setCreatedAt(LocalDateTime.now());
        mockMapping.setClickHistory(new ArrayList<>());

        when(urlService.getStats("abc123")).thenReturn(Optional.of(testUrlMapping));

        mockMvc.perform(get("/api/stats/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.clickCount").value(0))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.clickHistory").exists());
    }

    @Test
    void getStats_InvalidShortCode_ReturnsNotFound() throws Exception {
        when(urlService.getStats("invalid")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/stats/invalid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void health_ReturnsUpStatus() throws Exception {
        when(urlService.getTotalUrls()).thenReturn(5);

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.totalUrls").value(5));
    }

    @Test
    void error_ReturnsNotFoundError() throws Exception {
        mockMvc.perform(get("/api/error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Short URL not found"));
    }

    @Test
    void shortenUrl_MalformedJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenUrl_NullUrl_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": null}"))
                .andExpect(status().isBadRequest());
    }
}