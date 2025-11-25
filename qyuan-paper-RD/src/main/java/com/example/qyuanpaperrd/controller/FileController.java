package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.service.HuaweiObsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传下载管理接口")
@ConditionalOnProperty(prefix = "huawei.obs", name = "enabled", havingValue = "true", matchIfMissing = false)
public class FileController {

    private final HuaweiObsService obsService;

    /**
     * 构建成功响应
     */
    private ResponseEntity<Map<String, String>> buildSuccessResponse(Map<String, String> data) {
        return ResponseEntity.ok(data);
    }

    /**
     * 构建错误响应
     */
    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到华为云 OBS")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "papers") String folder) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return buildErrorResponse("文件不能为空", HttpStatus.BAD_REQUEST);
            }

            if (log.isInfoEnabled()) {
                log.info("开始上传文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            }

            String objectKey = obsService.uploadFile(file, folder);
            String presignedUrl = obsService.getPresignedUrl(objectKey);

            Map<String, String> result = new HashMap<>();
            result.put("objectKey", objectKey);
            result.put("url", presignedUrl);
            result.put("message", "文件上传成功");

            return buildSuccessResponse(result);
        } catch (IllegalArgumentException e) {
            log.warn("文件上传参数错误: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return buildErrorResponse("文件上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/url/{objectKey}")
    @Operation(summary = "获取文件访问链接", description = "获取文件的预签名访问链接")
    public ResponseEntity<Map<String, String>> getFileUrl(@PathVariable String objectKey) {
        try {
            String url = obsService.getPresignedUrl(objectKey);
            Map<String, String> result = new HashMap<>();
            result.put("objectKey", objectKey);
            result.put("url", url);
            return buildSuccessResponse(result);
        } catch (Exception e) {
            log.error("获取文件 URL 失败: objectKey={}", objectKey, e);
            return buildErrorResponse("获取文件 URL 失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{objectKey}")
    @Operation(summary = "删除文件", description = "从华为云 OBS 删除文件")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String objectKey) {
        try {
            obsService.deleteFile(objectKey);
            Map<String, String> result = new HashMap<>();
            result.put("message", "文件删除成功");
            result.put("objectKey", objectKey);
            return buildSuccessResponse(result);
        } catch (Exception e) {
            log.error("删除文件失败: objectKey={}", objectKey, e);
            return buildErrorResponse("删除文件失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/exists/{objectKey}")
    @Operation(summary = "检查文件是否存在", description = "检查文件是否存在于华为云 OBS")
    public ResponseEntity<Map<String, Object>> checkFileExists(@PathVariable String objectKey) {
        try {
            boolean exists = obsService.doesObjectExist(objectKey);
            Map<String, Object> result = new HashMap<>();
            result.put("objectKey", objectKey);
            result.put("exists", exists);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("检查文件是否存在失败: objectKey={}", objectKey, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "检查文件失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
