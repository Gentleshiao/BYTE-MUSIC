package com.devops26.gateway.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devops26.gateway.entity.ResultVO;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void shouldHandleTuneIslandException() {
        // Given
        String errorMessage = "Test error message";
        TuneIslandException exception = TuneIslandException.notLogin();

        // When
        ResultVO<String> result = exceptionHandler.handleAIExternalException(exception);

        // Then
        assertNotNull(result);
        assertEquals("400", result.getCode());
        assertEquals("未登录!", result.getMsg());
    }

    @Test
    public void shouldHandleTuneIslandExceptionWithCustomMessage() {
        // Given
        String errorMessage = "Custom error message";
        TuneIslandException exception = new TuneIslandException(errorMessage);

        // When
        ResultVO<String> result = exceptionHandler.handleAIExternalException(exception);

        // Then
        assertNotNull(result);
        assertEquals("400", result.getCode());
        assertEquals(errorMessage, result.getMsg());
    }
} 