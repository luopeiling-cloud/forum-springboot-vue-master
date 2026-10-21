package com.forum.im;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xiaozhao
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.forum.im","com.forum.xo","com.forum.common"})
public class ImApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
    }
}
