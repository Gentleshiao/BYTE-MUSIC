package com.devops26.gateway.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class TokenUtilTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private TokenUtil tokenUtil;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        tokenUtil = new TokenUtil(webClientBuilder);
    }

    @Test
    public void shouldReturnFalseForNullToken() {
        StepVerifier.create(tokenUtil.verifyToken(null))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void shouldReturnFalseForInvalidToken() {

        StepVerifier.create(tokenUtil.verifyToken("invalid-token"));
    }

    @Test
    public void shouldReturnTrueForValidToken() {
        // Create a valid JWT token
        String token = JWT.create()
            .withAudience("123")
            .sign(Algorithm.HMAC256("secret"));

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("true"));

        StepVerifier.create(tokenUtil.verifyToken(token))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void shouldHandleServerError() {
        // Create a valid JWT token
        String token = JWT.create()
            .withAudience("123")
            .sign(Algorithm.HMAC256("secret"));

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Server error")));

        StepVerifier.create(tokenUtil.verifyToken(token))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void shouldReturnFalseForServerResponseFalse() {
        // Create a valid JWT token
        String token = JWT.create()
            .withAudience("123")
            .sign(Algorithm.HMAC256("secret"));

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("false"));

        StepVerifier.create(tokenUtil.verifyToken(token))
            .expectNext(false)
            .verifyComplete();
    }
} 