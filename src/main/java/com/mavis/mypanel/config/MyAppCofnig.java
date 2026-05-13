package com.mavis.mypanel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author : ssj
 * @description : 配置类
 * @date : 2022/8/3
 **/
@Configuration
public class MyAppCofnig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //登录拦截器
        LoginInterceptor loginInterceptor = new LoginInterceptor();

        String[] excludePath = {
                "/",//首页
                "/index.html",//登录页
                "/user/login",//登录页
                "/data/**",//静态资源
                "/static/**"//静态资源
        };
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(excludePath);
    }
}
