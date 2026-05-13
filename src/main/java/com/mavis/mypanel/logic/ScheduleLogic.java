package com.mavis.mypanel.logic;

import com.mavis.mypanel.util.MyCacheUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ScheduleLogic {

    @Resource
    private ServerLogic serverLogic;
    @Resource
    private ServerDockerLogic serverDockerLogic;

    /**
     * 刷新服务器信息
     */
    public void spanServerInfo() {
        MyCacheUtil.setServerInfoMap(serverLogic.getAllServerInfo());
    }

    //刷新docker节点信息
    public void spanDockerNodeInfo() {
        serverDockerLogic.spanDockerNodeInfo();
    }
}
