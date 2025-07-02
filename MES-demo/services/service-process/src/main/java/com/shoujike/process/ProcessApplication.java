package com.shoujike.process;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.shoujike.process",
        "com.shoujike.common.config"
})
@EnableFeignClients
public class ProcessApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessApplication.class, args);
    }
}
