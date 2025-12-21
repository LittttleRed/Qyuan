package org.example.qyuanmanage.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;
import org.example.qyuanmanage.entity.AuditResult;
import org.example.qyuanmanage.service.AuditService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/manage/audit")
public class AuditController {

    @Resource
    private AuditService auditService;

    @GetMapping("/list")
    public Result<Object> listAudits(@RequestHeader("USER-ID") int user_id,
                                     @RequestParam("page_num") int pageNum,
                                     @RequestParam("page_size") int pageSize) {
        try {
            IPage<AuditResult> page = auditService.listAudits(pageNum, pageSize);
            return Result.ok(page);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/{auditId}")
    public Result<Object> getAudit(@PathVariable("auditId") Long auditId) {
        try {
            AuditResult ar = auditService.getAuditById(auditId);
            return Result.ok(ar);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/report")
    public Result<Object> auditReport(@RequestHeader("USER-ID") Long adminId,
                                      @RequestBody JSONObject body) {
        try {
            Long reportId = body.getLong("reportId");
            Integer result = body.getInteger("result");
            String opinion = body.getString("opinion"); // optional
            AuditResult ar = auditService.auditReport(adminId, reportId, result, opinion);
            return Result.ok(ar);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}

