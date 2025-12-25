package org.example.qyuanuser.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * MinIO 配置属性
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * 是否启用 MinIO
     */
    private boolean enabled = true;

    /**
     * MinIO 服务端点
     */
    @NotBlank(message = "MinIO Endpoint 不能为空")
    private String endpoint =  "http://120.46.205.113:9098";

    /**
     * Access Key
     */
    @NotBlank(message = "MinIO Access Key 不能为空")
    private String accessKey = "Uk8HWGjFTcurrYlNgMaq";

    /**
     * Secret Access Key
     */
    @NotBlank(message = "MinIO Secret Access Key 不能为空")
    private String secretKey = "0FM2WMsXjkKiLvugxGzagqfRyR4tHuPPOCT4Su8w";

    /**
     * 桶名称
     */
    @NotBlank(message = "MinIO Bucket Name 不能为空")
    private String bucketName = "avatar";

}