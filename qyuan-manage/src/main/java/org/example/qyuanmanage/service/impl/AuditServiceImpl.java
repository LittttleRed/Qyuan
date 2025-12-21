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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditServiceImpl
        extends ServiceImpl<AuditResultMapper, AuditResult>
        implements AuditService {

    @Resource
    ReportMapper reportMapper;

    @Resource
    KafkaTemplate<String,Object> kafkaTemplate;
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

        Map<String,String> message = new HashMap<>();
        String content=String.format("您举报的类型为 %s ,原因为 %s 的对象已处理,结果为%s",
                switch (rep.getTargetType()){
                    case 1 -> "论文";
                    case 2 -> "用户";
                    default -> "未知";
                },rep.getReportReason(),ar.getAuditOpinion());
        message.put("title","举报结果处理结果通知");
        message.put("content", content);
        message.put("userId",String.valueOf(rep.getUserId()));
        kafkaTemplate.send("message-audit-topic", message);

        // 同步更新举报状态
        rep.setStatus(result == 2 ? 2 : 1);
        reportMapper.updateById(rep);

        return ar;
    }
}
