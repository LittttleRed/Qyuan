package org.buaa.buaa_astro_architect.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.buaa.core.domain.Result;
import com.buaa.core.graph_entity.port.FunctionPortItem;
import com.buaa.core.graph_entity.port.ModulePortItem;
import org.buaa.buaa_astro_architect.entity.error_model.ErrorProperty;
import org.buaa.buaa_astro_architect.entity.error_model.ViewType;
import org.buaa.buaa_astro_architect.entity.error_model.neo4j.ErrorFlow;
import jakarta.annotation.Resource;
import org.buaa.buaa_astro_architect.mapper.core_entity.ErrorFlowMapper;
import org.buaa.buaa_astro_architect.mapper.core_entity.FunctionPortItemMapper;
import org.buaa.buaa_astro_architect.mapper.core_entity.ModulePortItemMapper;
import org.buaa.buaa_astro_architect.mapper.error_model.ErrorPropertyMapper;
import org.buaa.buaa_astro_architect.mapper.neo4j.FunctionNodeMapper;
import org.buaa.buaa_astro_architect.mapper.neo4j.ModuleNodeMapper;
import org.buaa.buaa_astro_architect.service.IErrorFlowService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: 29177
 * @time: 2025/10/22 23:47
 */

@Service
public class ErrorFlowServiceImpl implements IErrorFlowService {

    private static final String ERROR_PROPAGATION_CACHE_PREFIX = "FTA:ErrorPropagation:";

    private static final long CACHE_EXPIRE_TIME = 5;

    private static final TimeUnit CACHE_EXPIRE_TIME_UNIT = TimeUnit.MINUTES;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ErrorFlowMapper errorFlowMapper;

    @Resource
    private ErrorPropertyMapper errorPropertyMapper;

    @Resource
    private FunctionPortItemMapper functionPortItemMapper;
    @Resource
    private FunctionNodeMapper functionNodeMapper;
    @Resource
    private ModulePortItemMapper modulePortItemMapper;
    @Resource
    private ModuleNodeMapper moduleNodeMapper;

    @Override
    public void deleteErrorFlow(Long id) {
        ErrorFlow errorFlow = errorFlowMapper.getErrorFlowById(id);
        if(errorFlow!=null) {
            errorFlowMapper.deleteErrorFlowById(id);
        }
    }

    @Override
    public ErrorFlow updateErrorFlow(JSONObject  params) {
        Long id = params.getLong("id");
        String newName = params.getString("name");

        //这两个是errorProperty的id
        Integer outId = params.getInteger("outErrorId");
        Integer inId = params.getInteger("inErrorId");
        String inPortId = params.getString("inPortId");
        String outPortId = params.getString("outPortId");

        ErrorFlow errorFlow = errorFlowMapper.getErrorFlowById(id);
        if(errorFlow!=null) {
            if(newName!=null) {
                errorFlow.setName(newName);
            }
            if(outId!=null&&outPortId!=null){
                errorFlow.setOutErrorId(outId);
                errorFlowMapper.clearOutErrorFlowRelationship(id);
                errorFlowMapper.createOutErrorToFlowRelationship(outPortId,id);
            }
            if(inId!=null){
                errorFlow.setInErrorId(inId);
                errorFlowMapper.clearInErrorFlowRelationship(id);
                errorFlowMapper.createInErrorToFlowRelationship(inPortId,id);
            }

            errorFlowMapper.save(errorFlow);
            clearCache(errorFlow.getViewId(),errorFlow.getViewType().toString());
        }
        return errorFlow;
    }



    @Override
    public ErrorFlow createErrorFlow(String viewId,String viewType) {
        ErrorFlow errorFlow = new ErrorFlow();
        errorFlow.setName(generateUniqueName());
        errorFlow.setViewId(viewId);
        errorFlow.setViewType(ViewType.valueOf(viewType));
        errorFlowMapper.save(errorFlow);
        return errorFlow;
    }
    @Override
    public List<ErrorFlow> getAllErrorFlow(String viewId, String viewType) {
        return errorFlowMapper.getErrorFlowByViewId(viewId,viewType);
    }

