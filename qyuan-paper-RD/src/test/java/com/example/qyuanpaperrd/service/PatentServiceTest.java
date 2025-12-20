package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.entity.Patent;
import com.example.qyuanpaperrd.entity.PatentClaim;
import com.example.qyuanpaperrd.mapper.PatentClaimMapper;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatentService 测试")
class PatentServiceTest {

    @Mock
    private PatentMapper patentMapper;

    @Mock
    private PatentClaimMapper patentClaimMapper;

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
        testPatentDTO.setIsClaimed(false);
        testPatentDTO.setClaimStatus("未认领");
    }

    @Test
    @DisplayName("根据专利申请号获取专利详情 - 成功")
    void getPatentByNumber_Success() {
        when(patentMapper.selectById("US20230012345A")).thenReturn(testPatent);

        PatentDTO result = patentService.getPatentByNumber("US20230012345A", null);

        assertNotNull(result);
        assertEquals("US20230012345A", result.getPatentNumber());
        assertEquals("Test Patent", result.getPatentName());
        assertFalse(result.getIsClaimed());
        assertEquals("未认领", result.getClaimStatus());

        verify(patentMapper).selectById("US20230012345A");
        verify(patentClaimMapper, never()).findByUserIdAndPatentNumber(anyLong(), anyString());
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
    @DisplayName("根据专利申请号获取专利详情 - 带用户ID且已认领")
    void getPatentByNumber_WithUserIdAndClaimed() {
        PatentClaim mockClaim = new PatentClaim();
        mockClaim.setUserId(100L);
        mockClaim.setPatentNumber("US20230012345A");
        mockClaim.setStatus(PatentClaim.ClaimStatus.APPROVED.getCode());

        when(patentMapper.selectById("US20230012345A")).thenReturn(testPatent);
        when(patentClaimMapper.findByUserIdAndPatentNumber(100L, "US20230012345A")).thenReturn(mockClaim);

        PatentDTO result = patentService.getPatentByNumber("US20230012345A", 100L);

        assertNotNull(result);
        assertTrue(result.getIsClaimed());
        assertEquals("通过", result.getClaimStatus());

        verify(patentMapper).selectById("US20230012345A");
        verify(patentClaimMapper).findByUserIdAndPatentNumber(100L, "US20230012345A");
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

    @Test
    @DisplayName("认领专利 - 成功")
    void claimPatent_Success() {
        when(patentMapper.selectById("US20230012345A")).thenReturn(testPatent);
        when(patentClaimMapper.findByUserIdAndPatentNumber(100L, "US20230012345A")).thenReturn(null);
        when(patentClaimMapper.insert(any(PatentClaim.class))).thenReturn(1);

        boolean result = patentService.claimPatent("US20230012345A", 100L, "http://example.com/proof.jpg");

        assertTrue(result);
        verify(patentMapper).selectById("US20230012345A");
        verify(patentClaimMapper).findByUserIdAndPatentNumber(100L, "US20230012345A");
        verify(patentClaimMapper).insert(any(PatentClaim.class));
    }

    @Test
    @DisplayName("认领专利 - 专利不存在")
    void claimPatent_PatentNotFound() {
        when(patentMapper.selectById("INVALID123")).thenReturn(null);

        boolean result = patentService.claimPatent("INVALID123", 100L, "http://example.com/proof.jpg");

        assertFalse(result);
        verify(patentMapper).selectById("INVALID123");
        verify(patentClaimMapper, never()).findByUserIdAndPatentNumber(anyLong(), anyString());
        verify(patentClaimMapper, never()).insert(any(PatentClaim.class));
    }

    @Test
    @DisplayName("认领专利 - 已认领")
    void claimPatent_AlreadyClaimed() {
        PatentClaim existingClaim = new PatentClaim();
        existingClaim.setUserId(100L);
        existingClaim.setPatentNumber("US20230012345A");

        when(patentMapper.selectById("US20230012345A")).thenReturn(testPatent);
        when(patentClaimMapper.findByUserIdAndPatentNumber(100L, "US20230012345A")).thenReturn(existingClaim);

        boolean result = patentService.claimPatent("US20230012345A", 100L, "http://example.com/proof.jpg");

        assertFalse(result);
        verify(patentMapper).selectById("US20230012345A");
        verify(patentClaimMapper).findByUserIdAndPatentNumber(100L, "US20230012345A");
        verify(patentClaimMapper, never()).insert(any(PatentClaim.class));
    }

    @Test
    @DisplayName("获取用户认领的专利 - 有认领记录")
    void getUserClaimedPatents_WithClaims() {
        List<String> patentNumbers = Arrays.asList("US20230012345A", "US20230012346B");
        when(patentClaimMapper.findClaimedPatentNumbersByUserId(100L)).thenReturn(patentNumbers);

        Patent patent2 = new Patent();
        patent2.setPatentNumber("US20230012346B");
        patent2.setPatentName("Another Patent");

        when(patentMapper.selectBatchIds(patentNumbers)).thenReturn(Arrays.asList(testPatent, patent2));

        PageResult<PatentDTO> result = patentService.getUserClaimedPatents(100L, 1, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertTrue(result.getRecords().get(0).getIsClaimed());
        assertEquals("通过", result.getRecords().get(0).getClaimStatus());

        verify(patentClaimMapper).findClaimedPatentNumbersByUserId(100L);
        verify(patentMapper).selectBatchIds(patentNumbers);
    }

    @Test
    @DisplayName("获取用户认领的专利 - 无认领记录")
    void getUserClaimedPatents_NoClaims() {
        when(patentClaimMapper.findClaimedPatentNumbersByUserId(999L)).thenReturn(Collections.emptyList());

        PageResult<PatentDTO> result = patentService.getUserClaimedPatents(999L, 1, 10);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());

        verify(patentClaimMapper).findClaimedPatentNumbersByUserId(999L);
        verify(patentMapper, never()).selectBatchIds(any());
    }

    @Test
    @DisplayName("获取用户认领的专利 - 分页")
    void getUserClaimedPatents_WithPagination() {
        List<String> patentNumbers = Arrays.asList("PAT001", "PAT002", "PAT003", "PAT004", "PAT005");
        when(patentClaimMapper.findClaimedPatentNumbersByUserId(100L)).thenReturn(patentNumbers);

        List<Patent> patents = Arrays.asList(
            createPatent("PAT001", "Patent 1"),
            createPatent("PAT002", "Patent 2"),
            createPatent("PAT003", "Patent 3"),
            createPatent("PAT004", "Patent 4"),
            createPatent("PAT005", "Patent 5")
        );

        when(patentMapper.selectBatchIds(patentNumbers)).thenReturn(patents);

        PageResult<PatentDTO> result = patentService.getUserClaimedPatents(100L, 2, 2);

        assertNotNull(result);
        assertEquals(5, result.getTotal());
        assertEquals(2, result.getRecords().size()); // 第2页，每页2条

        verify(patentClaimMapper).findClaimedPatentNumbersByUserId(100L);
        verify(patentMapper).selectBatchIds(patentNumbers);
    }

    private Patent createPatent(String number, String name) {
        Patent patent = new Patent();
        patent.setPatentNumber(number);
        patent.setPatentName(name);
        patent.setInventor("Inventor");
        patent.setAssignee("Assignee");
        patent.setCountry("US");
        patent.setApplicationDate(LocalDateTime.now());
        patent.setCitationCount(0);
        return patent;
    }
}