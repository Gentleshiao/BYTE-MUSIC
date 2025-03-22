package com.devops26.gateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.devops26.gateway.exception.TuneIslandException;
import com.devops26.gateway.util.TokenUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoginFilter implements GlobalFilter, Ordered {

    private final TokenUtil tokenUtil;
    private final List<String> excludedPaths;

    public LoginFilter(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
        this.excludedPaths = Arrays.asList(
            "/user/register",
            "/user/login",
            "/user/getUserById",
            "/tools/**",
            "/songs/getAllSongs",
            "/songs/getSongById",
            "/songs/searchByName",
            "/songs/searchBySinger",
            "/songs/play",
            "/songs/getByTag",
            "/songs/search",
            "/songs/hotSongs",
            "/songs/getListByTag",
            "/songlist/getAllByOwnerId",
            "/songlist/getBySonglistId",
            "/songlist/getByName",
            "/songlist/getPublicSonglists",
            "/songlist/getRecommendations",
            "/comment/getSongCommentByArtId",
            "/comment/getSonglistCommentByArtId",
            "/comment/getByCommentId"
        );
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // 检查是否是排除的路径
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("token");
        return tokenUtil.verifyToken(token)
            .flatMap(isValid -> {
                if (isValid) {
                    return chain.filter(exchange);
                } else {
                    log.warn("Token verification failed at api: {}", path);
                    return Mono.error(TuneIslandException.notLogin());
                }
            });
    }

    private boolean isExcludedPath(String path) {
        return excludedPaths.stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        String prefix = pattern.substring(0, pattern.length() - 3);
                        return path.startsWith(prefix);
                    }
                    return path.equals(pattern);
                });
    }

    @Override
    public int getOrder() {
        return -1; // 确保这个过滤器最先执行
    }
} 