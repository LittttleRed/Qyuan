package org.example.qyuanmanage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.qyuanmanage.entity.Report;
import org.springframework.web.multipart.MultipartFile;

public interface ReportService {

    IPage<Report> listReports(int page, int size);

    Report getReportById(Long id);

    Report createReport(Long userId, Integer targetType, Long targetId,
                        String reportReason, MultipartFile reportPicture);
}
