package com.mavis.mypanel.logic;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.mavis.mypanel.entity.TJenkinsJob;
import com.mavis.mypanel.entity.TSystemParam;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.service.TJenkinsJobService;
import com.mavis.mypanel.service.TSystemParamService;
import com.mavis.mypanel.util.JenkinsUtil;
import com.offbytwo.jenkins.model.Job;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JenkinsLogic {

    @Resource
    private TSystemParamService tSystemParamService;

    @Resource
    private TJenkinsJobService jenkinsJobService;


    public JsonReturn testJenkins(String jenkinsUrl, String username, String password) {
        if(StringUtils.isAnyBlank(jenkinsUrl,username,password)){
            return JsonReturn.errorMsg("参数不能为空");
        }
        JenkinsUtil jenkinsUtil = new JenkinsUtil(jenkinsUrl,username,password);
        if(jenkinsUtil.isRunning()){
            return JsonReturn.successMsg("测试链接成功");
        }
        return JsonReturn.errorMsg("测试链接失败");
    }

    public JsonReturn saveJenkinsSetting(String jenkinsUrl, String username, String password) {

        JsonReturn jr = testJenkins(jenkinsUrl, username, password);
        if(!jr.isSuccess()){
            return jr;
        }
        //判断有没有三个参数
        List<TSystemParam> list = tSystemParamService.lambdaQuery().select(TSystemParam::getParamId).in(TSystemParam::getParamId, "JENKINS_URL", "JENKINS_USERNAME", "JENKINS_PASSWORD").list();
        List<String> keys = list.stream().map(TSystemParam::getParamId).collect(Collectors.toList());

        TSystemParam jenkinsUrlParam = new TSystemParam("JENKINS_URL", jenkinsUrl, "Jenkins服务地址");
        if (keys.contains("JENKINS_URL")) {
            tSystemParamService.updateByParamId(jenkinsUrlParam);
        } else {
            tSystemParamService.save(jenkinsUrlParam);
        }
        TSystemParam jenkinsUsernameParam = new TSystemParam("JENKINS_USERNAME", username, "Jenkins用户名");
        if (keys.contains("JENKINS_USERNAME")) {
            tSystemParamService.updateByParamId(jenkinsUsernameParam);
        } else {
            tSystemParamService.save(jenkinsUsernameParam);
        }
        TSystemParam jenkinsPasswordParam = new TSystemParam("JENKINS_PASSWORD", password, "Jenkins密码");
        if (keys.contains("JENKINS_PASSWORD")) {
            tSystemParamService.updateByParamId(jenkinsPasswordParam);
        } else {
            tSystemParamService.save(jenkinsPasswordParam);
        }
        return JsonReturn.successMsg(String.format("保存成功"));
    }

    public JsonReturn getJenkinsConfig() {
        List<TSystemParam> list = tSystemParamService.lambdaQuery().in(TSystemParam::getParamId, "JENKINS_URL", "JENKINS_USERNAME", "JENKINS_PASSWORD").list();
        return JsonReturn.success(list);
    }

    public JsonReturn getJenkinsJobList(TJenkinsJob jenkinsJob) {
        LambdaQueryChainWrapper<TJenkinsJob> qw = jenkinsJobService.lambdaQuery();
        if(StringUtils.isNotBlank(jenkinsJob.getJobName())){
            qw.like(TJenkinsJob::getJobName,jenkinsJob.getJobName());
        }
        List<TJenkinsJob> list = qw.list();
        return JsonReturn.success(list);
    }

    public JsonReturn getJenkinsJobListOffset(String jobName, Integer offset, Integer limit) {
        LambdaQueryChainWrapper<TJenkinsJob> qw = jenkinsJobService.lambdaQuery();
        if(StringUtils.isNotBlank(jobName)){
            qw.like(TJenkinsJob::getJobName,jobName);
        }
        if(offset != null && limit != null){
            qw.orderByAsc(TJenkinsJob::getId)
                    .gt(TJenkinsJob::getId,offset)
                    .last("limit "+ limit)
                    .list();
        }
        List<TJenkinsJob> list = qw.list();
        return JsonReturn.success(list);
    }

    public JsonReturn syncJob() {
        //判断有没有配置
        List<TSystemParam> list = tSystemParamService.lambdaQuery().select(TSystemParam::getParamId).in(TSystemParam::getParamId, "JENKINS_URL", "JENKINS_USERNAME", "JENKINS_PASSWORD").list();
        List<String> keys = list.stream().map(TSystemParam::getParamId).collect(Collectors.toList());
        if (keys.size() != 3) {
            return JsonReturn.errorMsg("请先配置Jenkins服务");
        }
        TSystemParam jenkinsUrlParam = tSystemParamService.getByParamId("JENKINS_URL");
        TSystemParam jenkinsUsernameParam = tSystemParamService.getByParamId("JENKINS_USERNAME");
        TSystemParam jenkinsPasswordParam = tSystemParamService.getByParamId("JENKINS_PASSWORD");
        JenkinsUtil jenkinsUtil = new JenkinsUtil(jenkinsUrlParam.getParamValue(), jenkinsUsernameParam.getParamValue(), jenkinsPasswordParam.getParamValue());
        Map<String, Job> jobMap = jenkinsUtil.getJobList();

        List<TJenkinsJob> allJenkins = jenkinsJobService.lambdaQuery().select(TJenkinsJob::getJobName).list();
        List<String> allJenkinsName = allJenkins.stream().map(TJenkinsJob::getJobName).collect(Collectors.toList());
        ArrayList<TJenkinsJob> saveList = new ArrayList<>();
        jobMap.keySet().forEach(e->{
            if(!allJenkinsName.contains(e)){
                saveList.add(new TJenkinsJob(null,e,null));
            }
        });
        if(CollectionUtil.isNotEmpty(saveList)){
            jenkinsJobService.saveBatch(saveList);
        }
        return JsonReturn.successMsg(String.format(String.format("同步成功,新增:%s 个Job",saveList.size())));
    }

    public JsonReturn delJobById(String id) {
        if(jenkinsJobService.removeById(id)){
            return JsonReturn.successMsg(String.format("删除成功"));
        }
        return JsonReturn.errorMsg(String.format("删除失败"));
    }

    //全量同步，先删除重新同步
    public JsonReturn syncJobHard() {
        //判断有没有配置
        List<TSystemParam> list = tSystemParamService.lambdaQuery().select(TSystemParam::getParamId).in(TSystemParam::getParamId, "JENKINS_URL", "JENKINS_USERNAME", "JENKINS_PASSWORD").list();
        List<String> keys = list.stream().map(TSystemParam::getParamId).collect(Collectors.toList());
        if (keys.size() != 3) {
            return JsonReturn.errorMsg("请先配置Jenkins服务");
        }
        jenkinsJobService.lambdaUpdate().last("where 1=1").remove();
        return syncJob();
    }

    /**
     * 构建job
     * @return
     */
    public JsonReturn buildJobByJobName(String jobName) {
        //先判断有没有这个name
        Long c = jenkinsJobService.lambdaQuery().eq(TJenkinsJob::getJobName, jobName).count();
        if(c == 0){
            return JsonReturn.errorMsg(String.format("没有找到Job:%s",jobName));
        }
        //判断有没有配置
        List<TSystemParam> list = tSystemParamService.lambdaQuery().select(TSystemParam::getParamId).in(TSystemParam::getParamId, "JENKINS_URL", "JENKINS_USERNAME", "JENKINS_PASSWORD").list();
        List<String> keys = list.stream().map(TSystemParam::getParamId).collect(Collectors.toList());
        if (keys.size() != 3) {
            return JsonReturn.errorMsg("请先配置Jenkins服务");
        }
        TSystemParam jenkinsUrlParam = tSystemParamService.getByParamId("JENKINS_URL");
        TSystemParam jenkinsUsernameParam = tSystemParamService.getByParamId("JENKINS_USERNAME");
        TSystemParam jenkinsPasswordParam = tSystemParamService.getByParamId("JENKINS_PASSWORD");
        JenkinsUtil jenkinsUtil = new JenkinsUtil(jenkinsUrlParam.getParamValue(), jenkinsUsernameParam.getParamValue(), jenkinsPasswordParam.getParamValue());
        boolean f = jenkinsUtil.buildJob(jobName);
        if(f){
            return JsonReturn.successMsg(String.format("开始构建Job:%s",jobName));
        }
        return JsonReturn.errorMsg(String.format("构建Job:%s失败",jobName));
    }
}
