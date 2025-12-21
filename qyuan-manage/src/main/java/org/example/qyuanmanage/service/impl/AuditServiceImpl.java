package org.example.qyuanmanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.qyuanmanage.entity.AuditResult;
import org.example.qyuanmanage.entity.Report;
import org.example.qyuanmanage.mapper.AuditResultMapper;
import org.example.qyuanmanage.mapper.ReportMapper;
import org.example.qyuanmanage.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl
        extends ServiceImpl<AuditResultMapper, AuditResult>
        implements AuditService {

    @Resource
    ReportMapper reportMapper;

    @Override
    public IPage<AuditResult> listAudits(int page, int size) {
        return this.page(new Page<>(page, size));
    }

    @Override
    public AuditResult getAuditById(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional
    public AuditResult auditReport(Long adminId, Long reportId,
                                   Integer result, String opinion) {

        Report rep = reportMapper.selectById(reportId);
        if (rep == null) throw new RuntimeException("Report not found");

        LambdaQueryWrapper<AuditResult> qw = new LambdaQueryWrapper<>();
        qw.eq(AuditResult::getAuditType, 1)
                .eq(AuditResult::getTargetId, reportId);

        if (this.count(qw) > 0) {
            throw new RuntimeException("Report already audited");
        }

        AuditResult ar = new AuditResult();
        ar.setAdminId(adminId);
        ar.setAuditType(1); // 举报审核
        ar.setTargetId(reportId);
        ar.setAuditResult(result);
        ar.setAuditOpinion(opinion);
        ar.setAuditTime(LocalDateTime.now());
        this.save(ar);

        // 同步更新举报状态
        rep.setStatus(result == 2 ? 2 : 1);
        reportMapper.updateById(rep);

        return ar;
    }
}
