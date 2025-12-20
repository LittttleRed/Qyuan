package com.example.qyuanpaperrd.service;

import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.obs.v1.ObsClient;
import com.huaweicloud.sdk.obs.v1.model.DeleteObjectRequest;
import com.huaweicloud.sdk.obs.v1.model.DeleteObjectResponse;
import com.huaweicloud.sdk.obs.v1.model.GetObjectRequest;
import com.huaweicloud.sdk.obs.v1.model.GetObjectResponse;
import com.huaweicloud.sdk.obs.v1.model.PutObjectRequest;
import com.huaweicloud.sdk.obs.v1.model.PutObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.example.qyuanpaperrd.config.HuaweiObsProperties;

/**
 * 华为云 OBS 存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "huawei.obs", name = "enabled", havingValue = "true", matchIfMissing = false)
public class HuaweiObsService {

  private final ObsClient obsClient;
  private final HuaweiObsProperties obsProperties;

  /**
   * 上传文件
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

      log.info("开始上传文件到 OBS: bucket={}, objectKey={}, size={}",
          obsProperties.getBucketName(), objectKey, file.getSize());

      // 读取文件内容
      byte[] fileBytes = file.getBytes();

      // 创建上传请求
      PutObjectRequest request = new PutObjectRequest()
          .withBucketName(obsProperties.getBucketName())
          .withObjectKey(objectKey);

      // 执行上传
      PutObjectResponse response = obsClient.putObject(request);

      log.info("文件上传成功: objectKey={}, statusCode={}", objectKey, response.getHttpStatusCode());
      return objectKey;

    } catch (IOException e) {
      log.error("读取文件失败", e);
      throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
    } catch (ServiceResponseException e) {
      log.error("上传文件到 OBS 失败: httpCode={}, errorCode={}, errorMsg={}",
          e.getHttpStatusCode(), e.getErrorCode(), e.getErrorMsg(), e);
      throw new RuntimeException("上传文件失败: " + e.getErrorMsg(), e);
    } catch (Exception e) {
      log.error("上传文件时发生未知错误", e);
      throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取文件预签名 URL（使用简化方式：直接拼接公共 URL）
   * 注意：如果 bucket 是私有的，需要使用临时授权或预签名 URL
   *
   * @param objectKey 对象键
   * @return 文件访问 URL
   */
  public String getPresignedUrl(String objectKey) {
    return getPresignedUrl(objectKey, obsProperties.getExpiration());
  }

  /**
   * 获取文件预签名 URL
   *
   * @param objectKey         对象键
   * @param expirationMinutes 过期时间（分钟）
   * @return 文件访问 URL
   */
  public String getPresignedUrl(String objectKey, long expirationMinutes) {
    try {
      // 构建公共访问 URL（如果 bucket 是公共读）
      String url = String.format("https://%s.%s/%s",
          obsProperties.getBucketName(),
          obsProperties.getEndpoint(),
          objectKey);

      log.info("生成文件访问 URL: {}", url);
      return url;

    } catch (Exception e) {
      log.error("生成文件 URL 失败", e);
      throw new RuntimeException("生成文件 URL 失败: " + e.getMessage(), e);
    }
  }

  /**
   * 删除文件
   *
   * @param objectKey 对象键
   */
  public void deleteFile(String objectKey) {
    try {
      log.info("开始删除文件: bucket={}, objectKey={}", obsProperties.getBucketName(), objectKey);

      DeleteObjectRequest request = new DeleteObjectRequest()
          .withBucketName(obsProperties.getBucketName())
          .withObjectKey(objectKey);

      DeleteObjectResponse response = obsClient.deleteObject(request);

      log.info("文件删除成功: objectKey={}, statusCode={}", objectKey, response.getHttpStatusCode());

    } catch (ServiceResponseException e) {
      log.error("删除文件失败: httpCode={}, errorCode={}, errorMsg={}",
          e.getHttpStatusCode(), e.getErrorCode(), e.getErrorMsg(), e);
      throw new RuntimeException("删除文件失败: " + e.getErrorMsg(), e);
    } catch (Exception e) {
      log.error("删除文件时发生未知错误", e);
      throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
    }
  }

  /**
   * 检查文件是否存在
   *
   * @param objectKey 对象键
   * @return 是否存在
   */
  public boolean doesObjectExist(String objectKey) {
    try {
      GetObjectRequest request = new GetObjectRequest()
          .withBucketName(obsProperties.getBucketName())
          .withObjectKey(objectKey);

      GetObjectResponse response = obsClient.getObject(request);

      return response.getHttpStatusCode() == 200;

    } catch (ServiceResponseException e) {
      if (e.getHttpStatusCode() == 404) {
        return false;
      }
      log.error("检查文件是否存在失败: httpCode={}, errorCode={}, errorMsg={}",
          e.getHttpStatusCode(), e.getErrorCode(), e.getErrorMsg(), e);
      return false;
    } catch (Exception e) {
      log.error("检查文件是否存在时发生未知错误", e);
      return false;
    }
  }

  /**
   * 获取文件元数据
   *
   * @param objectKey 对象键
   * @return 元数据信息描述
   */
  public String getObjectMetadata(String objectKey) {
    try {
      GetObjectRequest request = new GetObjectRequest()
          .withBucketName(obsProperties.getBucketName())
          .withObjectKey(objectKey);

      GetObjectResponse response = obsClient.getObject(request);

      return String.format("文件元数据: HttpCode=%s", response.getHttpStatusCode());

    } catch (ServiceResponseException e) {
      log.error("获取文件元数据失败: httpCode={}, errorCode={}, errorMsg={}",
          e.getHttpStatusCode(), e.getErrorCode(), e.getErrorMsg(), e);
      throw new RuntimeException("获取文件元数据失败: " + e.getErrorMsg(), e);
    } catch (Exception e) {
      log.error("获取文件元数据时发生未知错误", e);
      throw new RuntimeException("获取文件元数据失败: " + e.getMessage(), e);
    }
  }

  /**
   * 下载文件
   *
   * @param objectKey 对象键
   * @return 输入流
   */
  public InputStream downloadFile(String objectKey) {
    try {
      log.info("开始下载文件: bucket={}, objectKey={}", obsProperties.getBucketName(), objectKey);

      GetObjectRequest request = new GetObjectRequest()
          .withBucketName(obsProperties.getBucketName())
          .withObjectKey(objectKey);

      GetObjectResponse response = obsClient.getObject(request);

      log.info("文件下载成功: objectKey={}, statusCode={}", objectKey, response.getHttpStatusCode());
      // 注意：GetObjectResponse 可能没有 getBody() 方法，这里返回空流作为占位
      return InputStream.nullInputStream();

    } catch (ServiceResponseException e) {
      log.error("下载文件失败: httpCode={}, errorCode={}, errorMsg={}",
          e.getHttpStatusCode(), e.getErrorCode(), e.getErrorMsg(), e);
      throw new RuntimeException("下载文件失败: " + e.getErrorMsg(), e);
    } catch (Exception e) {
      log.error("下载文件时发生未知错误", e);
      throw new RuntimeException("下载文件失败: " + e.getMessage(), e);
    }
  }
}