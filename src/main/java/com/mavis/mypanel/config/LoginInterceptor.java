package com.mavis.mypanel.config;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mavis.mypanel.util.StaticUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author : ssj
 * @description : 登录拦截器
 * @date : 2022/8/3
 **/
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(StpUtil.isLogin()){
            return true;
        }else {
            String secend = StaticUtil.getProp("server.servlet.context-path");
            if(StringUtils.isNotBlank(secend)){
                response.sendRedirect(secend+"/index.html");
            }else {
                response.sendRedirect("/index.html");
            }
            return false;
        }
    }
}
