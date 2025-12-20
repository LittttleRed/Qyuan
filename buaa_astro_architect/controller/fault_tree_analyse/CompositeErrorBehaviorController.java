package org.buaa.buaa_astro_architect.controller.fault_tree_analyse;

import com.alibaba.fastjson2.JSONObject;
import com.buaa.core.domain.Result;
import com.buaa.log.annotation.Log;
import com.buaa.log.enums.BusinessType;
import com.buaa.log.enums.OperatorType;
import org.buaa.buaa_astro_architect.entity.error_model.ErrorBehavior;
import org.buaa.buaa_astro_architect.service.IErrorBehaviorService;
import org.buaa.buaa_astro_architect.service.impl.ErrorBehaviorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/fta/compositeErrorBehavior")
public class CompositeErrorBehaviorController {

    private IErrorBehaviorService errorBehaviorService;

    @Autowired
    public void setErrorBehaviorService(ErrorBehaviorServiceImpl errorBehaviorService){
        this.errorBehaviorService = errorBehaviorService;
    }

    @GetMapping("/get/{viewType}/{viewId}")
    @Log(title = "获取视图的组合错误行为", businessType = BusinessType.EXPORT, operatorType = OperatorType.SUPERROOT)
    public Result<Object> getAllErrorBehavior(@PathVariable("viewId") String viewId,
                                              @PathVariable("viewType") String viewType) {
        try {
            String compositeErrorBehavior = errorBehaviorService.getCompositeErrorBehavior(viewId, viewType);
            return Result.ok(compositeErrorBehavior, "查询成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    @PostMapping("/set")
    @Log(title = "修改组合错误行为", businessType = BusinessType.UPDATE, operatorType = OperatorType.SUPERROOT)
    public Result<Object> updateErrorBehavior(@RequestBody JSONObject params) {
        try {
            String viewId = params.getString("viewId");
            String viewType = params.getString("viewType");
            String compositeErrorBehavior = params.getString("compositeErrorBehavior");
            errorBehaviorService.setCompositeErrorBehavior(viewId, viewType, compositeErrorBehavior);
            return Result.ok(null, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }
    }
}
