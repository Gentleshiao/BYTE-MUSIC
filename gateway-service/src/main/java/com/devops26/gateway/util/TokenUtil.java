package com.devops26.gateway.util;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TokenUtil {
    private final WebClient webClient;

    public TokenUtil(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://user-service")
                .build();
    }

    public Mono<Boolean> verifyToken(String token) {
        if (token == null) {
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
            try {
                String userId = JWT.decode(token).getAudience().get(0);
                log.debug("Attempting to verify token for user: {}", userId);
                return userId;
            } catch (Exception e) {
                log.error("Failed to decode token: {}", e.getMessage());
                return null;
            }
        })
        .flatMap(userId -> {
            if (userId == null) {
                return Mono.just(false);
            }

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token", token);

            return webClient.post()
                    .uri("/user/verifyToken")
                    .bodyValue(formData)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        log.debug("Token verification response: {}", response);
                        return response.contains("true");
                    })
                    .onErrorResume(error -> {
                        log.error("Token verification failed: {}", error.getMessage());
                        return Mono.just(false);
                    });
        });
    }
} 