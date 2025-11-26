package org.example.qyuangateway.filter;


import lombok.RequiredArgsConstructor;
import org.example.qyuangateway.common.exception.UnauthorizedException;
import org.example.qyuangateway.common.utils.CollUtils;

import org.example.qyuangateway.gateway.AuthProperties;
import org.example.qyuangateway.gateway.util.JwtTool;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTool jwtTool;

    private final AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取Request
        ServerHttpRequest request = exchange.getRequest();
        System.out.println("request = " + request);
        // 2.判断是否不需要拦截
        if(isExclude(request.getPath().toString(), request.getMethod().toString())){
            // 无需拦截，直接放行
            return chain.filter(exchange);
        }
//         3.获取请求头中的token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        System.out.println("Authorization header: " + authHeader);
        System.out.println("All headers: " + request.getHeaders());

        String token = null;
        System.out.println("token = " + token);
        List<String> headers = request.getHeaders().get("authorization");
        if (!CollUtils.isEmpty(headers)) {
            token = headers.get(0);
        }
        // 4.校验并解析token
        String userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            // 如果无效，拦截
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            return response.setComplete();
        }

        // TODO 5.如果有效，传递用户信息
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("USER-ID", userId)
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        // 6.放行
        return chain.filter(exchange);
    }

    private boolean isExclude(String requestPath, String requestMethod) {
        // 检查是否有针对特定方法的排除路径配置
        if (!CollUtils.isEmpty(authProperties.getExcludeMethodPaths())) {
            for (AuthProperties.MethodExcludePath methodExcludePath : authProperties.getExcludeMethodPaths()) {
                // 检查当前请求方法是否在排除配置中
                if (requestMethod.equalsIgnoreCase(methodExcludePath.getMethod())) {
                    // 检查当前路径是否在该方法的排除路径列表中
                    if (!CollUtils.isEmpty(methodExcludePath.getPaths())) {
                        for (String pathPattern : methodExcludePath.getPaths()) {
                            if (antPathMatcher.match(pathPattern, requestPath)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        // 检查通用的排除路径配置（不区分方法）
        if (!CollUtils.isEmpty(authProperties.getExcludePaths())) {
            for (String pathPattern : authProperties.getExcludePaths()) {
                if (antPathMatcher.match(pathPattern, requestPath)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public int getOrder() {
        return 0;
    }
}