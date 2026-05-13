package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.TServer;
import com.mavis.mypanel.entity.TServerDocker;
import com.mavis.mypanel.entity.TServerUserTemplate;
import com.mavis.mypanel.entity.anno.Permission;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.logic.ServerDockerLogic;
import com.mavis.mypanel.logic.ServerLogic;
import com.mavis.mypanel.logic.UserTemplateLogic;
import com.mavis.mypanel.util.MyCacheUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("server")
public class ServerController {

    @Resource
    private ServerLogic serverLogic;

    @Resource
    private UserTemplateLogic userTemplateLogic;

    @Resource
    private ServerDockerLogic serverDockerLogic;

    @RequestMapping("list")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn list(TServer server){
        return serverLogic.list(server);
    }

    @RequestMapping("testServerConn")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn testServerConn(TServer server){
        return serverLogic.testServerConn(server);
    }

    @RequestMapping("saveServer")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn saveServer(TServer server){
        return serverLogic.saveServer(server);
    }

    @RequestMapping("deleteServerById")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn deleteServerById(Integer id){
        return serverLogic.deleteServerById(id);
    }

    @RequestMapping("updateServer")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn updateServer(TServer server){
        return serverLogic.updateServer(server);
    }

    @RequestMapping("getUserTemplateList")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn getUserTemplateList(TServerUserTemplate serverUserTemplate){
        return userTemplateLogic.getUserTemplateList(serverUserTemplate);
    }

    @RequestMapping("addUserTemplate")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn addUserTemplate(TServerUserTemplate serverUserTemplate){
        return userTemplateLogic.addUserTemplate(serverUserTemplate);
    }

    @RequestMapping("deleteUserTemplate")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn deleteUserTemplate(Integer id){
        return userTemplateLogic.deleteUserTemplate(id);
    }
    @RequestMapping("updateUserTemplate")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn updateUserTemplate(TServerUserTemplate serverUserTemplate){
        return userTemplateLogic.updateUserTemplate(serverUserTemplate);
    }

    @RequestMapping("getAllServerInfo")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn getAllServerInfo(){
        return JsonReturn.success(MyCacheUtil.getServerInfoMap());
    }
    @RequestMapping("regetAllServerInfo")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn regetAllServerInfo(){
        MyCacheUtil.setServerInfoMap(null);
        return JsonReturn.success(MyCacheUtil.getServerInfoMap());
    }

    // ================================================== 容器管理 ==================================================

    @RequestMapping("getDockerList")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn getDockerList(TServerDocker serverDocker){
        return serverDockerLogic.getDockerList(serverDocker);
    }

    @RequestMapping("deleteDockerNodeById")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn deleteDockerNodeById(Integer id){
        return serverDockerLogic.deleteDockerNodeById(id);
    }

    @RequestMapping("addDockerNode")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn addDockerNode(TServerDocker serverDocker){
        return serverDockerLogic.addDockerNode(serverDocker);
    }

    @RequestMapping("editDockerNode")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn editDockerNode(TServerDocker serverDocker){
        return serverDockerLogic.editDockerNode(serverDocker);
    }

    @RequestMapping("testDockerNode")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn testDockerNode(TServerDocker serverDocker){
        return serverDockerLogic.testDockerNode(serverDocker);
    }

    @RequestMapping("respanDockerStatus")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn respanDockerStatus(){
        return serverDockerLogic.respanDockerStatus();
    }

    /**
     * 获取docker常见信息
     */
    @RequestMapping("getDockerNodeInfo")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn getDockerNodeInfo(TServerDocker serverDocker){
        return serverDockerLogic.getDockerNodeInfo(serverDocker);
    }

    /**
     * 获取已使用的docker节点端口列表
     */
    @RequestMapping("getDockerNodeUsedPort")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn getDockerNodeUsedPort(Integer id){
        return serverDockerLogic.getDockerNodeUsedPort(id);
    }


    // ================================================== 终端相关 ==================================================
    @RequestMapping("loginSSH")
    @Permission(permission = PermissionEnum.SYSTEM_SERVER)
    public JsonReturn loginSsh(@RequestParam Integer serverId){
        return serverLogic.loginSsh(serverId);
    }
}
