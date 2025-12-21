package org.example.qyuanmanage.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;
import org.example.qyuanmanage.entity.AuditResult;
import org.example.qyuanmanage.entity.Report;
import org.example.qyuanmanage.service.ReportService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/manage/report")
public class ReportController {

    @Resource
    ReportService reportService;

    @GetMapping("/reportList")
    public Result<Object> listReports(
            @RequestHeader("USER-ID") int user_id,
            @RequestParam("page_num") int page_num,
            @RequestParam("page_size") int page_size,
            @RequestParam(name="status", required = false) Integer status) {
        try {
            IPage<Report> page = reportService.listReports(page_num, page_size,status);
            return Result.ok(page);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/{report_id}")
    public Result<Object> getReport(
            @RequestHeader("USER-ID") int user_id,
            @PathVariable Long report_id) {
        try {
            Report rep = reportService.getReportById(report_id);
            return Result.ok(rep);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/createReport")
    public Result<Object> createReport(
            @RequestParam Long userId,
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            @RequestParam String reportReason,
            @RequestParam(required = false) MultipartFile reportPicture
            ) {
        return Result.ok(reportService.createReport(
                userId, targetType, targetId, reportReason, reportPicture
        ));
    }
}

