package org.buaa.buaa_astro_architect.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.buaa.buaa_astro_architect.entity.error_model.Distribution;
import org.buaa.buaa_astro_architect.entity.error_model.ErrorProperty;
import org.buaa.buaa_astro_architect.entity.error_model.ViewType;
import org.buaa.buaa_astro_architect.entity.neo4j.ModuleNode;
import org.buaa.buaa_astro_architect.mapper.error_model.ErrorPropertyMapper;
import org.buaa.buaa_astro_architect.mapper.neo4j.ArchitectureNodeMapper;
import org.buaa.buaa_astro_architect.mapper.neo4j.FunctionNodeMapper;
import org.buaa.buaa_astro_architect.mapper.neo4j.ModuleNodeMapper;
import org.buaa.buaa_astro_architect.service.IErrorPropertyService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ErrorPropertyServiceImpl extends ServiceImpl<ErrorPropertyMapper, ErrorProperty> implements IErrorPropertyService {
    private static final String ERROR_PROPERTY_CACHE_PREFIX = "FTA:ErrorProperty:";
    private static final long CACHE_EXPIRE_TIME = 5;
    private static final TimeUnit CACHE_EXPIRE_TIME_UNIT = TimeUnit.MINUTES;

    @Resource
    private ErrorPropertyMapper errorPropertyMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ArchitectureNodeMapper architectureNodeMapper;
    @Resource
    private ModuleNodeMapper moduleNodeMapper;
    @Resource
    private FunctionNodeMapper functionNodeMapper;

    @Override
    public List<ErrorProperty> getAllErrorProperty(String viewId, String viewType) {
        String key = generateCacheKey(viewId, viewType);
        List<ErrorProperty> errorProperties = (List<ErrorProperty>) redisTemplate.opsForValue().get(key);
        if (errorProperties == null) {
            QueryWrapper<ErrorProperty> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("view_id", viewId)
                    .eq("view_type", viewType);
            errorProperties = this.list(queryWrapper);
            if (errorProperties != null && !errorProperties.isEmpty()) {
                redisTemplate.opsForValue().set(key, errorProperties, CACHE_EXPIRE_TIME, CACHE_EXPIRE_TIME_UNIT);
            }
        }
        return errorProperties;
    }

    @Override
    public List<JSONObject> getAllErrorPropertyInView(String viewId, String viewType) {

        List<JSONObject> errors = new ArrayList<>();
        if (viewType.equals("architecture")) {
//            List<ModuleNode> moduleNodes = architectureNodeMapper.getModuleByArchId(Long.valueOf(viewId));
//            System.out.println(moduleNodes.size());
//            for (ModuleNode moduleNode : moduleNodes) {
//                System.out.println("hhhhhhhhh");
//                System.out.println(JSONObject.from(moduleNode));
//            }
            List<String> moduleIds = architectureNodeMapper.getModuleIdByArchId(Long.valueOf(viewId));
            for (String moduleId : moduleIds) {
                QueryWrapper<ErrorProperty> queryWrapper = new QueryWrapper<>();
                String moduleName = moduleNodeMapper.getModuleNameByModuleId(moduleId);
                queryWrapper.eq("view_id", moduleId)
                        .eq("view_type", "module");
                List<ErrorProperty> moduleErrorProperties = this.list(queryWrapper);

                for (ErrorProperty errorProperty : moduleErrorProperties) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("viewType", "module");
                    jsonObject.put("viewId", moduleId);
                    jsonObject.put("viewName", moduleName);
                    jsonObject.put("errorName", errorProperty.getName());
                    jsonObject.put("probabilityValue", errorProperty.getProbabilityValue());
                    errors.add(jsonObject);
                }

                List<String> functionIds = moduleNodeMapper.getFunctionIdByModuleId(moduleId);
                for (String functionId : functionIds) {
                    String functionName = functionNodeMapper.getFUnctionNameByFunctionId(functionId);
                    QueryWrapper<ErrorProperty> functionErrorQuery = new QueryWrapper();
                    functionErrorQuery.eq("view_id", functionId)
                            .eq("view_type", "function");
                    List<ErrorProperty> functionErrorProperties = this.list(functionErrorQuery);
                    for (ErrorProperty errorProperty : functionErrorProperties) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("viewType", "function");
                        jsonObject.put("viewId", functionId);
                        jsonObject.put("viewName", functionName);
                        jsonObject.put("errorName", errorProperty.getName());
                        jsonObject.put("probabilityValue", errorProperty.getProbabilityValue());
                        errors.add(jsonObject);
                    }
                }

            }
        } else if (viewType.equals("module")) {
            List<String> functionIds = moduleNodeMapper.getFunctionIdByModuleId(viewId);
            for (String functionId : functionIds) {
                String functionName = functionNodeMapper.getFUnctionNameByFunctionId(functionId);
                QueryWrapper<ErrorProperty> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("view_id", functionId)
                        .eq("view_type", "function");
                List<ErrorProperty> functionErrorProperties = this.list(queryWrapper);

                for (ErrorProperty errorProperty : functionErrorProperties) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("viewType", "function");
                    jsonObject.put("viewId", functionId);
                    jsonObject.put("viewName", functionName);
                    jsonObject.put("errorName", errorProperty.getName());
                    jsonObject.put("probabilityValue", errorProperty.getProbabilityValue());
                    errors.add(jsonObject);
                }
            }
        }

        return errors;
    }


    @Override
    public ErrorProperty addErrorProperty(String viewId, String viewType) {
        ErrorProperty errorProperty = new ErrorProperty();
        errorProperty.setName(generateUniqueName(viewId, viewType));
        errorProperty.setViewId(viewId);
        errorProperty.setViewType(ViewType.valueOf(viewType));

        if (this.save(errorProperty)) {
            clearCache(viewId, viewType);
            return errorProperty;
        }
        return null;
    }

    @Override
    public ErrorProperty updateErrorProperty(int id, String name, BigDecimal probabilityValue, String distribution,String portItemId) {
        ErrorProperty errorProperty = this.getById(id);
        if (errorProperty != null) {
            if (name != null) {
                errorProperty.setName(name);
            }
            if (probabilityValue != null) {
                errorProperty.setProbabilityValue(probabilityValue);
            }
            if (distribution != null) {
                errorProperty.setDistribution(Distribution.valueOf(distribution));
            }
            if(portItemId != null){
                errorProperty.setPortItemId(portItemId);
            }
            if (this.updateById(errorProperty)) {
                clearCache(errorProperty.getViewId(), errorProperty.getViewType().name());
                return errorProperty;
            }
        }
        return null;
    }

    @Override
    public void deleteErrorProperty(int id) {
        ErrorProperty errorProperty = this.getById(id);
        if (errorProperty != null) {
            this.removeById(id);
            clearCache(errorProperty.getViewId(), errorProperty.getViewType().name());
        }
    }

    private String generateUniqueName(String viewId, String viewType) {
        String name;
        do {
            // 生成一个随机的 UUID 作为 name
            name = UUID.randomUUID().toString();
        } while (isNameExists(name, viewId, viewType));
        return name;
    }

    @Override
    public boolean newNameExists(int id, String name) {
        return errorPropertyMapper.newNameExists(id, name);
    }

    private boolean isNameExists(String name, String viewId, String viewType) {
        QueryWrapper<ErrorProperty> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name)
                .eq("view_id", viewId)
                .eq("view_type", viewType);
        return this.count(queryWrapper) > 0;
    }

    private String generateCacheKey(String viewId, String viewType) {
        return ERROR_PROPERTY_CACHE_PREFIX + viewType + ":" + viewId;
    }

    private void clearCache(String viewId, String viewType) {
        String key = generateCacheKey(viewId, viewType);
        redisTemplate.delete(key);
    }
}