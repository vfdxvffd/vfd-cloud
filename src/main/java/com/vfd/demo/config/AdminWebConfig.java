package com.vfd.demo.config;

import com.vfd.demo.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @PackageName: com.vfd.demo.config
 * @ClassName: AdminWebConfig
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/1/27 下午12:51
 */
@Configuration
public class AdminWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/","/login","/asserts/**","/webjars/**");
    }
}