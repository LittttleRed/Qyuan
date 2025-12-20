package org.example.qyuanreadnotes;

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
public class QyuanReadNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(QyuanReadNotesApplication.class, args);
    }


}
