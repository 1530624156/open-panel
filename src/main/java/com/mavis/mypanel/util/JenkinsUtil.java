package com.mavis.mypanel.util;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.client.util.EncodingUtils;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * @program: cx-mirror
 * @description: jenkins工具类
 * 1 . 创建 Job 的 xml，可以在 jenkins 中查看，例如 https://jenkins.jw.chaoxing.com/job/{job名称}/config.xml 来查看该 job 的 xml 配置
 * 2 . 更新视图配置信息 https://jenkins.jw.chaoxing.com/view/{视图名}/config.xml
 * @author: liwei
 * @create: 2023-11-22 10:30
 **/
@SuppressWarnings("all")
public class JenkinsUtil {

    private String JENKINS_URL;
    private String JENKINS_USERNAME;
    private String JENKINS_PASSWORD;

    private JenkinsServer jenkinsServer;
    private JenkinsHttpClient jenkinsHttpClient;

    private static final Long SLEEP_TIME = 30000L;

    public JenkinsUtil(String JENKINS_URL, String JENKINS_USERNAME, String JENKINS_PASSWORD) {
        this.JENKINS_URL = JENKINS_URL;
        this.JENKINS_USERNAME = JENKINS_USERNAME;
        this.JENKINS_PASSWORD = JENKINS_PASSWORD;
        this.jenkinsServer = setJenkinsServer();
        this.jenkinsHttpClient = setJenkinsHttpClient();
    }

    /**
     * Http 客户端工具
     * @return JenkinsServer
     */
    private JenkinsHttpClient setJenkinsHttpClient(){
        try{
            jenkinsHttpClient = new JenkinsHttpClient(new URI(JENKINS_URL),JENKINS_USERNAME,JENKINS_PASSWORD);
        }catch (URISyntaxException e){
            e.printStackTrace();
        }
        return jenkinsHttpClient;
    }

