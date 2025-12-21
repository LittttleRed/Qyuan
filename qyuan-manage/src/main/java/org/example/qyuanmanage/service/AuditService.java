package org.example.qyuanmanage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.qyuanmanage.entity.AuditResult;

public interface AuditService {

    IPage<AuditResult> listAudits(int page, int size);

    AuditResult getAuditById(Long id);

    AuditResult auditReport(Long adminId, Long reportId, Integer result, String opinion);
}
