package com.example.qyuanpaperrd.obs;

import com.example.qyuanpaperrd.config.HuaweiObsProperties;
import com.huaweicloud.sdk.obs.v1.ObsClient;
import com.huaweicloud.sdk.obs.v1.model.ListBucketsRequest;
import com.huaweicloud.sdk.obs.v1.model.ListBucketsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 华为云 OBS 连接测试
 * 验证 OBS 配置是否正确，能否正常连接
 */
@SpringBootTest
@ActiveProfiles("test")
class ObsConnectionTest {

    @Autowired(required = false)
    private ObsClient obsClient;

    @Autowired
    private HuaweiObsProperties obsProperties;

    @Test
    void testObsPropertiesLoaded() {
        System.out.println("==================== OBS 配置验证 ====================");
        
        assertNotNull(obsProperties, "OBS 配置属性应该被加载");
        
        System.out.println("Endpoint: " + obsProperties.getEndpoint());
        System.out.println("Bucket: " + obsProperties.getBucketName());
        System.out.println("AccessKey: " + (obsProperties.getAccessKey() != null ? "已设置" : "未设置"));
        System.out.println("SecretKey: " + (obsProperties.getSecretAccessKey() != null ? "已设置" : "未设置"));
        System.out.println("Enabled: " + obsProperties.isEnabled());
        
        assertNotNull(obsProperties.getEndpoint(), "Endpoint 不应为空");
        assertNotNull(obsProperties.getBucketName(), "BucketName 不应为空");
        assertNotNull(obsProperties.getAccessKey(), "AccessKey 不应为空");
        assertNotNull(obsProperties.getSecretAccessKey(), "SecretAccessKey 不应为空");
        
        System.out.println("✅ OBS 配置验证通过！");
    }

    @Test
    void testObsClientCreated() {
        System.out.println("==================== OBS 客户端验证 ====================");
        
        assertNotNull(obsClient, "OBS 客户端应该被创建");
        System.out.println("✅ OBS 客户端创建成功！");
    }

    @Test
    void testObsConnection() {
        System.out.println("==================== OBS 连接测试 ====================");
        
        assertNotNull(obsClient, "OBS 客户端必须存在才能测试连接");
        
        try {
            // 尝试列出桶来验证连接
            ListBucketsRequest request = new ListBucketsRequest();
            ListBucketsResponse response = obsClient.listBuckets(request);
            
            assertNotNull(response, "响应不应为空");
            System.out.println("HTTP Status: " + response.getHttpStatusCode());
            
            if (response.getBuckets() != null && response.getBuckets().getBucket() != null) {
                System.out.println("可用的桶数量: " + response.getBuckets().getBucket().size());
                response.getBuckets().getBucket().forEach(bucket -> 
                    System.out.println("  - " + bucket.getName())
                );
            }
            
            System.out.println("✅ OBS 连接测试通过！华为云 OBS 服务可以正常访问。");
            
        } catch (Exception e) {
            System.err.println("❌ OBS 连接失败: " + e.getMessage());
            e.printStackTrace();
            fail("OBS 连接测试失败: " + e.getMessage());
        }
    }
}

