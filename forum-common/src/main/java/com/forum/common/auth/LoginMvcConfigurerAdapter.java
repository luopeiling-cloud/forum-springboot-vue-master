package com.forum.common.auth;
import com.forum.common.constant.UploadFileConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author xiaozhao
 */
@Configuration
public class LoginMvcConfigurerAdapter extends WebMvcConfigurationSupport{

    // /util/oss/**（上传/删除）要求登录；白名单仅保留 msm/dm（短信/邮件预留）
    private static final String[] EXCLUDE_PATHS = {"/**/auth/**", "/**/util/msm/**", "/**/util/dm/**", "/**/search/**","/**/oss/**","/**/category/getCategoryTreeData"};
    /**
     * 拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLoginInterceptor())
                .excludePathPatterns(EXCLUDE_PATHS)
                .addPathPatterns("/**");

        super.addInterceptors(registry);
    }

    /**
     * 静态资源访问
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(UploadFileConstant.OSS+"/**") //虚拟url路径
                .addResourceLocations("file:"+UploadFileConstant.ADDRESS); //真实本地路径
        super.addResourceHandlers(registry);
    }
}
