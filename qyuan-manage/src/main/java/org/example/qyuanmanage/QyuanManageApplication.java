package org.example.qyuanmanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class QyuanManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(QyuanManageApplication.class, args);
    }

}
