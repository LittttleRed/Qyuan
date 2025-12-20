package com.example.qyuanpaperrd.config;

import com.example.qyuanpaperrd.common.Result;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(Result.validateError("参数校验失败: " + errorMessage));
    }

    /**
     * 处理参数绑定异常（表单参数）
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<String>> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("参数绑定失败: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(Result.validateError("参数校验失败: " + errorMessage));
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        assert e.getRequiredType() != null;
        String errorMessage = String.format("参数 '%s' 类型不匹配，期望类型: %s",
                e.getName(), e.getRequiredType().getSimpleName());

        log.warn("参数类型不匹配: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(Result.validateError(errorMessage));
    }

    /**
     * 处理参数约束违反异常（@RequestParam, @PathVariable）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<String>> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数约束违反: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(Result.validateError("参数校验失败: " + errorMessage));
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<String>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .body(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.notFound(e.getMessage()));
    }

    /**
     * 处理未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<String>> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("未授权访问: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.unauthorized(e.getMessage()));
    }

    /**
     * 处理禁止访问异常
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result<String>> handleForbiddenException(ForbiddenException e) {
        log.warn("禁止访问: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.forbidden(e.getMessage()));
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统内部错误，请稍后重试"));
    }

    /**
     * 自定义业务异常
     */
    @Getter
    public static class BusinessException extends RuntimeException {
        private final Integer code;
        private final HttpStatus httpStatus;

        public BusinessException(String message) {
            this(500, HttpStatus.INTERNAL_SERVER_ERROR, message);
        }

        public BusinessException(Integer code, HttpStatus httpStatus, String message) {
            super(message);
            this.code = code;
            this.httpStatus = httpStatus;
        }

    }

    /**
     * 资源不存在异常
     */
    public static class ResourceNotFoundException extends BusinessException {
        public ResourceNotFoundException(String message) {
            super(404, HttpStatus.NOT_FOUND, message);
        }
    }

    /**
     * 未授权异常
     */
    public static class UnauthorizedException extends BusinessException {
        public UnauthorizedException(String message) {
            super(401, HttpStatus.UNAUTHORIZED, message);
        }
    }

    /**
     * 禁止访问异常
     */
    public static class ForbiddenException extends BusinessException {
        public ForbiddenException(String message) {
            super(403, HttpStatus.FORBIDDEN, message);
        }
    }
}