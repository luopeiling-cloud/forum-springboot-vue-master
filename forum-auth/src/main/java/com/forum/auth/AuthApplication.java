package com.forum.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xiaozhao
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.forum.auth","com.forum.xo","com.forum.common"})
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
