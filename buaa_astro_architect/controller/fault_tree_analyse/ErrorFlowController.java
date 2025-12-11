package org.buaa.buaa_astro_architect.controller.fault_tree_analyse;

import com.alibaba.fastjson2.JSONObject;
import com.buaa.core.domain.Result;
import com.buaa.log.annotation.Log;
import com.buaa.log.enums.BusinessType;
import com.buaa.log.enums.OperatorType;
import org.buaa.buaa_astro_architect.entity.error_model.neo4j.ErrorFlow;
import org.buaa.buaa_astro_architect.service.IErrorFlowService;
import org.buaa.buaa_astro_architect.service.impl.ErrorFlowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @author: 29177
 * @time: 2025/10/22 21:31
 */
@RestController
@RequestMapping("/fta/flow")
public class ErrorFlowController {
    private IErrorFlowService errorFlowService;

    @Autowired
    public void setErrorFlowService(ErrorFlowServiceImpl errorFlowService) {
        this.errorFlowService = errorFlowService;
    }

    /*
        param:
        viewType
        viewId
        body:
     */
    @PostMapping("/createErrorFlow/{viewType}/{viewId}")
    @Log(title = "创建错误流", businessType = BusinessType.INSERT, operatorType = OperatorType.SUPERROOT)
    public Result<Object> createPathErrorFlow(@PathVariable("viewId") String viewId,
                                              @PathVariable("viewType") String viewType){
            try{
                ErrorFlow errorFlow = errorFlowService.createErrorFlow(viewId, viewType);
                return Result.ok(errorFlow, "创建成功");
            }catch (Exception e){
                return Result.fail(e.getMessage());
            }
    }
    @PostMapping("/deleteErrorFlow")
    @Log(title = "删除错误流", businessType = BusinessType.DELETE, operatorType = OperatorType.SUPERROOT)
    public Result<Object> deleteErrorFlow(@RequestBody JSONObject params){
        try{
            Long id = params.getLong("id");
            errorFlowService.deleteErrorFlow(id);
            return Result.ok(null, "删除成功");
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("updateErrorFlow/{viewType}/{viewId}")
    @Log(title = "修改错误流", businessType = BusinessType.UPDATE, operatorType = OperatorType.SUPERROOT)
    public Result<Object> updateErrorFlow(@PathVariable("viewId") String viewId,
                                          @PathVariable("viewType") String viewType,
                                          @RequestBody JSONObject params){
        try{
            Long id = params.getLong("id");
            String name = params.getString("name");
            if(errorFlowService.newErrorFlow(id,name,viewId,viewType)) {
                ErrorFlow errorFlow = errorFlowService.updateErrorFlow(params);
                return Result.ok(errorFlow, "修改成功");
            }else{
                return Result.fail("命名重复");
            }
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
    @GetMapping("/getAllErrorFlow/{viewType}/{viewId}")
    @Log(title = "获取视图中所有错误流", businessType = BusinessType.EXPORT, operatorType = OperatorType.SUPERROOT)
    public Result<Object> getAllErrorFlow(@PathVariable("viewId") String viewId,
                                         @PathVariable("viewType") String viewType){
        try{
            List<ErrorFlow> errorFlowList = errorFlowService.getAllErrorFlow(viewId,viewType);
            return Result.ok(errorFlowList, "查询成功");
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
    @PostMapping("/genFTA/{errorPropertyId}")
    @Log(title = "生成FTA", businessType = BusinessType.EXPORT, operatorType = OperatorType.SUPERROOT)
    public Result<Object> genFTA(@PathVariable("errorPropertyId") int errorPropertyId){
        try{
            return errorFlowService.genFTA(errorPropertyId);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

}
