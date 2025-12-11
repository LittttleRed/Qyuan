package org.buaa.buaa_astro_architect.openfeign;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "astro-requirement")
@Component
public interface RequirementFeign {
    /**
     * 导出需求模型为json字符串
     * 为架构模块的服务提供需求模型的信息
     *
     * @param modelId 要导出的需求模型的id
     * @return 导出后的需求模型文件的json字符串
     */
    @GetMapping("/lib/export/model/jsonObject")
    JSONObject exportModelToJsonString(@RequestParam("modelId") Long modelId);
}
