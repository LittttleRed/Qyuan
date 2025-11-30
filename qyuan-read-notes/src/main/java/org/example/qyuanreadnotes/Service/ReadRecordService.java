package org.example.qyuanreadnotes.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.qyuanreadnotes.Entity.ReadRecord;

import java.math.BigDecimal;

public interface ReadRecordService {
    ReadRecord createReadRecord(int userId, Integer paperId, String paperTitle, String firstAuthor, String abstractText);

    ReadRecord getReadRecord(int userId, Integer paperId);

    ReadRecord updateReadRecord(Integer recordId, Integer pageNumber, BigDecimal scrollPosition);

    Page<ReadRecord> getMyReadRecordByPage(int userId, int pageNum, int pageSize);
}
