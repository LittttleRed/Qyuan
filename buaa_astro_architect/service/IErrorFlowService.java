package org.buaa.buaa_astro_architect.service;

import com.alibaba.fastjson2.JSONObject;
import com.buaa.core.domain.Result;
import org.buaa.buaa_astro_architect.entity.error_model.neo4j.ErrorFlow;

import java.util.List;

public interface IErrorFlowService {
    ErrorFlow createErrorFlow(String viewId,String viewType);

    void deleteErrorFlow(Long id);

    ErrorFlow updateErrorFlow(JSONObject  params);

    boolean newErrorFlow(Long id,String newName, String viewId, String viewType);

    List<ErrorFlow> getAllErrorFlow(String viewId, String viewType);

    Result<Object> genFTA(int errorPropertyId);
}
