package com.example.qyuanpaperrd.config;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.obs.v1.ObsCredentials;
import com.huaweicloud.sdk.core.http.HttpConfig;
import com.huaweicloud.sdk.obs.v1.ObsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 华为云 OBS 配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "huawei.obs", name = "enabled", havingValue = "true", matchIfMissing = false)
public class HuaweiObsConfig {

    private final HuaweiObsProperties obsProperties;

    /**
     * 创建华为云 OBS 客户端 Bean
     *
     * @return OBS 客户端实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "huawei.obs", name = "enabled", havingValue = "true", matchIfMissing = false)
    public ObsClient obsClient() {
        if (log.isInfoEnabled()) {
            log.info("初始化华为云 OBS 客户端");
            log.info("Endpoint: {}", obsProperties.getEndpoint());
            log.info("Bucket: {}", obsProperties.getBucketName());
        }

        try {
            // 验证必需的配置
            validateProperties();

            // 创建认证信息
            ICredential auth = new ObsCredentials()
                    .withAk(obsProperties.getAccessKey())
                    .withSk(obsProperties.getSecretAccessKey());

            // 配置 HTTP 客户端
            HttpConfig config = HttpConfig.getDefaultHttpConfig();
            config.withIgnoreSSLVerification(true);

            // 创建 OBS 客户端
            ObsClient client = ObsClient.newBuilder()
                    .withHttpConfig(config)
                    .withCredential(auth)
                    .withEndpoint(obsProperties.getEndpoint())
                    .build();

            log.info("华为云 OBS 客户端初始化成功");
            return client;
        } catch (Exception e) {
            log.error("华为云 OBS 客户端初始化失败", e);
            throw new RuntimeException("初始化 OBS 客户端失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证 OBS 配置属性
     */
    private void validateProperties() {
        if (obsProperties.getEndpoint() == null || obsProperties.getEndpoint().trim().isEmpty()) {
            throw new IllegalArgumentException("华为云 OBS Endpoint 未配置");
        }
        if (obsProperties.getAccessKey() == null || obsProperties.getAccessKey().trim().isEmpty()) {
            throw new IllegalArgumentException("华为云 OBS AccessKey 未配置");
        }
        if (obsProperties.getSecretAccessKey() == null || obsProperties.getSecretAccessKey().trim().isEmpty()) {
            throw new IllegalArgumentException("华为云 OBS SecretAccessKey 未配置");
        }
        if (obsProperties.getBucketName() == null || obsProperties.getBucketName().trim().isEmpty()) {
            throw new IllegalArgumentException("华为云 OBS BucketName 未配置");
        }
    }
}