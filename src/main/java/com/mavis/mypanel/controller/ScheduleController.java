package com.mavis.mypanel.controller;

import com.mavis.mypanel.logic.ScheduleLogic;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 定时任务控制层
 */
@RestController
@EnableScheduling
public class ScheduleController {

    @Resource
    private ScheduleLogic scheduleLogic;

    /**
     * 每5分钟更新主机信息
     */
    @Scheduled(cron = "0/5 * * * * ? ")
    @RequestMapping("schedule/tests")
    public void spanServerInfo(){
        //更新主机信息
        scheduleLogic.spanServerInfo();
        //更新docker节点信息
        scheduleLogic.spanDockerNodeInfo();
    }


}
