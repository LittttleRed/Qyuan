package org.example.qyuanreadnotes.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.qyuanreadnotes.Entity.HighLights;
import org.example.qyuanreadnotes.Mapper.HighLightsMapper;
import org.example.qyuanreadnotes.Service.HighLightsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 17:23
 */
@Service
public class HighLightsServiceImpl extends ServiceImpl<HighLightsMapper, HighLights>implements HighLightsService {
    @Resource
    HighLightsMapper highLightsMapper;
    @Override
    public List<HighLights> getHighLights(int userId, Integer paperId) {
         List<HighLights> highLights=highLightsMapper.getByUserAndPaper(userId,paperId);
         return highLights;
    }

    @Override
    public HighLights createHighlight(int userId, Integer paperId, int pageIndex, String highlightedText, String positionData, String color, BigDecimal opacity) {
             HighLights highLights=new HighLights();
             highLights.setUserId(userId);
             highLights.setPaperId(paperId);
             highLights.setPageIndex(pageIndex);
             highLights.setHighlightedText(highlightedText);
             highLights.setPositionData(positionData);
             highLights.setColor(color);
             highLights.setOpacity(opacity);
             this.save(highLights);
             return highLights;
    }
}