    @Override
    public Result<Object> genFTA(int errorPropertyId) {
        //初始化json对象
        JSONObject result = new JSONObject();
        result.put("use_type",null);
        result.put("use_behavior",null);
        result.put("component_error_behavior",null);
        result.put("properties",new HashSet<>());
        result.put("composite_error_behavior",new ArrayList<>());

        ErrorProperty errorProperty = errorPropertyMapper.getById(errorPropertyId);
        if(errorProperty==null){
            return Result.fail("errorPropertyId不存在");
        }
        ((HashSet) result.get("properties")).add(errorProperty2json(errorProperty,"errorState"));
        HashSet<JSONObject> resources = ((HashSet) result.get("properties"));
        JSONObject source = new JSONObject();
        source.put("state",errorProperty.getName());
        source.put("logic",new JSONObject());
        ((ArrayList) result.get("composite_error_behavior")).add(source);
        //区分模块和函数
        FunctionPortItem functionPortItem = functionPortItemMapper.findFunctionPortItemById(errorProperty.getPortItemId());
        if(functionPortItem==null){
            ModulePortItem modulePortItem = modulePortItemMapper.findModulePortItemById(errorProperty.getPortItemId());
            if(modulePortItem.getGroup().equals("out")) {
                handleModuleOutError(source, errorProperty, resources,modulePortItem);
            }
        }else{
            if(functionPortItem.getGroup().equals("out")){
                handleFunctionOutError((JSONObject) source.get("logic"),errorProperty,resources);
            }
        }
        System.out.println(result.toJSONString(JSONWriter.Feature.WriteMapNullValue));
        return Result.ok(result.toJSONString(JSONWriter.Feature.WriteMapNullValue),"生成成功");
    }

    private void handleModuleOutError(JSONObject source, ErrorProperty errorProperty,HashSet<JSONObject> resources,ModulePortItem modulePortItem){
            int moduleFlowCount =handleFunctionOutError((JSONObject) source.get("logic"),errorProperty,resources);
            if(modulePortItemMapper.hasIncomingEdge(modulePortItem.getOwnId())){
                FunctionPortItem outPortItem = functionPortItemMapper.findModuleOutputPortItem(modulePortItem.getOwnId());
                List<ErrorProperty> errorProperties = errorPropertyMapper.getAllByPortItemIdAndName(outPortItem.getId(),errorProperty.getName());
                for(ErrorProperty errorProperty1:errorProperties){
                        if(moduleFlowCount>0){
                            JSONObject right = (JSONObject) source.get("logic");
                            JSONObject left = new JSONObject();
                            JSONObject newLogic = new JSONObject();
                            source.remove("logic");
                            source.put("logic",newLogic);
                            newLogic.put("type","LogicalOr");
                            newLogic.put("right", right);
                            newLogic.put("left", left);
                            handleFunctionOutError(left, errorProperty1, resources);
                        }else{
                          handleFunctionOutError((JSONObject) source.get("logic"), errorProperty1, resources);
                        }
                        break;
                }
            }
    }
    private int handleFunctionOutError(JSONObject logic, ErrorProperty errorProperty, HashSet<JSONObject> resources){
            List<ErrorFlow> outErrorFlows = errorFlowMapper.getAllOutErrorFlowByPortId(errorProperty.getPortItemId());
            JSONObject writeIn=logic;
            for(int i=0;i<outErrorFlows.size();i++){
                if(i+1<outErrorFlows.size()){
                    writeIn.put("type","LogicalOr");
                    JSONObject left = new JSONObject();
                    JSONObject right = new JSONObject();
                    writeIn.put("left",left);
                    writeIn.put("right", right);
                    ErrorProperty inErrorProperty = errorPropertyMapper.getById(outErrorFlows.get(i).getInErrorId());
                    putErrorProperty(right,inErrorProperty,resources);
                    handleErrorBetweenFunction(right,inErrorProperty,resources);
                    writeIn=left;
                }else{
                    ErrorProperty inErrorProperty = errorPropertyMapper.getById(outErrorFlows.get(i).getInErrorId());
                    putErrorProperty(writeIn,inErrorProperty,resources);
                    //在这里处理跨函数的错误流
                    handleErrorBetweenFunction(writeIn,inErrorProperty,resources);
                }
            }
            return outErrorFlows.size();
    }

