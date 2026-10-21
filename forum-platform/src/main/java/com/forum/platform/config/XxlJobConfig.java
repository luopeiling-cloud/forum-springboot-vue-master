package com.forum.platform.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class XxlJobConfig {

    @Value("${forum.admin.addresses}")
    String adminAddresses;
    @Value("${forum.executor.appname}")
    String appname;
    @Value("${forum.accessToken}")
    String accessToken;

    @Value("${forum.executor.ip}")
    String ip;
    @Value("${forum.executor.port}")
    Integer port;
    @Value("${forum.executor.logpath}")
    String logpath;
    @Value("${forum.executor.logretentiondays}")
    Integer logretentiondays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logpath);
        xxlJobSpringExecutor.setLogRetentionDays(logretentiondays);

        return xxlJobSpringExecutor;
    }
}
