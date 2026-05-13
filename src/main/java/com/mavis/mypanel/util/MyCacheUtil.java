package com.mavis.mypanel.util;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.mavis.mypanel.logic.ServerLogic;

import java.util.HashMap;

public class MyCacheUtil {


    private static HashMap<Integer, JSONObject> SERVER_INFO_MAP;

    public static HashMap<Integer, JSONObject> getServerInfoMap() {
        if (SERVER_INFO_MAP == null) {
            SERVER_INFO_MAP = SpringUtil.getBean(ServerLogic.class).getAllServerInfo();
        }
        return SERVER_INFO_MAP;
    }

    public static void setServerInfoMap(HashMap<Integer, JSONObject> serverInfoMap) {
        SERVER_INFO_MAP = serverInfoMap;
    }

    public static void main(String[] args) {

    }
}
