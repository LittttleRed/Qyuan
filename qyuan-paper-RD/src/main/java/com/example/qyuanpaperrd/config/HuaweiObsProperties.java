package com.example.qyuanpaperrd.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 华为云 OBS 配置属性
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "huawei.obs")
public class HuaweiObsProperties {

    /**
     * 是否启用 OBS
     */
    private boolean enabled = true;

    /**
     * OBS 服务端点
     */
    @NotBlank(message = "华为云 OBS Endpoint 不能为空")
    private String endpoint;

    /**
     * Access Key
     */
    @NotBlank(message = "华为云 OBS Access Key 不能为空")
    private String accessKey;

    /**
     * Secret Access Key
     */
    @NotBlank(message = "华为云 OBS Secret Access Key 不能为空")
    private String secretAccessKey;

    /**
     * 桶名称
     */
    @NotBlank(message = "华为云 OBS Bucket Name 不能为空")
    private String bucketName;

    /**
     * 预签名 URL 过期时间（分钟）
     */
    @Min(value = 1, message = "预签名 URL 过期时间必须大于 0")
    private Long expiration = 60L;
}
