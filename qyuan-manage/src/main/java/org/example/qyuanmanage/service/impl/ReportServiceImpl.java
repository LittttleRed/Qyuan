package org.example.qyuanmanage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.qyuanmanage.entity.Report;
import org.example.qyuanmanage.mapper.ReportMapper;
import org.example.qyuanmanage.service.MinioService;
import org.example.qyuanmanage.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Resource
    private MinioService minioService;

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
                               String reportReason, MultipartFile reportPicture) {
        Report r = new Report();
        r.setUserId(userId);
        r.setTargetType(targetType);
        r.setTargetId(targetId);
        r.setReportReason(reportReason);

        String pictureUrl = null;
        MultipartFile claimPicture = reportPicture;
        if (claimPicture != null && !claimPicture.isEmpty()) {
            try {
                // 上传到 MinIO
                String folder = "claims"; // 按用户ID分文件夹存储
                String objectKey = minioService.uploadFile(claimPicture, folder);
                pictureUrl = minioService.getPublicUrl(objectKey);
            } catch (Exception e) {
                log.error("上传认领图片失败", e);
                throw new RuntimeException("上传认领图片失败: " + e.getMessage());
            }
        }
        r.setReportUrl(pictureUrl);
        r.setSubmitTime(LocalDateTime.now());
        r.setStatus(0);
        this.save(r);
        return r;
    }
}
