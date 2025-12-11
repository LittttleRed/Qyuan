package org.buaa.buaa_astro_architect.openfeign;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "astro-auth")
@Component
public interface AuthFeign {
    @GetMapping("/projects/getRequirementModelId")
    JSONObject getRequirementModelIdByProjectId(@RequestParam("pid") Long pid);
}
