package org.buaa.buaa_astro_architect.service;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.buaa.buaa_astro_architect.entity.error_model.ErrorProperty;

import java.math.BigDecimal;
import java.util.List;

public interface IErrorPropertyService {

    List<ErrorProperty> getAllErrorProperty(String viewId, String viewType);

    List<JSONObject> getAllErrorPropertyInView(String viewId, String viewType) throws JsonProcessingException;

    ErrorProperty addErrorProperty(String viewId, String viewType);

    boolean newNameExists(int id, String name);

    ErrorProperty updateErrorProperty(int id, String name, BigDecimal probabilityValue, String distribution,String portItemId);

    void deleteErrorProperty(int id);
}
