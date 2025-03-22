package com.devops26.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import com.devops26.gateway.exception.TuneIslandException;
import com.devops26.gateway.util.TokenUtil;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class LoginFilterTest {
    @Mock
    private TokenUtil tokenUtil;

    @Mock
    private GatewayFilterChain chain;

    private LoginFilter loginFilter;

    @BeforeEach
    public void setUp() {
        loginFilter = new LoginFilter(tokenUtil);
    }

    @Test
    public void shouldPassForExcludedPath() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/user/login").build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = loginFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
        verify(chain).filter(exchange);
        verifyNoInteractions(tokenUtil);
    }

    @Test
    public void shouldPassForValidToken() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/protected/resource")
                .header("token", "valid-token")
                .build()
        );
        when(tokenUtil.verifyToken("valid-token")).thenReturn(Mono.just(true));
        when(chain.filter(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = loginFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
        verify(chain).filter(exchange);
        verify(tokenUtil).verifyToken("valid-token");
    }

    @Test
    public void shouldRejectForInvalidToken() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/protected/resource")
                .header("token", "invalid-token")
                .build()
        );
        when(tokenUtil.verifyToken("invalid-token")).thenReturn(Mono.just(false));

        // When
        Mono<Void> result = loginFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .expectError(TuneIslandException.class)
            .verify();
        verify(tokenUtil).verifyToken("invalid-token");
        verifyNoInteractions(chain);
    }

    @Test
    public void shouldRejectForMissingToken() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/protected/resource").build()
        );
        when(tokenUtil.verifyToken(null)).thenReturn(Mono.just(false));

        // When
        Mono<Void> result = loginFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .expectError(TuneIslandException.class)
            .verify();
        verify(tokenUtil).verifyToken(null);
        verifyNoInteractions(chain);
    }

    @Test
    public void shouldPassForWildcardExcludedPath() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/tools/something").build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = loginFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
        verify(chain).filter(exchange);
        verifyNoInteractions(tokenUtil);
    }
} 