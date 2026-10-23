package com.forum.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.forum.util","com.forum.common"},exclude = {DataSourceAutoConfiguration.class})
public class UtilApplication {
    public static void main(String[] args) {
        SpringApplication.run(UtilApplication.class, args);
    }
}
