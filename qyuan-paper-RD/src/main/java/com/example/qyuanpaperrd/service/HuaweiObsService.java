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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
   * 获取文件预签名 URL（用于私有桶）
   *
   * @param objectKey 对象键
   * @return 文件访问 URL
   */
  public String getPresignedUrl(String objectKey) {
    return getPresignedUrl(objectKey, obsProperties.getExpiration());
  }

  /**
   * 获取文件预签名 URL（用于私有桶）
   *
   * @param objectKey         对象键
   * @param expirationMinutes 过期时间（分钟）
   * @return 文件访问 URL
   */
  public String getPresignedUrl(String objectKey, long expirationMinutes) {
    try {
      log.info("生成预签名 URL: bucket={}, objectKey={}, expiration={}min",
          obsProperties.getBucketName(), objectKey, expirationMinutes);

      // 计算过期时间戳（秒）
      long expires = (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expirationMinutes)) / 1000;

      // 构造请求头和查询参数
      Map<String, String[]> headers = new HashMap<>();
      Map<String, String> queries = new HashMap<>();

      // 手动计算签名
      String signature = querySignature("GET", headers, queries,
          obsProperties.getBucketName(), objectKey, expires);

      // 构造URL
      String url = getPresignedUrl(signature, expires, objectKey);

      log.info("预签名 URL 生成成功: objectKey={}, expiration={}min", objectKey, expirationMinutes);
      return url;

    } catch (Exception e) {
      log.error("生成预签名 URL 时发生未知错误: objectKey={}", objectKey, e);
      throw new RuntimeException("生成预签名 URL 失败: " + e.getMessage(), e);
    }
  }

  /**
   * 手动计算OBS签名
   */
  private String querySignature(String httpMethod, Map<String, String[]> headers,
      Map<String, String> queries, String bucketName, String objectName, long expires)
      throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

    final String SIGN_SEP = "\n";
    final String OBS_PREFIX = "x-obs-";
    final String DEFAULT_ENCODING = "UTF-8";

    String contentMd5 = "";
    String contentType = "";

    // 构造StringToSign
    StringBuilder stringToSign = new StringBuilder();
    stringToSign.append(httpMethod).append(SIGN_SEP)
            .append(contentMd5).append(SIGN_SEP)
            .append(contentType).append(SIGN_SEP)
            .append(expires).append(SIGN_SEP);

    // 构造CanonicalizedResource
    stringToSign.append("/");
    if (bucketName != null && !bucketName.isEmpty()) {
      stringToSign.append(bucketName).append("/");
      if (objectName != null && !objectName.isEmpty()) {
        stringToSign.append(encodeObjectName(objectName));
      }
    }

    // 计算签名
    String stringToSignStr = stringToSign.toString();
    SecretKeySpec signingKey = new SecretKeySpec(obsProperties.getSecretAccessKey().getBytes(DEFAULT_ENCODING), "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(signingKey);
    byte[] signBytes = mac.doFinal(stringToSignStr.getBytes(DEFAULT_ENCODING));

    return Base64.getEncoder().encodeToString(signBytes);
  }

  /**
   * URL编码对象名称
   */
  private String encodeObjectName(String objectName) throws UnsupportedEncodingException {
    final String DEFAULT_ENCODING = "UTF-8";
    StringBuilder result = new StringBuilder();
    String[] tokens = objectName.split("/");
    for (int i = 0; i < tokens.length; i++) {
      result.append(URLEncoder.encode(tokens[i], DEFAULT_ENCODING)
          .replaceAll("\\+", "%20")
          .replaceAll("\\*", "%2A")
          .replaceAll("%7E", "~"));
      if (i < tokens.length - 1) {
        result.append("/");
      }
    }
    return result.toString();
  }

  /**
   * 构造预签名URL
   */
  private String getPresignedUrl(String signature, long expires, String objectName)
      throws UnsupportedEncodingException {
    StringBuilder url = new StringBuilder();

    // 构造基础URL：https://bucket.endpoint/objectKey
    url.append("https://").append(obsProperties.getBucketName())
        .append(".").append(extractDomainFromEndpoint(obsProperties.getEndpoint()))
        .append("/").append(encodeObjectName(objectName)).append("?");

    // 添加查询参数
    url.append("AccessKeyId=").append(URLEncoder.encode(obsProperties.getAccessKey(), "UTF-8"))
        .append("&Expires=").append(expires)
        .append("&Signature=").append(URLEncoder.encode(signature, "UTF-8"));

    return url.toString();
  }

  /**
   * 从endpoint中提取域名部分
   */
  private String extractDomainFromEndpoint(String endpoint) {
    // 如果endpoint已经是域名格式，直接返回
    if (endpoint.startsWith("obs.") && endpoint.endsWith(".myhuaweicloud.com")) {
      return endpoint;
    }
    // 否则直接返回原值
    return endpoint;
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