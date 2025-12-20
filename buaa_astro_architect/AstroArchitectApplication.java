package org.buaa.buaa_astro_architect;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@ComponentScan(value = {"com.buaa.log", "com.buaa.llm", "org.buaa.buaa_astro_architect"})
@MapperScan
@EnableFeignClients
public class AstroArchitectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstroArchitectApplication.class, args);
    }

}
