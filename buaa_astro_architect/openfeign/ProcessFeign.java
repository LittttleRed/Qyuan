package org.buaa.buaa_astro_architect.openfeign;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient("astro-process")
@Component
public interface ProcessFeign {
    @PostMapping("/process/saveArchitectureId2ProcessId")
    JSONObject saveArchitectureId2ProcessId(@RequestBody JSONObject params);

    @DeleteMapping("/process/deleteArchitectureId2ProcessId/{architectureId}")
    JSONObject deleteArchitectureId2ProcessId(@PathVariable(value = "architectureId") String architectureId);

    @GetMapping("/process/getProcessGraphId/{architectureId}")
    JSONObject getProcessGraphId(@PathVariable(value = "architectureId") String architectureId);
}
