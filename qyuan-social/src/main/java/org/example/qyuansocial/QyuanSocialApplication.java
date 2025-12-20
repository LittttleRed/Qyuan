package org.example.qyuansocial;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 社交模块应用启动类
 * 
 * @author Qyuan
 */
@SpringBootApplication
@MapperScan("org.example.qyuansocial.mapper")
public class QyuanSocialApplication {

    public static void main(String[] args) {
        SpringApplication.run(QyuanSocialApplication.class, args);
    }

    /**
     * 配置 MyBatis Plus 分页插件
     * 用于支持 Follow 等功能的分页查询
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}
