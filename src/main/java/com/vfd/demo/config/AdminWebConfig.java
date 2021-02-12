package com.vfd.demo.config;

import com.vfd.demo.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    LoginInterceptor loginInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/pages/login","/login","/pages/register","/pages/forgot-password",
                        "/forget_password","/reset_password","/register","/sendCode","/preserve-file",
                        "/pages/reset-password","/pages/share-file","/test","/shareLogin",
                        "/asserts/**","/webjars/**");
    }
}