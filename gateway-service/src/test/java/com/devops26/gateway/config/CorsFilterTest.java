package com.devops26.gateway.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

public class CorsFilterTest {

    @Test
    public void shouldConfigureCorsWebFilter() {
        // Given
        CorsFilter corsFilter = new CorsFilter();

        // When
        CorsWebFilter filter = corsFilter.corsWebFilter();

        // Then
        assertNotNull(filter, "CorsWebFilter should not be null");
    }
} 