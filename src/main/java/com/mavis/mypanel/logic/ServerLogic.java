package com.mavis.mypanel.logic;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.mavis.mypanel.entity.TServer;
import com.mavis.mypanel.entity.TServerUserTemplate;
import com.mavis.mypanel.entity.TSystemAttachment;
import com.mavis.mypanel.entity.pojo.SSHConnectInfo;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.entity.vo.TServerVO;
import com.mavis.mypanel.service.TServerService;
import com.mavis.mypanel.service.TSystemAttachmentService;
import com.mavis.mypanel.service.TTerminalService;
import com.mavis.mypanel.util.ShellUtil;
import com.mavis.mypanel.util.StaticUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 主机逻辑层
 */
@Component
public class ServerLogic {

    @Resource
    private TServerService tServerService;

    @Resource
    private TSystemAttachmentService attachmentService;

    @Resource
    private UserTemplateLogic userTemplateLogic;


    public JsonReturn list(TServer server) {
        LambdaQueryChainWrapper<TServer> qw = tServerService.lambdaQuery();
        if(StringUtils.isNotBlank(server.getName())){
            qw.like(TServer::getName, server.getName());
        }
        if(StringUtils.isNotBlank(server.getIp())){
            qw.like(TServer::getIp, server.getIp());
        }
        List<TServer> servers = qw.list();
        return JsonReturn.success(servers);
    }

    public JsonReturn testServerConn(TServer server) {
        String ip = server.getIp();
        String port = server.getPort();
        if(StringUtils.isBlank(ip) || StringUtils.isBlank(port)){
            return JsonReturn.errorMsg("ip或端口不能为空");
        }
        Session session = getSshSessionByServer(server);
        if(session != null){
            if(session.isConnected()){
                JSONObject info = getServerAllInfo(session);
                session.disconnect();
                return JsonReturn.success("连接成功",info);
            }
        }
        return JsonReturn.errorMsg("连接失败");
    }

    public JsonReturn saveServer(TServer server) {
        Long c = tServerService.lambdaQuery().eq(TServer::getIp, server.getIp()).count();
        if(c > 0){
            return JsonReturn.errorMsg("该主机已存在");
        }
        return tServerService.save(server) ? JsonReturn.successMsg("保存成功") : JsonReturn.errorMsg("保存失败");
    }

    public JsonReturn deleteServerById(Integer id) {
        return tServerService.removeById(id) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    public JsonReturn updateServer(TServer server) {
        return tServerService.updateById(server) ? JsonReturn.successMsg("修改成功") : JsonReturn.errorMsg("修改失败");
    }

    /**
     * 根据server，自动判断连接方式连接ssh
     * @param server
     * @return
     */
    public Session getSshSessionByServer(TServer server){
        Integer userTemplateId = server.getUserTemplateId();
        TServerUserTemplate userTemplate = userTemplateLogic.getById(userTemplateId);
        if(userTemplate == null){
            return null;
        }
        if(userTemplate.getType() == 1){
            //账号密码
            return ShellUtil.getSshSession(server.getIp(), server.getPort(), userTemplate.getUsername(), userTemplate.getPassword());
        }else {
            //秘钥
            //拼接私钥文件路径
            TSystemAttachment attachment = attachmentService.getByUuid(userTemplate.getPemAttachmentUuid());
            if(attachment == null){
                return null;
            }
            String filePath = StaticUtil.mypanel_save_path + attachment.getUuid() + attachment.getSuffix();
            return ShellUtil.getSshSession(server.getIp(), server.getPort(), userTemplate.getUsername(), filePath, userTemplate.getPassword());
        }
    }

    /**
     * 获取单个服务器的所有信息
     */
    public JSONObject getServerAllInfo(TServer server){
        Session session = getSshSessionByServer(server);
        return getServerAllInfo(session);
    }
    /**
     * 获取单个服务器的所有信息
     */
    public JSONObject getServerAllInfo(Session session){
        if(session == null){
            return null;
        }
        try {
            JSONObject res = new JSONObject();
            String ram_total = ShellUtil.sshShell(session, "free | grep \"Mem\" | awk '{print $2}'").trim();
            String ram_used = ShellUtil.sshShell(session, "free | grep \"Mem\" | awk '{print $3}'").trim();
            String cpu = StringUtils.substringBetween(ShellUtil.sshShell(session, "lscpu"), "CPU(s):", "\n");
            if(StringUtils.isBlank(cpu)){
                cpu = StringUtils.substringBetween(ShellUtil.sshShell(session, "lscpu"), "CPU:", "\n");
            }
            String cpu_num = cpu.trim();
            String cpu_used = ShellUtil.sshShell(session, "top -bn 1 | grep \"Cpu(s)\" | awk '{print 100 - $8 \"%\"}'").trim();
            res.put("ram_total", ram_total);
            res.put("ram_used", ram_used);
            res.put("cpu_num", cpu_num);
            res.put("cpu_used", cpu_used.replaceAll("%",""));
            session.disconnect();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取所有服务器的所有信息
     */
    public HashMap<Integer, JSONObject> getAllServerInfo() {
        List<TServer> servers = tServerService.list();
        CountDownLatch countDownLatch = new CountDownLatch(servers.size());
        HashMap<Integer, JSONObject> serverId_info_map = new HashMap<>();
        for (TServer server : servers) {
            new Thread(() -> {
                JSONObject info = getServerAllInfo(server);
                if(info != null){
                    serverId_info_map.put(server.getId(), info);
                }
                countDownLatch.countDown();
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return serverId_info_map;
    }

    /**
     * 登录 ssh
     * @param serverId
     * @return
     */
    public JsonReturn loginSsh(Integer serverId) {
        // 获取主机基本信息 Ip port 账号模板等
        TServer server = tServerService.getById(serverId);
        String serverKey = server.getIp() + ":" + server.getPort();

        // 检查是否已有可复用的连接
        SSHConnectInfo existingInfo = TTerminalService.webSshMap.get(serverKey);
        if (existingInfo != null && existingInfo.getSession() != null && existingInfo.getSession().isConnected()) {
            // 复用已有连接
            String tagId = IdUtil.simpleUUID();
            TTerminalService.webLoginMap.put(tagId, server);
            existingInfo.setTagId(tagId);

            TServerVO tServerVO = new TServerVO();
            tServerVO.setIp(server.getIp());
            tServerVO.setPort(server.getPort());
            tServerVO.setName(server.getName());
            tServerVO.setTagId(tagId);
            tServerVO.setReused(true);
            return JsonReturn.success("复用已有连接", tServerVO);
        }

        try {
            Session session  = getSshSessionByServer(server);
            String tagId = IdUtil.simpleUUID();
            TTerminalService.webLoginMap.put(tagId, server);
            // 会话共用技术
            SSHConnectInfo sshConnectInfo = new SSHConnectInfo();
            sshConnectInfo.setSession(session);
            sshConnectInfo.setTagId(tagId);
            TTerminalService.webSshMap.put(serverKey, sshConnectInfo);
            // 返回 tagId 和 server 信息
            TServerVO tServerVO = new TServerVO();
            tServerVO.setIp(server.getIp());
            tServerVO.setPort(server.getPort());
            tServerVO.setName(server.getName());
            tServerVO.setTagId(tagId);
            tServerVO.setReused(false);
            return JsonReturn.success("登录成功", tServerVO);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonReturn.errorMsg("登录失败，" + e.getMessage());
        }

    }
}
