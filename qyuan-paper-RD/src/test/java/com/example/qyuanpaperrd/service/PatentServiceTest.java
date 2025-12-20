package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.entity.Patent;
import com.example.qyuanpaperrd.mapper.PatentMapper;
import com.example.qyuanpaperrd.service.impl.PatentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatentService 测试")
class PatentServiceTest {

    @Mock
    private PatentMapper patentMapper;

    @InjectMocks
    private PatentServiceImpl patentService;

    private Patent testPatent;
    private PatentDTO testPatentDTO;

    @BeforeEach
    void setUp() {
        testPatent = new Patent();
        testPatent.setPatentNumber("US20230012345A");
        testPatent.setPatentName("Test Patent");
        testPatent.setAbstractText("Test abstract");
        testPatent.setInventor("John Doe");
        testPatent.setAssignee("Test Company");
        testPatent.setCountry("US");
        testPatent.setApplicationDate(LocalDateTime.of(2023, 1, 15, 0, 0));
        testPatent.setAuthorizationDate(LocalDateTime.of(2023, 5, 20, 0, 0));
        testPatent.setCitationCount(10);

        testPatentDTO = new PatentDTO();
        testPatentDTO.setPatentNumber("US20230012345A");
        testPatentDTO.setPatentName("Test Patent");
        testPatentDTO.setAbstractText("Test abstract");
        testPatentDTO.setInventor("John Doe");
        testPatentDTO.setAssignee("Test Company");
        testPatentDTO.setCountry("US");
        testPatentDTO.setApplicationDate(LocalDateTime.of(2023, 1, 15, 0, 0));
        testPatentDTO.setAuthorizationDate(LocalDateTime.of(2023, 5, 20, 0, 0));
        testPatentDTO.setCitationCount(10);
    }

    @Test
    @DisplayName("根据专利申请号获取专利详情 - 成功")
    void getPatentByNumber_Success() {
        when(patentMapper.selectById("US20230012345A")).thenReturn(testPatent);

        PatentDTO result = patentService.getPatentByNumber("US20230012345A", null);

        assertNotNull(result);
        assertEquals("US20230012345A", result.getPatentNumber());
        assertEquals("Test Patent", result.getPatentName());

        verify(patentMapper).selectById("US20230012345A");
    }

    @Test
    @DisplayName("根据专利申请号获取专利详情 - 专利不存在")
    void getPatentByNumber_NotFound() {
        when(patentMapper.selectById("INVALID123")).thenReturn(null);

        PatentDTO result = patentService.getPatentByNumber("INVALID123", null);

        assertNull(result);
        verify(patentMapper).selectById("INVALID123");
    }

    @Test
    @DisplayName("搜索专利 - 关键词搜索")
    void searchPatents_ByKeyword() {
        when(patentMapper.searchPatents("test")).thenReturn(Arrays.asList(testPatent));

        PageResult<PatentDTO> result = patentService.searchPatents("test", null, null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("Test Patent", result.getRecords().get(0).getPatentName());

        verify(patentMapper).searchPatents("test");
    }

    @Test
    @DisplayName("搜索专利 - 按发明人搜索")
    void searchPatents_ByInventor() {
        when(patentMapper.searchByInventor("John Doe")).thenReturn(Arrays.asList(testPatent));

        PageResult<PatentDTO> result = patentService.searchPatents(null, "John Doe", null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("John Doe", result.getRecords().get(0).getInventor());

        verify(patentMapper).searchByInventor("John Doe");
    }

    @Test
    @DisplayName("搜索专利 - 按专利权人搜索")
    void searchPatents_ByAssignee() {
        when(patentMapper.searchByAssignee("Test Company")).thenReturn(Arrays.asList(testPatent));

        PageResult<PatentDTO> result = patentService.searchPatents(null, null, "Test Company", null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("Test Company", result.getRecords().get(0).getAssignee());

        verify(patentMapper).searchByAssignee("Test Company");
    }

    @Test
    @DisplayName("搜索专利 - 按国家搜索")
    void searchPatents_ByCountry() {
        when(patentMapper.searchByCountry("US")).thenReturn(Arrays.asList(testPatent));

        PageResult<PatentDTO> result = patentService.searchPatents(null, null, null, "US", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("US", result.getRecords().get(0).getCountry());

        verify(patentMapper).searchByCountry("US");
    }

    @Test
    @DisplayName("搜索专利 - 默认搜索（无参数）")
    void searchPatents_Default() {
        when(patentMapper.selectList(any())).thenReturn(Arrays.asList(testPatent));

        PageResult<PatentDTO> result = patentService.searchPatents(null, null, null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());

        verify(patentMapper).selectList(any());
    }

    @Test
    @DisplayName("搜索专利 - 分页超出范围")
    void searchPatents_PageOutOfRange() {
        when(patentMapper.searchPatents("test")).thenReturn(Arrays.asList(testPatent));

        PageResult<PatentDTO> result = patentService.searchPatents("test", null, null, null, 2, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getRecords().size());

        verify(patentMapper).searchPatents("test");
    }

    @Test
    @DisplayName("获取热门专利")
    void getHotPatents() {
        Patent highCitationPatent = new Patent();
        highCitationPatent.setPatentNumber("HOT001");
        highCitationPatent.setPatentName("Hot Patent");
        highCitationPatent.setCitationCount(50);

        when(patentMapper.getHotPatents(5)).thenReturn(Arrays.asList(highCitationPatent, testPatent));

        List<PatentDTO> result = patentService.getHotPatents(5);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getCitationCount() == 50));

        verify(patentMapper).getHotPatents(5);
    }

    @Test
    @DisplayName("获取最新专利")
    void getLatestPatents() {
        Patent latestPatent = new Patent();
        latestPatent.setPatentNumber("NEW001");
        latestPatent.setPatentName("New Patent");
        latestPatent.setApplicationDate(LocalDateTime.of(2023, 3, 1, 0, 0));

        when(patentMapper.getLatestPatents(5)).thenReturn(Arrays.asList(latestPatent, testPatent));

        List<PatentDTO> result = patentService.getLatestPatents(5);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(patentMapper).getLatestPatents(5);
    }
}
