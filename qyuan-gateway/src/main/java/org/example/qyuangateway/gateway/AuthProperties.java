package org.example.qyuangateway.gateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "demo.gateway.auth")
public class AuthProperties {
    private List<String> excludePaths;

    private List<MethodExcludePath> excludeMethodPaths;

    @Data
    public static class MethodExcludePath {
        // HTTP方法：GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD, TRACE
        private String method;
        // 排除的路径列表
        private List<String> paths;
    }
}
