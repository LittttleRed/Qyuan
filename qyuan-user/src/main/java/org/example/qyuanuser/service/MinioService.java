package org.example.qyuanuser.service;


import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qyuanuser.config.MinioProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * MinIO 存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "minio", name = "endpoint")
public class MinioService {
    private final MinioProperties minioProperties;
    /**
     * 上传文件到 MinIO
     *
     * @param file   文件
     * @param folder 文件夹路径（可选）
     * @return 文件对象键
     */
    public String uploadFile(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        try {
            // 生成唯一文件名
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String objectKey = (folder != null && !folder.isEmpty() ? folder + "/" : "")
                    + UUID.randomUUID() + extension;

            log.info("开始上传文件到 MinIO: bucket={}, objectKey={}, size={}",
                    minioProperties.getBucketName(), objectKey, file.getSize());

            // 创建 MinIO 客户端
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            log.info("文件上传成功: bucket={}, objectKey={}", minioProperties.getBucketName(), objectKey);
            return objectKey;

        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("上传文件到 MinIO 失败", e);
            throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件的永久公开 URL（适用于公开桶）
     * @param objectKey 对象键
     * @return 文件访问 URL
     */
    public String getPublicUrl(String objectKey) {
        try {
            // 构造公开访问的 URL
            String endpoint = minioProperties.getEndpoint();
            String bucketName = minioProperties.getBucketName();
            
            // 确保 endpoint 不以斜杠结尾
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            
            // 构造 URL: endpoint/bucketName/objectKey
            String url = String.format("%s/%s/%s", endpoint, bucketName, objectKey);
            
            log.info("生成公开 URL: objectKey={}, url={}", objectKey, url);
            return url;

        } catch (Exception e) {
            log.error("生成公开 URL 时发生未知错误: objectKey={}", objectKey, e);
            throw new RuntimeException("生成公开 URL 失败: " + e.getMessage(), e);
        }
    }
}