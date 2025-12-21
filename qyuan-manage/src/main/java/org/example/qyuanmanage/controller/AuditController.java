package org.example.qyuanmanage.controller;

import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;
import org.example.qyuanmanage.service.AuditService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Resource
    private AuditService auditService;

    @GetMapping("/auditList")
    public Result<Object> listAudits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return Result.ok(auditService.listAudits(page, size));
    }

    @GetMapping("/getAudit/{id}")
    public Result<Object> getAudit(@PathVariable Long id) {
        return Result.ok(auditService.getAuditById(id));
    }

    @PostMapping("/auditReport")
    public Result<Object> auditReport(
            @RequestHeader("USER-ID") int user_id,
            @RequestParam Long reportId,
            @RequestParam Integer result,
            @RequestParam(required = false) String opinion) {

        return Result.ok(
                auditService.auditReport(adminId, reportId, result, opinion)
        );
    }
}

// token: get userid
//

