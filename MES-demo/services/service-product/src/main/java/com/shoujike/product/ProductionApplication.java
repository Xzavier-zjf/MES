package com.shoujike.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.shoujike.product.mapper")
@EnableFeignClients
public class ProductionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductionApplication.class, args);
    }
}
