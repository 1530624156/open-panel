package com.mavis.mypanel.logic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mavis.mypanel.entity.TNginxNode;
import com.mavis.mypanel.entity.TSystemParam;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.service.TNginxNodeService;
import com.mavis.mypanel.service.TSystemParamService;
import com.mavis.mypanel.util.MoudleAuthUtil;
import com.mavis.mypanel.util.httputil.HttpResult;
import com.mavis.mypanel.util.httputil.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NginxLogic {


    //根据传入配置进行测试（保存前测试）
    private static String API_TEST_BY_CONFIG = "%s/testByConfig";
    //直接测试nginx节点本地配置
    private static String API_TEST_BY_PROP = "%s/testByProperties";
    //获取版本接口
    private static String API_VERSION = "%s/version";
    //初始化nginx配置接口
    private static String API_INIT = "%s/nginx/init";

    //获取nginx监控接口
    private static String API_MONITOR = "%s/monitor/getMonitorInfo";
    //获取nginx进程接口
    private static String API_GET_PROCESS = "%s/nginx/getNginxProcess";

    //config配置相关接口
    //获取config接口
    private static String API_GET_CONFIGS = "%s/nginx/getServerConfigs";
    //获取config文件名里列表
    private static String API_GET_CONFIG_NAMES = "%s/nginx/getServerConfigsNames";
    //修改config文件
    private static String API_UPDATE_CONFIG = "%s/nginx/updateConfig";
    //添加config文件
    private static String API_CREATE_CONFIG = "%s/nginx/createConfig";
    //添加config文件
    private static String API_DELETE_CONFIG = "%s/nginx/deleteConfig";

    //nginx操控相关接口
    //启动
    private static String API_START_NGINX = "%s/nginx/start";
    //停止
    private static String API_STOP_NGINX = "%s/nginx/stop";
    //重载
    private static String API_RELOAD_NGINX = "%s/nginx/reload";
    //测试
    private static String API_TEST_NGINX = "%s/nginx/test";

    @Resource
    private TNginxNodeService nginxNodeService;

    @Resource
    private TSystemParamService systemParamService;


    public JsonReturn getNginxNode(TNginxNode nginxNode) {
        QueryWrapper<TNginxNode> qw = new QueryWrapper<>();
        if(StringUtils.isNotBlank(nginxNode.getName())){
            qw.like("name", nginxNode.getName());
        }
        if(nginxNode.getStatus() != null){
            qw.eq("status", nginxNode.getStatus());
        }
        return JsonReturn.success(nginxNodeService.list(qw));
    }


    /**
     * 测试nginx节点情况
     * @param nginxNode
     * @param by 测试方式 (config,id)
     * @return
     */
    public JsonReturn testBy(TNginxNode nginxNode,String by) {
        String url;
        if("config".equals(by)){
            url = String.format(API_TEST_BY_CONFIG, nginxNode.getApiUrl());
        }else {
            url = String.format(API_TEST_BY_PROP, nginxNode.getApiUrl());
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("nginx_deploy_type", nginxNode.getDeployType());
        params.put("nginx_config_path", nginxNode.getConfPath());
        params.put("nginx_exec_path", nginxNode.getExecPath());
        params.put("nginx_docker_name", nginxNode.getDockerName());
        params.put("enc", MoudleAuthUtil.getMoudleAuthEnc());
        HttpResult resp = HttpUtil.postMethod(url, params, "utf-8");
        if("200".equals(resp.getStatuscodes())){
            try {
                String html = resp.getResponseHtml();
                JSONObject res = JSON.parseObject(html);
                if(res.getInteger("result") != 1){
                    return JsonReturn.errorMsg(res.getString("msg"));
                }
                String data = res.getString("data");
                if(data.contains("worker process")){
                    return JsonReturn.successMsg("测试通过");
                }else {
                    return JsonReturn.errorMsg("测试失败(未检测到nginx进程)");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return JsonReturn.errorMsg(e.getMessage());
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }

    public JsonReturn addNginxNode(TNginxNode nginxNode) {
        nginxNode.setStatus(-1);
        nginxNode.setApiVersion(getApiVersion(nginxNode));
        boolean f = nginxNodeService.save(nginxNode);
        if(f){
            initNginxNode(nginxNode);
            return JsonReturn.successMsg("添加成功");
        }
        return JsonReturn.errorMsg("添加失败");
    }

    /**
     * 修改节点配置（初始化）
     * @param nginxNode
     * @return
     */
    private boolean initNginxNode(TNginxNode nginxNode) {
        String url = String.format(API_INIT, nginxNode.getApiUrl());
        HashMap<String, String> params = new HashMap<>();
        params.put("nginx_deploy_type", nginxNode.getDeployType());
        params.put("nginx_config_path", nginxNode.getConfPath());
        params.put("nginx_exec_path", nginxNode.getExecPath());
        params.put("nginx_docker_name", nginxNode.getDockerName());
        params.put("enc", MoudleAuthUtil.getMoudleAuthEnc());
        HttpResult resp = HttpUtil.postMethod(url, params, "utf-8");
        if("200".equals(resp.getStatuscodes())){
            try {
                String html = resp.getResponseHtml();
                JSONObject res = JSON.parseObject(html);
                if(res.getInteger("result") != 1){
                    return false;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public JsonReturn removeNginxNode(Integer id) {
        Long c = nginxNodeService.lambdaQuery().eq(TNginxNode::getId, id).count();
        if(c == 0){
            return JsonReturn.errorMsg("未找到该节点");
        }
        return nginxNodeService.removeById(id) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }


    public JsonReturn testByConfig(TNginxNode nginxNode) {
        return testBy(nginxNode, "config");
    }

    public JsonReturn testByNginxNodeId(Integer id) {
        TNginxNode nginxNode = nginxNodeService.getById(id);
        if(nginxNode == null){
            return JsonReturn.errorMsg("未找到该节点");
        }
        JsonReturn jr = testBy(nginxNode, "id");
        if(jr.isSuccess()){
            nginxNode.setStatus(1);
        }else{
            nginxNode.setStatus(0);
        }
        nginxNodeService.updateById(nginxNode);
        return jr;
    }

    public JsonReturn updateNginxNode(TNginxNode nginxNode) {
        if(nginxNodeService.lambdaQuery().eq(TNginxNode::getId, nginxNode.getId()).count() == 0){
            return JsonReturn.errorMsg("未找到该节点");
        }
        nginxNode.setStatus(-1);
        //更新版本
        nginxNode.setApiVersion(getApiVersion(nginxNode));
        boolean f = nginxNodeService.updateById(nginxNode);
        if(f){
            initNginxNode(nginxNode);
            return JsonReturn.successMsg("修改成功");
        }
        return JsonReturn.errorMsg("修改失败");
    }

    public String getApiVersion(TNginxNode tNginxNode){
        String url = String.format(API_VERSION, tNginxNode.getApiUrl());
        HttpResult resp = HttpUtil.postMethod(url,MoudleAuthUtil.getMoudleAuthParam(), "utf-8");
        if("200".equals(resp.getStatuscodes())){
            try {
                String html = resp.getResponseHtml();
                JSONObject res = JSON.parseObject(html);
                if(res.getInteger("result") == 1){
                    String data = res.getString("data");
                    return data;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public JsonReturn getNginxNodeMornitor(TNginxNode nginxNode) {
        String url = String.format(API_MONITOR, nginxNode.getApiUrl());
        HttpResult resp = HttpUtil.postMethod(url,MoudleAuthUtil.getMoudleAuthParam(),"utf-8");
        if("200".equals(resp.getStatuscodes())){
            try {
                String html = resp.getResponseHtml();
                JSONObject res = JSON.parseObject(html);
                if(res.getInteger("result") == 1){
                    String data = res.getString("data");
                    return JsonReturn.success(JSON.parseObject(data));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }

    public JsonReturn getNginxCheckStatus(TNginxNode nginxNode) {
        String url = nginxNode.getServiceUrl() + nginxNode.getCheckStatus();
        if(StringUtils.isNotBlank(nginxNode.getCheckStatusUsername())){
            try {
                if(url.contains("https://")){
                    String replace = String.format("https://%s:%s@", nginxNode.getCheckStatusUsername(), URLEncoder.encode(nginxNode.getCheckStatusPassword(),"utf-8"));
                    url = url.replace("https://", replace);
                }else {
                    String replace = String.format("http://%s:%s@", nginxNode.getCheckStatusUsername(), URLEncoder.encode(nginxNode.getCheckStatusPassword(),"utf-8"));
                    url = url.replace("http://", replace);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        HttpResult resp = HttpUtil.getMethod(url);
        if("200".equals(resp.getStatuscodes())){
            String html = resp.getResponseHtml();
            Document doc = Jsoup.parse(html);
            JSONArray arr = new JSONArray();
            Iterator<Element> trs = doc.select("tr").not(":nth-child(1)").iterator();
            while (trs.hasNext()){
                Element tr = trs.next();
                Elements td = tr.select("td");

                JSONObject obj = new JSONObject();
                obj.put("Upstream",td.get(1).text());
                obj.put("Name",td.get(2).text());
                obj.put("Status",td.get(3).text());
                obj.put("Rise_counts",td.get(4).text());
                obj.put("Fall_counts",td.get(5).text());
                arr.add(obj);
            }
            return JsonReturn.success(arr);
        }
        return JsonReturn.errorMsg("获取健康状态失败");
    }

    /**
     * 获取nginx所有的config配置文件情况
     * @return
     */
    public JsonReturn getServerConfigsNames(Integer nodeId) {
        TNginxNode nginxNode = nginxNodeService.getById(nodeId);
        if(nginxNode == null){
            return JsonReturn.errorMsg("未找到该节点");
        }
        String url = String.format(API_GET_CONFIG_NAMES, nginxNode.getApiUrl());
        HttpResult resp = HttpUtil.postMethod(url,MoudleAuthUtil.getMoudleAuthParam(),"utf-8");
        if("200".equals(resp.getStatuscodes())){
            try {
                String html = resp.getResponseHtml();
                JSONObject res = JSON.parseObject(html);
                if(res.getInteger("result") == 1){
                    JSONArray arr = res.getJSONArray("data");
                    return JsonReturn.success(arr);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }


    /**
     * 获取nginx所有的config配置文件情况
     * @param nodeId
     * @param configName
     * @return
     */
    public JsonReturn getNginxConfigs(Integer nodeId,String configName) {
        TNginxNode nginxNode = nginxNodeService.getById(nodeId);
        if(nginxNode == null){
            return JsonReturn.errorMsg("未找到该节点");
        }
        String url = String.format(API_GET_CONFIGS, nginxNode.getApiUrl());
        Map<String, String> params = MoudleAuthUtil.getMoudleAuthParam();
        params.put("fname", configName);
        HttpResult resp = HttpUtil.postMethod(url,params,"utf-8");
        if("200".equals(resp.getStatuscodes())){
            try {
                String html = resp.getResponseHtml();
                JSONObject res = JSON.parseObject(html);
                if(res.getInteger("result") == 1){
                    JSONObject configs = res.getJSONObject("data");
                    return JsonReturn.success(configs);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }

    /**
     * 获取nginx进程信息
     * @param nginxNode
     * @return
     */
    public JsonReturn getNginxProcess(TNginxNode nginxNode) {
        String url = String.format(API_GET_PROCESS, nginxNode.getApiUrl());
        HttpResult resp = HttpUtil.postMethod(url,MoudleAuthUtil.getMoudleAuthParam(),"utf-8");
        if("200".equals(resp.getStatuscodes())){
            try {
                String html = resp.getResponseHtml();
                JSONObject res = JSON.parseObject(html);
                if(res.getInteger("result") == 1){
                    List<String> process = Arrays.asList(res.getString("data").split("\n"));
                    process = process.stream().map(s -> s.trim()).collect(Collectors.toList());
                    return JsonReturn.success(process);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }

    /**
     * 操作nginx
     * @param apiUrl
     * @param action start,stop,reload,test
     * @return
     */
    public JsonReturn nginxAction(String apiUrl,String action,Integer nodeId) {
        if(nodeId != null){
            TNginxNode nginxNode = nginxNodeService.getById(nodeId);
            if(nginxNode == null){
                return JsonReturn.errorMsg("未找到该节点");
            }
            apiUrl = nginxNode.getApiUrl();
        }
        String url = null;
        switch (action){
            case "start":
                url = String.format(API_START_NGINX, apiUrl);break;
            case "stop":
                url = String.format(API_STOP_NGINX, apiUrl);break;
            case "reload":
                url = String.format(API_RELOAD_NGINX, apiUrl);break;
            case "test":
                url = String.format(API_TEST_NGINX, apiUrl);break;
        }
        if(StringUtils.isBlank(url)){
            return JsonReturn.errorMsg("没有对应操作");
        }

        HttpResult resp = HttpUtil.postMethod(url,MoudleAuthUtil.getMoudleAuthParam(),"utf-8");
        if("200".equals(resp.getStatuscodes())){
            try {
                String html = resp.getResponseHtml();
                JSONObject res = JSON.parseObject(html);
                if(res.getInteger("result") == 1){
                    return JsonReturn.successMsg(res.getString("msg"));
                }else {
                    return JsonReturn.errorMsg(res.getString("msg"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }

    /**
     * 更新nginx配置文件
     * @param nodeId
     * @param fname
     * @param content
     * @return
     */
    public JsonReturn updateNginxConfig(Integer nodeId,String fname,String content){
        TNginxNode nginxNode = nginxNodeService.getById(nodeId);
        if(nginxNode == null){
            return JsonReturn.errorMsg("未找到该节点");
        }
        String url = String.format(API_UPDATE_CONFIG, nginxNode.getApiUrl());
        Map<String, String> param = MoudleAuthUtil.getMoudleAuthParam();
        param.put("fname",fname);
        param.put("content",URLEncoder.encode(content));
        HttpResult resp = HttpUtil.postMethod(url,param,"utf-8");
        if("200".equals(resp.getStatuscodes())){
            String html = resp.getResponseHtml();
            JSONObject res = JSON.parseObject(html);
            if(res.getInteger("result") == 1){
                return JsonReturn.successMsg(res.getString("msg"));
            }else {
                return JsonReturn.errorMsg(res.getString("msg"));
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }

    /**
     * 添加nginx文件
     * @param nodeId
     * @param fname
     * @param content
     * @return
     */
    public JsonReturn createNginxConfig(Integer nodeId,String fname,String content) {
        if(StringUtils.isBlank(fname)){
            return JsonReturn.errorMsg("文件名不能为空");
        }
        if(StringUtils.isBlank(content)){
            return JsonReturn.errorMsg("文件内容不能为空");
        }
        TNginxNode nginxNode = nginxNodeService.getById(nodeId);
        if(nginxNode == null){
            return JsonReturn.errorMsg("未找到该节点");
        }
        String url = String.format(API_CREATE_CONFIG, nginxNode.getApiUrl());
        Map<String, String> param = MoudleAuthUtil.getMoudleAuthParam();
        param.put("fname",fname);
        param.put("content",URLEncoder.encode(content));
        HttpResult resp = HttpUtil.postMethod(url,param,"utf-8");
        if("200".equals(resp.getStatuscodes())){
            String html = resp.getResponseHtml();
            JSONObject res = JSON.parseObject(html);
            if(res.getInteger("result") == 1){
                return JsonReturn.successMsg(res.getString("msg"));
            }else {
                return JsonReturn.errorMsg(res.getString("msg"));
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }

    public JsonReturn createNginxConfigByTemplate(String fname, HttpServletRequest request) {
        String nodeIds = request.getParameter("nodeIds");
        String serverName = request.getParameter("serverName");
        String serverPort = request.getParameter("serverPort");
        String upstreamName = request.getParameter("upstreamName");
        String upstreams = request.getParameter("upstreams");
        if(StringUtils.isAnyBlank(serverName,serverPort,upstreamName,upstreams)){
            return JsonReturn.errorMsg("参数不能为空");
        }
        TSystemParam param = systemParamService.lambdaQuery().eq(TSystemParam::getParamId, "NGINX_CONFIG_TEMPLATE").one();
        String config = param.getParamValue();
        config = config.replaceAll("\\{\\$server_name}",serverName);
        config = config.replaceAll("\\{\\$upstream}",upstreamName);
        config = config.replaceAll("\\{\\$port}",serverPort);
        config = config.replaceAll("\\{\\$port}",serverPort);
        List<JSONObject> nodes = JSON.parseArray(upstreams).toJavaList(JSONObject.class);
        ArrayList<String> upstream_list = new ArrayList<>();
        for (JSONObject node : nodes) {
            upstream_list.add(String.format("\tserver %s:%s;", node.getString("host"), node.getString("port")));
        }
        config = config.replaceAll("\\{\\$upstream_list}",String.join("\n",upstream_list));
        //写入文件
        for (String nodeId : nodeIds.split(",")) {
            Integer id = Integer.valueOf(nodeId);
            if(id != null){
                JsonReturn jr = createNginxConfig(id, fname, config);
                if(!jr.isSuccess()){
                    return jr;
                }
            }
        }
        return JsonReturn.successMsg("添加成功");
    }

    /**
     * 删除nginx配置文件
     * @param nodeId
     * @param fname
     * @return
     */
    public JsonReturn deleteNginxConfig(Integer nodeId,String fname){
        TNginxNode nginxNode = nginxNodeService.getById(nodeId);
        if(nginxNode == null){
            return JsonReturn.errorMsg("未找到该节点");
        }
        String url = String.format(API_DELETE_CONFIG, nginxNode.getApiUrl());
        Map<String, String> param = MoudleAuthUtil.getMoudleAuthParam();
        param.put("fname",fname);
        HttpResult resp = HttpUtil.postMethod(url,param,"utf-8");
        if("200".equals(resp.getStatuscodes())){
            String html = resp.getResponseHtml();
            JSONObject res = JSON.parseObject(html);
            if(res.getInteger("result") == 1){
                return JsonReturn.successMsg(res.getString("msg"));
            }else {
                return JsonReturn.errorMsg(res.getString("msg"));
            }
        }
        return JsonReturn.errorMsg(String.format("接口未正确响应(%s)", resp.getStatuscodes()));
    }
}
