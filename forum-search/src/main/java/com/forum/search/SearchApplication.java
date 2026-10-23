package com.forum.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xiaozhao
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.forum.search","com.forum.xo","com.forum.common"})
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