    private void handleErrorBetweenFunction(JSONObject logic,ErrorProperty inErrorProperty,HashSet<JSONObject> resources){
        //在这里处理跨函数的错误流
        //如果这个error对应的port是functionPort,则继续向下解析,如果是module,则终止,跨模块的调用暂时不考虑
        //先探索一下这个error对应的port有没有inedge,如果没有就在这里终止,如果有的话,就获取对应边的inport
        //如果portId==inportId的errorProperty存在,获取所有名字和inError一致的property,一致则能传播(加一个logic,里面记录这个property),不一致就终止不加logic
        //加了logic后添加了property,若这个inport是modulePort,就不再加了,如果是一个functionPort,再在这个logic中添加一个logic,在这个logic中处理新的handleOutError
        String portId = inErrorProperty.getPortItemId();
        FunctionPortItem functionPortItem = functionPortItemMapper.findFunctionPortItemById(portId);
        if(functionPortItem!=null){
            Long id = functionPortItem.getOwnId();
            if(functionPortItemMapper.hasIncomingEdge(id)){
                FunctionPortItem outputPortItem = functionPortItemMapper.findOutputPortItem(id);
                if(outputPortItem!=null){
                    String portItemId = outputPortItem.getId();
                    List<ErrorProperty> errorProperties=errorPropertyMapper.getAllByPortItemIdAndName(portItemId,inErrorProperty.getName());
                    for(ErrorProperty errorProperty:errorProperties){
                            JSONObject newLogic=new JSONObject();
                            logic.put("logic",newLogic);
                            putErrorProperty(newLogic,errorProperty,resources);
                            JSONObject nextLogic=new JSONObject();
                            handleFunctionOutError(nextLogic,errorProperty,resources);
                            if(!nextLogic.isEmpty()) {
                                newLogic.put("logic", nextLogic);
                            }
                            break;
                    }
                }else{
                    ModulePortItem outputModulePortItem = modulePortItemMapper.findOutputModulePortItem(id);
                    String portItemId = outputModulePortItem.getId();
                    List<ErrorProperty> errorProperties=errorPropertyMapper.getAllByPortItemIdAndName(portItemId,inErrorProperty.getName());
                    for (ErrorProperty errorProperty:errorProperties){
                            JSONObject newLogic=new JSONObject();
                            logic.put("logic",newLogic);
                            putErrorProperty(newLogic,errorProperty,resources);
                            break;
                    }
                }
            }
        }
    }


    private JSONObject errorProperty2json(ErrorProperty errorProperty,String type) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value",errorProperty.getProbabilityValue()!=null?errorProperty.getProbabilityValue():0);
        jsonObject.put("distribution",errorProperty.getDistribution()!=null?errorProperty.getDistribution():"None");
        JSONObject error = new JSONObject();
        if(type.equals("propagationError")){
            error.put("flowName",getErrorPropertyParentName(errorProperty));
        }
        error.put("errorName",errorProperty.getName());
        error.put("type",type);
        jsonObject.put("error", error);
        return jsonObject;
    }
    private void putErrorProperty(JSONObject logic,ErrorProperty errorProperty,HashSet<JSONObject>  resources){
        resources.add(errorProperty2json(errorProperty,"propagationError"));
        logic.put("type","ErrorState");
        logic.put("component",getErrorPropertyParentName(errorProperty));
        logic.put("stateName",errorProperty.getName());
    }
   private String getErrorPropertyParentName(ErrorProperty errorProperty) {
         String name=null;
            if(errorProperty.getViewType().equals(ViewType.function)){
                name=functionNodeMapper.getFUnctionNameByFunctionId(errorProperty.getViewId());
            }else if(errorProperty.getViewType().equals(ViewType.module)){
                name=moduleNodeMapper.getModuleNameByModuleId(errorProperty.getViewId());
            }
            return name;
    }
    private String generateUniqueName() {
        String name;
        // 生成一个随机的 UUID 作为 name
        name = UUID.randomUUID().toString();
        return name;
    }
    private String generateCacheKey(String viewId, String viewType) {
        return ERROR_PROPAGATION_CACHE_PREFIX + viewType + ":" + viewId;
    }
    private void clearCache(String viewId, String viewType) {
        String key = generateCacheKey(viewId, viewType);
        redisTemplate.delete(key);
    }


    @Override
    public boolean newErrorFlow(Long id,String newName, String viewId, String viewType) {
        List<ErrorFlow> errorFlows=errorFlowMapper.getErrorFlowByViewId(viewId,viewType);
        return errorFlows.stream().filter(errorFlow -> !errorFlow.getOwnId().equals(id))
                .map(ErrorFlow::getName)
                .noneMatch(name -> name.equals(newName));
    }
}
