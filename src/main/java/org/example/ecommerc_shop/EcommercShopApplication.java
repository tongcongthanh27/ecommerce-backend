package org.example.ecommerc_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableScheduling
@EnableRetry
public class EcommercShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommercShopApplication.class, args);
    }

}