    /**
     * 连接jenkins
     * @return JenkinsServer
     */
    private JenkinsServer setJenkinsServer() {
        try {
            jenkinsServer = new JenkinsServer(new URI(JENKINS_URL), JENKINS_USERNAME, JENKINS_PASSWORD);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return jenkinsServer;
    }


    // ==================== View 视图相关 (开始) ====================
    /**
     * 创建视图 (默认创建 List View)
     * @param viewName 视图名称
     * @param description 视图描述
     */
    public void createView(String viewName,String description) {
        try {
            String xml ="<listView _class=\"hudson.model.ListView\">\n" +
                    "<description>" + description +"</description>\n" +
                    "</listView>";
            jenkinsServer.createView(viewName,xml);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * 获取视图基本信息
     * @param viewName 视图名称
     */
    public View getView(String viewName) {
        try {
            View view = jenkinsServer.getView(viewName);
            // 解码
            String url  = URLDecoder.decode(view.getUrl(),"UTF-8");
            view.setUrl(url);
            
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取视图配置信息
     * @param viewName 视图名称
     */
    public Map<String,String> getViewConfig(String viewName) {
        try {
            Map<String,String> map = new HashMap<>();
            String viewXml = jenkinsHttpClient.get("/view/" + viewName + "/api/xml");
            String viewConfigXml = jenkinsHttpClient.get("/view/" + viewName + "/config.xml");
            map.put("viewXml",viewXml);
            map.put("viewConfigXml",viewConfigXml);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新视图配置信息
     * @param viewName 视图名称
     * @param viewConfigXml 视图配置信息
     */
    public void updateViewConfig(String viewName,String viewConfigXml) {
        try {
            jenkinsServer.updateView(viewName, viewConfigXml);
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 删除视图
     * @param viewName 视图名称
     */
    public void deleteView(String viewName) {
        try {
            jenkinsHttpClient.post("/view/" + viewName + "/doDelete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== View 视图相关 (结束) ====================




    // ==================== Job 任务相关 (开始) ====================
    /**
     * 创建 Jenkins Job 并指定 Job 类型   此处为自由风格项目
     * 关于 Jenkins Job 部分类型，如下：
     *   - 自由风格项目：hudson.model.FreeStyleProject
     *   - Maven 项目：hudson.maven.MavenModuleSet
     *   - 流水线项目：org.jenkinsci.plugins.workflow.job.WorkflowJob
     *   - 多配置项目：hudson.matrix.MatrixProject
     *
     * @param jobName 任务名称
     */
    public boolean createJob(String jobName,String jobXml) {
        try {
            // 创建 Jenkins Job 并指定 Job 类型
            jenkinsHttpClient.post_xml("createItem?name=" + EncodingUtils.encodeParam(jobName) +
                    "&mode=hudson.model.FreeStyleProject", jobXml, true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取 Job 基本信息
     * @param jobName 任务名称
     */
    public Job getJob(String jobName) {
        try {
            JobWithDetails job = jenkinsServer.getJob(jobName);
            return job;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Maven Job 基本信息
     * @param jobName 任务名称
     */
    public Job getMavenJob(String jobName){
        try {
            // 获取 Job 信息
            MavenJobWithDetails job = jenkinsServer.getMavenJob(jobName);
            
            return job;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Job 列表
     * @return Map<String,Job> Job 列表
     */
    public Map<String, Job> getJobList(){
        try {
            // 获取 Job 列表
            Map<String,Job> jobs = jenkinsServer.getJobs();
            
            return jobs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据 View 获取 Job 列表
     */
    public Map<String, Job> getJobListByView(String viewName) {
        try {
            // 获取 Job 列表
            Map<String, Job> jobs = jenkinsServer.getJobs(viewName);
            
            return jobs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Job XML 配置信息
     * @param jobName 任务名称
     */
    public String getJobConfig(String jobName) {
        try {
            // 获取 Job XML 配置信息
            String jobConfigXml = jenkinsServer.getJobXml(jobName);
            
            return jobConfigXml;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 执行 无参数 Job build
     */
    public boolean buildJob(String jobName) {
        try {
            jenkinsServer.getJob(jobName).build();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 构建任务并且返回当前构建的buildNumber
     *  * @param jobName 任务名称
     * @param parameters 构建参数
     * @return 当前任务的buildNumber
     * @throws IOException
     */
    public Long getBulidNumberBuildJob(String jobName)  {
        try{
            // 1.获取Job信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 2.使用构建参数执行本次构建
            QueueReference queueReference = job.build();
            QueueItem queueItem = jenkinsServer.getQueueItem(queueReference);
            // 3.获取构建的buildNumber
            Executable executable = queueItem.getExecutable();
            // 这边需要进行一下轮训
            while (executable == null) {
                executable = jenkinsServer.getQueueItem(queueReference).getExecutable();
                try {
                    sleep(SLEEP_TIME);
                    System.out.println("等待执行任务中...");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Long number = executable.getNumber();
            return number;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据 Job 名称和 Job Build 编号判断 执行结果
     * @param name 任务名称
     * @param number 编号
     * @return boolean
     */
    public boolean getBuildResult(String name, int number) {
        try {
            Build build = jenkinsServer.getJob(name).getBuildByNumber(number);
            BuildWithDetails details = build.details();
            // 轮询
            while (details.isBuilding()) {
                details = build.details();
                try {
                    sleep(SLEEP_TIME);
                    System.out.println("等待构建结果中...");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return details.getResult().equals(BuildResult.SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 执行 有参数 Job build
     */
    public void buildJobWithParams(String jobName, Map<String, String> params) {
        try {
            jenkinsServer.getJob(jobName).build(params);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止最后构建的 Job Build
     */
    public void stopJobBuild(String jobName) {
        try {
            // 获取最后的 build 信息
            Build build = jenkinsServer.getJob(jobName).getLastBuild();
            // 停止最后的 build
            build.Stop();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除job
     */
    public void deleteJob(String jobName) {
        try {
            jenkinsServer.deleteJob(jobName);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 禁用job
     */
    public void disableJob(String jobName) {
        try {
            jenkinsServer.disableJob(jobName);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启用job
     */
    public void enableJob(String jobName) {
        try {
            jenkinsServer.enableJob(jobName);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== Job 任务相关 (结束) ====================



    // ==================== Build 构建相关 (开始) ====================

    public boolean isSuccessBuild(String jobName) {
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 获取最后成功的 build 信息
            Build lastSuccessfulBuild = job.getLastSuccessfulBuild();
            // 获取最后执行的 build 信息
            Build lastBuild = job.getLastBuild();
            // 判断最后成功的 build 是否等于最后执行的 build
            if (lastSuccessfulBuild.getNumber() == lastBuild.getNumber()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断 Job 是否正在构建
     * @param jobName 任务名称
     * @return boolean
     */
    public boolean isBuilding(String jobName) {
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 获取最后的 build 信息
            Build build = job.getLastBuild();
            // 判断是否正在构建
            boolean building = build.details().isBuilding();

            return building;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取 Job 最后成功的 Build
     * @param jobName 任务名称
     */
    public Build getLastSuccessfulBuild(String jobName) {
        try {
            // 获取最后的 build 信息
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 获得最后编译信息
            Build lastBuild = job.getLastBuild();
            // 获取最后成功的编译信息
            Build lastSuccessfulBuild = job.getLastSuccessfulBuild();
            // 获取最后事变的编译信息
            Build lastFailedBuild = job.getLastFailedBuild();
            // 获取最后完成的编译信息
            Build lastCompletedBuild = job.getLastCompletedBuild();
            // 获取最后稳定的编译信息
            Build lastStableBuild = job.getLastStableBuild();
            // 获取最后不稳定的编译信息
            Build lastUnstableBuild = job.getLastUnstableBuild();
            // 获取最后未成功的编译信息
            Build lastUnsuccessfulBuild = job.getLastUnsuccessfulBuild();
            return lastSuccessfulBuild;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Job 首次 Build 信息
     * @param jobName 任务名称
     */
    public Build getFirstBuild(String jobName) {
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 获取首次编译信息
            Build firstBuild = job.getFirstBuild();
            return firstBuild;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据 Job Build 编号 获取 Build 信息
     */
    public Build getJobByNumber(String jobName, Integer number){
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 根据 Build 编号获取 Build 信息
            Build build = job.getBuildByNumber(number);
            
            return build;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取全部 Job Build 列表
     */
    public List<Build> getJobBuildListAll(String jobName) {
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 获取全部 Build 列表
            List<Build> builds = job.getAllBuilds();
            
            return builds;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Job Build 列表(带范围)
     */
    public List<Build> getJobBuildListRange(String jobName,Integer from,Integer to) {
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 设定范围
            Range range = Range.build().from(from).to(to);
            System.err.println(range.getRangeString());
            // 获取一定范围的 Build 信息
            List<Build> builds = job.getAllBuilds(range);
            
            return builds;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Build 信息
     */
    public Build getJobBuildInfo(String jobName){
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 这里用最后一次编译来示例
            Build build = job.getLastBuild();
            // 获取构建的 URL 地址
            System.out.println(build.getUrl());
            // 获取构建编号
            System.out.println(build.getNumber());
            // 获取测试报告
            //build.getTestReport();
            // 获取测试结果
            //build.getTestResult();
            
            return build;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Build 详细信息
     */
    public BuildWithDetails getJobBuildDetailInfo(String jobName){
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 这里用最后一次编译来示例
            BuildWithDetails build = job.getLastBuild().details();
            // 获取构建的显示名称
            System.out.println(build.getDisplayName());
            // 获取构建的参数信息
            System.out.println(build.getParameters());
            // 获取构建编号
            System.out.println(build.getNumber());
            // 获取构建结果，如果构建未完成则会显示为null
            System.out.println(build.getResult());
            // 获取执行构建的活动信息
            System.out.println(build.getActions());
            // 获取构建持续多少时间(ms)
            System.out.println(build.getDuration());
            // 获取构建开始时间戳
            System.out.println(build.getTimestamp());
            // 获取构建头信息，里面包含构建的用户，上游信息，时间戳等
            List<BuildCause> buildCauses = build.getCauses();
            for (BuildCause bc:buildCauses){
                System.out.println(bc.getUserId());
                System.out.println(bc.getShortDescription());
                System.out.println(bc.getUpstreamBuild());
                System.out.println(bc.getUpstreamProject());
                System.out.println(bc.getUpstreamUrl());
                System.out.println(bc.getUserName());
            }
            
            return build;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Build Log 日志信息
     */
    public BuildWithDetails getJobBuildLog(String jobName){
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 这里用最后一次编译来示例
            BuildWithDetails build = job.getLastBuild().details();
            // 获取构建的日志，如果正在执行构建，则会只获取已经执行的过程日志

            // Text格式日志
            System.out.println(build.getConsoleOutputText());
            // Html格式日志
            System.out.println(build.getConsoleOutputHtml());

            // 获取部分日志,一般用于正在执行构建的任务
            ConsoleLog consoleLog = build.getConsoleOutputText(0);
            // 获取当前日志大小
            System.out.println(consoleLog.getCurrentBufferSize());
            // 是否已经构建完成，还有更多日志信息
            System.out.println(consoleLog.getHasMoreData());
            // 获取当前截取的日志信息
            System.out.println(consoleLog.getConsoleLog());
            
            return build;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 最后一次构建 日志
     */
    public BuildWithDetails getLastBuildLog(String jobName) {
        try {
            // 获取 Job 信息
            JobWithDetails job = jenkinsServer.getJob(jobName);
            // 最后一次编译
            BuildWithDetails build = job.getLastBuild().details();
            return build;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取正在执行构建任务的日志信息
     */
    public ConsoleLog getBuildActiveLog(String jobName){
        try {
            // 这里用最后一次编译来示例
            BuildWithDetails build = jenkinsServer.getJob(jobName).getLastBuild().details();
            // 当前日志
            ConsoleLog currentLog = build.getConsoleOutputText(0);
            // 输出当前获取日志信息
            System.out.println(currentLog.getConsoleLog());
            // 检测是否还有更多日志,如果是则继续循环获取
            while (currentLog.getHasMoreData()){
                // 获取最新日志信息
                ConsoleLog newLog = build.getConsoleOutputText(currentLog.getCurrentBufferSize());
                // 输出最新日志
                System.out.println(newLog.getConsoleLog());
                currentLog = newLog;
                // 睡眠1s
                sleep(1000);
            }
            
            return currentLog;
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== Build 构建相关 (结束) ====================


    // ==================== jenkins 系统相关 (开始) ====================
    /**
     * 获取 主机 信息
     */
    public Map<String, Computer> getComputerInfo(){
        try {
            Map<String, Computer> map = jenkinsServer.getComputers();
            for (Computer computer : map.values()) {
                // 获取当前节点-节点名称
                System.out.println(computer.details().getDisplayName());
                // 获取当前节点-执行者数量
                System.out.println(computer.details().getNumExecutors());
                // 获取当前节点-执行者详细信息
                List<Executor> executorList = computer.details().getExecutors();
                // 查看当前节点-是否脱机
                System.out.println(computer.details().getOffline());
                // 获得节点的全部统计信息
                LoadStatistics loadStatistics = computer.details().getLoadStatistics();
                // 获取节点的-监控数据
                Map<String, Map> monitorData = computer.details().getMonitorData();
            }
            
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 重启 Jenkins (慎重使用)
     */
    public void restart(){
        try {
            jenkinsServer.restart(true);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 安全重启 Jenkins (慎重使用)
     */
    public void safeRestart(){
        try {
            jenkinsServer.safeRestart(true);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭 Jenkins 连接
     */
    public void close(){
        if (jenkinsServer != null) {
            jenkinsServer.close();
        }
    }

    /**
     * 根据 Label 查找代理节点信息
     */
    public void getLabelNodeInfo() {
        try {
            LabelWithDetails labelWithDetails = jenkinsServer.getLabel("jnlp-agent");
            // 获取标签名称
            System.out.println(labelWithDetails.getName());
            // 获取 Cloud 信息
            System.out.println(labelWithDetails.getClouds());
            // 获取节点信息
            System.out.println(labelWithDetails.getNodeName());
            // 获取关联的 Job
            System.out.println(labelWithDetails.getTiedJobs());
            // 获取参数列表
            System.out.println(labelWithDetails.getPropertiesList());
            // 是否脱机
            System.out.println(labelWithDetails.getOffline());
            // 获取描述信息
            System.out.println(labelWithDetails.getDescription());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断 Jenkins 是否运行
     */
    public boolean isRunning() {
        boolean isRunning = jenkinsServer.isRunning();
        
        return isRunning;
    }

    /**
     * 获取 Jenkins 插件信息
     */
    public List<Plugin> getPluginInfo(){
        try {
            PluginManager pluginManager =jenkinsServer.getPluginManager();
            // 获取插件列表
            List<Plugin> plugins = pluginManager.getPlugins();
            for (Plugin plugin:plugins){
                // 插件 wiki URL 地址
                System.out.println(plugin.getUrl());
                // 版本号
                System.out.println(plugin.getVersion());
                // 简称
                System.out.println(plugin.getShortName());
                // 完整名称
                System.out.println(plugin.getLongName());
                // 是否支持动态加载
                System.out.println(plugin.getSupportsDynamicLoad());
                // 插件依赖的组件
                System.out.println(plugin.getDependencies());
            }
            
            return plugins;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JenkinsServer getJenkinsServer() {
        return jenkinsServer;
    }

// ==================== jenkins 系统相关 (结束) ====================

    public static void main(String[] args) {
        //JenkinsUtil jenkinsUtil = new JenkinsUtil("https://jenkins.jw.chaoxing.com","devops", "devops_jenkins@");
        //HashMap<String, String> params = new HashMap<>();
        //params.put("param1","value1");
        //jenkinsUtil.buildJobWithParams("paramJob", params);
    }
}

