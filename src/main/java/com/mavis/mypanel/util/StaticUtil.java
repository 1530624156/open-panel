package com.mavis.mypanel.util;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * 静态数据工具
 * yll 2022年8月3日09:45:20
 */
@Component
public class StaticUtil {


    @Autowired
    private Environment env;

    /**
     * 附件存储路径
     */
    public static String mypanel_save_path;

    /**
     * dockerTLS证书CA，CERT，KEY存放路径
     */
    public static String mypanel_docker_tls_root;

    public static String JENKINS_BASE_URL;

    public static String JENKINS_USERNAME;

    public static String JENKINS_PASSWORD;

    public Environment getEnv() {
        return env;
    }

    public static String getProp(String key) {
        Environment env = SpringUtil.getBean(StaticUtil.class).getEnv();
        return env.getProperty(key);
    }

    @PostConstruct
    private void init(){
        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {
            try {
                String value = env.getProperty(field.getName());
                Class<?> type = field.getType();
                switch (type.getName()){
                    case "java.lang.Integer":
                        field.set(field,Integer.valueOf(value));
                        break;
                    case "java.lang.Boolean":
                        field.set(field,Boolean.valueOf(value));
                        break;
                    default:
                        field.set(field,value);
                        break;
                }
            } catch (IllegalAccessException e) {
                System.out.println(field.getName() + " 初始化失败");
            }
        }
    }


}
