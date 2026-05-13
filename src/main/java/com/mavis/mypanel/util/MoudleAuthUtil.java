package com.mavis.mypanel.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.mavis.mypanel.entity.TSystemParam;
import com.mavis.mypanel.service.TSystemParamService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 模块校验工具
 */
public class MoudleAuthUtil {

    /**
     * 获取模块加密串
     * @return
     */
    public static String getMoudleAuthEnc(){
        TSystemParamService systemParamService = SpringUtil.getBean(TSystemParamService.class);
        TSystemParam param = systemParamService.lambdaQuery().eq(TSystemParam::getParamId, "KEY_MOUDLE_API").one();
        String time = DateUtil.format(new Date(), "yyyy-MM-dd");
        return SecureUtil.md5(param.getParamValue()+time);
    }

    public static Map<String,String> getMoudleAuthParam(){
        HashMap<String, String> map = new HashMap<>();
        map.put("enc", getMoudleAuthEnc());
        return map;
    }
}
