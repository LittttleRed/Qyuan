package org.example.qyuanmanage.controller;

import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;
import org.example.qyuanmanage.service.ReportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    ReportService reportService;

    @GetMapping("/reportList")
    public Result<Object> listReports(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int size) {

        return Result.ok(reportService.listReports(page, size));
    }

    @GetMapping("/getReport/{id}")
    public Result<Object> getReport(@PathVariable Long id) {
        return Result.ok(reportService.getReportById(id));
    }

    @PostMapping("/createReport")
    public Result<Object> createReport(
            @RequestParam Long userId,
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            @RequestParam String reportReason,
            @RequestParam(required = false) String reportUrl) {

        return Result.ok(reportService.createReport(
                userId, targetType, targetId, reportReason, reportUrl
        ));
    }
}

