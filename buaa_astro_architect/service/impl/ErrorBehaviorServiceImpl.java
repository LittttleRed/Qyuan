package org.buaa.buaa_astro_architect.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.buaa.buaa_astro_architect.entity.error_model.ErrorBehavior;
import org.buaa.buaa_astro_architect.entity.error_model.ViewType;
import org.buaa.buaa_astro_architect.mapper.error_model.ErrorBehaviorMapper;
import org.buaa.buaa_astro_architect.service.IErrorBehaviorService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ErrorBehaviorServiceImpl extends ServiceImpl<ErrorBehaviorMapper, ErrorBehavior> implements IErrorBehaviorService {
    @Resource
    private ErrorBehaviorMapper errorBehaviorMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    private long CACHE_EXPIRE_TIME = 5;
    private TimeUnit CACHE_EXPIRE_TIME_UNIT = TimeUnit.MINUTES;
    private String errorBehaviorRedisPrefix = "FTA:ErrorBehavior:";

    private void setStringValue(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Failed to set value in Redis for key: {}", key, e);
        }
    }

    private String getStringValue(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Failed to get value from Redis for key: {}", key, e);
            return null;
        }
    }

    private String generateFullKey(String viewId, String viewType) {
        return errorBehaviorRedisPrefix + viewType + ":" + viewId;
    }

    @Override
    public String getCompositeErrorBehavior(String viewId, String viewType) {
        String fullKey = generateFullKey(viewId, viewType);
        String result = getStringValue(fullKey);
        if (result == null) {
            try {
                result = errorBehaviorMapper.getCompositeErrorBehavior(viewId, viewType);
                result = result == null ? "" : result;
                setStringValue(fullKey, result, CACHE_EXPIRE_TIME, CACHE_EXPIRE_TIME_UNIT);
            } catch (Exception e) {
                log.error("Failed to get composite error behavior from MySQL for viewId: {}, viewType: {}", viewId, viewType, e);
            }
        }
        return result;
    }

    @Override
    public void setCompositeErrorBehavior(String viewId, String viewType, String compositeErrorBehavior) {
        try {
            Integer id = errorBehaviorMapper.getIdByView(viewId, viewType);
            if (id == null) {
                ErrorBehavior errorBehavior = new ErrorBehavior();
                errorBehavior.setCompositeErrorBehavior(compositeErrorBehavior);
                errorBehavior.setComponentErrorBehavior("");
                errorBehavior.setViewId(viewId);
                errorBehavior.setViewType(ViewType.valueOf(viewType));
                this.save(errorBehavior);
            } else {
                errorBehaviorMapper.setCompositeErrorBehavior(id, compositeErrorBehavior);
            }
            String fullKey = generateFullKey(viewId, viewType);
            setStringValue(fullKey, compositeErrorBehavior, CACHE_EXPIRE_TIME, CACHE_EXPIRE_TIME_UNIT);
        } catch (Exception e) {
            log.error("Failed to set composite error behavior for viewId: {}, viewType: {}", viewId, viewType, e);
        }
    }
}