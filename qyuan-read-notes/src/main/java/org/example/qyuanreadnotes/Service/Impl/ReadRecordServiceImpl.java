package org.example.qyuanreadnotes.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.qyuanreadnotes.Entity.ReadRecord;
import org.example.qyuanreadnotes.Mapper.ReadRecordMapper;
import org.example.qyuanreadnotes.Service.ReadRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 16:28
 */


@Service
public class ReadRecordServiceImpl extends ServiceImpl<ReadRecordMapper,ReadRecord> implements ReadRecordService {
    @Resource
    ReadRecordMapper readRecordMapper;
    @Override
    public ReadRecord createReadRecord(int userId, Integer paperId, String paperTitle, String firstAuthor, String abstractText) {
        ReadRecord readRecord=new ReadRecord();
        readRecord.setUserId(userId);
        readRecord.setPaperId(paperId);
        readRecord.setPaperTitle(paperTitle);
        readRecord.setAbstractText(abstractText);
        readRecord.setFirstAuthor(firstAuthor);
        readRecord.setUpdatedAt(LocalDateTime.now());
        this.save(readRecord);
        return readRecord;
    }

    @Override
    public ReadRecord getReadRecord(int userId, Integer paperId) {
        ReadRecord readRecord=readRecordMapper.getByUserAndPaper(userId,paperId);
        if(readRecord==null){
            throw new RuntimeException("无阅读记录");
        }
        return readRecord;
    }

    @Override
    public ReadRecord updateReadRecord(Integer recordId, Integer pageNumber, BigDecimal scrollPosition) {
        ReadRecord readRecord=this.getById(recordId);
        if(readRecord==null){
            throw new RuntimeException("无阅读记录");
        }
        readRecord.setPageNumber(pageNumber);
        readRecord.setScrollPosition(scrollPosition);
        this.updateById(readRecord);
        return readRecord;
    }

    @Override
    public Page<ReadRecord> getMyReadRecordByPage(int userId, int pageNum, int pageSize) {
       Page<ReadRecord> readRecords=readRecordMapper.selectPage(new Page<>(pageNum,pageSize),
                new QueryWrapper<ReadRecord>().eq("user_id",userId).orderByDesc("updated_at"));
       return readRecords;
    }
}
