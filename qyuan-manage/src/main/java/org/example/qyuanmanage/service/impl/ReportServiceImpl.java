package org.example.qyuanmanage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.qyuanmanage.entity.Report;
import org.example.qyuanmanage.mapper.ReportMapper;
import org.example.qyuanmanage.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Override
    public IPage<Report> listReports(int page, int size) {
        return this.page(new Page<>(page, size));
    }

    @Override
    public Report getReportById(Long id) {
        return this.getById(id);
    }

    @Override
    public Report createReport(Long userId, Integer targetType, Long targetId,
                               String reportReason, String reportUrl) {
        Report r = new Report();
        r.setUserId(userId);
        r.setTargetType(targetType);
        r.setTargetId(targetId);
        r.setReportReason(reportReason);
        r.setReportUrl(reportUrl);
        r.setSubmitTime(LocalDateTime.now());
        r.setStatus(0);
        this.save(r);
        return r;
    }
}
