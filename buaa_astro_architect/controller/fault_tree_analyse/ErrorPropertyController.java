package org.buaa.buaa_astro_architect.controller.fault_tree_analyse;

import com.alibaba.fastjson2.JSONObject;
import com.buaa.core.domain.Result;
import com.buaa.log.annotation.Log;
import com.buaa.log.enums.BusinessType;
import com.buaa.log.enums.OperatorType;
import org.buaa.buaa_astro_architect.entity.error_model.ErrorProperty;
import org.buaa.buaa_astro_architect.service.IErrorPropertyService;
import org.buaa.buaa_astro_architect.service.impl.ErrorPropertyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/fta")
public class ErrorPropertyController {

    private IErrorPropertyService errorPropertyService;

    @Autowired
    public void setErrorPropertyService(ErrorPropertyServiceImpl errorPropertyService){
        this.errorPropertyService = errorPropertyService;
    }
    @GetMapping("/getAllErrorProperty/{viewType}/{viewId}")
    @Log(title = "获取视图内全部错误属性", businessType = BusinessType.EXPORT, operatorType = OperatorType.SUPERROOT)
    public Result<Object> getAllErrorProperty(@PathVariable("viewId") String viewId,
                                              @PathVariable("viewType") String viewType) {
        try {
            List<ErrorProperty> errorPropertyList = errorPropertyService.getAllErrorProperty(viewId, viewType);
            return Result.ok(errorPropertyList, "查询成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/addErrorProperty")
    @Log(title = "新增错误属性", businessType = BusinessType.INSERT, operatorType = OperatorType.SUPERROOT)
    public Result<Object> addErrorProperty(@RequestBody JSONObject params) {
        try {
            String viewId = params.getString("viewId");
            String viewType = params.getString("viewType");
            ErrorProperty errorProperty = errorPropertyService.addErrorProperty(viewId, viewType);
            return Result.ok(errorProperty, "新增成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/updateErrorProperty")
    @Log(title = "修改错误属性", businessType = BusinessType.UPDATE, operatorType = OperatorType.SUPERROOT)
    public Result<Object> updateErrorProperty(@RequestBody JSONObject params) {
        try {
            int id = params.getIntValue("id");
            String name = params.getString("name");
            BigDecimal probabilityValue = params.getBigDecimal("probabilityValue");
            String distribution = params.getString("distribution");
            String portItemId = params.getString("portId");
            if(errorPropertyService.newNameExists(id, name)){
                System.out.println("hhhhh");
                return Result.fail("命名重复");
            }
            ErrorProperty errorProperty = errorPropertyService.updateErrorProperty(id, name, probabilityValue, distribution, portItemId);
            return Result.ok(errorProperty, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/deleteErrorProperty")
    @Log(title = "删除错误属性", businessType = BusinessType.DELETE, operatorType = OperatorType.SUPERROOT)
    public Result<Object> deleteErrorProperty(@RequestBody JSONObject params) {
        try {
            int id = params.getIntValue("id");
            errorPropertyService.deleteErrorProperty(id);
            return Result.ok( null, "删除成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/getAllErrorPropertyInView/{viewType}/{viewId}")
    @Log(title = "获取视图及其子视图内全部错误属性", businessType = BusinessType.EXPORT, operatorType = OperatorType.SUPERROOT)
    public Result<Object> getAllErrorPropertyInView(@PathVariable("viewId") String viewId,
                                                    @PathVariable("viewType") String viewType) {
        try {
            List<JSONObject> errorPropertyList = errorPropertyService.getAllErrorPropertyInView(viewId, viewType);
            return Result.ok(errorPropertyList, "查询成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
