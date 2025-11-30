package org.example.qyuanreadnotes.Service;

import java.math.BigDecimal;

public interface HighLightsService {
    Object getHighLights(int userId, Integer paperId);

    Object createHighlight(int userId, Integer paperId, int pageIndex, String highlightedText, String positionData, String color, BigDecimal opacity);
}
