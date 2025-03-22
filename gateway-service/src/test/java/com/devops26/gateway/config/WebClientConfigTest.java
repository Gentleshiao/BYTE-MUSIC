package com.devops26.gateway.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class WebClientConfigTest {
    private WebClientConfig webClientConfig = new WebClientConfig();

    @Test
    void webClientBuilder_ShouldCreateBuilder() {
        WebClient.Builder builder = webClientConfig.webClientBuilder();
        assertNotNull(builder, "WebClient.Builder should not be null");
    }
} 