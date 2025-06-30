package com.shoujike.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.example.authservice.util.JwtUtil;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 登录注册接口直接放行
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            return chain.filter(exchange);
        }

        // 获取 token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || !JwtUtil.isTokenValid(token)) {
            // 拒绝访问
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // token 合法，放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
