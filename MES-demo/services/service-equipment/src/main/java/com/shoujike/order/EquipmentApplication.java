package com.shoujike.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@EnableFeignClients
public class EquipmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(EquipmentApplication.class, args);
    }
}
