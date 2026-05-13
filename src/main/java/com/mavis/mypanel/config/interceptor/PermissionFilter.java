package com.mavis.mypanel.config.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.mavis.mypanel.entity.anno.Permission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * 权限过滤器
 */

@Component
@Aspect
public class PermissionFilter {


    //拦截Permission注解
    @Pointcut(" @annotation(com.mavis.mypanel.entity.anno.Permission)")
    public void proxyAspect() {

    }


    @Around("proxyAspect()")
    public Object doInvoke(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Permission permission = AnnotationUtils.findAnnotation(signature.getMethod(), Permission.class);
        //判断用户有没有权限
        try {
            if(StpUtil.getPermissionList().contains(permission.permission().getPermission())){
                return joinPoint.proceed();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
