package com.mavis.mypanel.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.lang.reflect.Field;

public class QueryWrapperBuffer {
    public static QueryWrapper getByNotNullField(Object t){
        if(t == null){
            return null;
        }
        Class clazz = t.getClass();
        QueryWrapper queryWrapper = new QueryWrapper<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if(field.get(t)!=null && !"serial_Version_U_I_D".equals(fieldToColumn(field.getName())) && field.get(t)!="") {
                    //queryWrapper.eq(field.getName(),field.get(t));
                    queryWrapper.eq(fieldToColumn(field.getName()),field.get(t));
                }
            } catch (IllegalAccessException e) {
                continue;
            }
        }
        return queryWrapper;
    }

    private static String fieldToColumn(String field){
        StringBuffer sb = new StringBuffer("");
        for(int i=0;i<field.length();i++){
            //判断首字母是否为大写字母
            if('A'<=field.charAt(i)&&field.charAt(i)<='Z'){
                sb.append("_"+field.charAt(i));
            }else {
                sb.append(field.charAt(i)+"");
            }
        }
        return sb.toString();
    }
}
