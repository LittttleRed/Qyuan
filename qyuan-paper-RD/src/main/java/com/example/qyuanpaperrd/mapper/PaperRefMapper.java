package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qyuanpaperrd.entity.PaperRef;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 论文引用Mapper接口
 */
@Mapper
public interface PaperRefMapper extends BaseMapper<PaperRef> {
    
    /**
     * 根据论文ID删除引用关系
     */
    int deleteByPaperId(Long paperId);
    
    /**
     * 根据被引用论文ID查询引用关系
     */
    List<PaperRef> selectByCitedPaperId(Long citedPaperId);
    
    /**
     * 根据引用论文ID查询引用关系
     */
    List<PaperRef> selectByCitingPaperId(Long citingPaperId);
}
