package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.anno.Permission;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.logic.JenkinsLogic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("jenkins")
public class JenkinsController {

    @Resource
    private JenkinsLogic jenkinsLogic;


    @RequestMapping("testJenkins")
    @Permission(permission = PermissionEnum.SYSTEM_JENKINS)
    public JsonReturn testJenkins(String jenkinsUrl,String username,String password){
        return jenkinsLogic.testJenkins(jenkinsUrl,username,password);
    }

    @RequestMapping("saveJenkinsSetting")
    @Permission(permission = PermissionEnum.SYSTEM_JENKINS)
    public JsonReturn saveJenkinsSetting(String jenkinsUrl,String username,String password){
        return jenkinsLogic.saveJenkinsSetting(jenkinsUrl,username,password);
    }

    @RequestMapping("getJenkinsConfig")
    @Permission(permission = PermissionEnum.SYSTEM_JENKINS)
    public JsonReturn getJenkinsConfig(){
        return jenkinsLogic.getJenkinsConfig();
    }

    @RequestMapping("getJenkinsJobListOffset")
    @Permission(permission = PermissionEnum.SYSTEM_JENKINS)
    public JsonReturn getJenkinsJobListOffset(String jobName,Integer offset,Integer limit){
        return jenkinsLogic.getJenkinsJobListOffset(jobName,offset,limit);
    }

    @RequestMapping("syncJob")
    @Permission(permission = PermissionEnum.SYSTEM_JENKINS)
    public JsonReturn syncJob(){
        return jenkinsLogic.syncJob();
    }

    @RequestMapping("syncJobHard")
    @Permission(permission = PermissionEnum.SYSTEM_JENKINS)
    public JsonReturn syncJobHard(){
        return jenkinsLogic.syncJobHard();
    }

    @RequestMapping("delJobById")
    @Permission(permission = PermissionEnum.SYSTEM_JENKINS)
    public JsonReturn delJobById(String id){
        return jenkinsLogic.delJobById(id);
    }

    @RequestMapping("buildJobByJobName")
    @Permission(permission = PermissionEnum.SYSTEM_JENKINS)
    public JsonReturn buildJobByJobName(String jobName){
        return jenkinsLogic.buildJobByJobName(jobName);
    }

}
