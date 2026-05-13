package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.TNginxNode;
import com.mavis.mypanel.entity.anno.Permission;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.logic.NginxLogic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("nginx")
public class NginxController {

    @Resource
    private NginxLogic nginxLogic;

    @RequestMapping("getNginxNode")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn getNginxNode(TNginxNode nginxNode){
        return nginxLogic.getNginxNode(nginxNode);
    }


    @RequestMapping("addNginxNode")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn addNginxNode(TNginxNode nginxNode){
        return nginxLogic.addNginxNode(nginxNode);
    }

    @RequestMapping("removeNginxNode")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn removeNginxNode(Integer id){
        return nginxLogic.removeNginxNode(id);
    }

    @RequestMapping("updateNginxNode")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn updateNginxNode(TNginxNode nginxNode){
        return nginxLogic.updateNginxNode(nginxNode);
    }


    /**
     * 根据传入配置测试，用于保存前的测试
     * @param nginxNode
     * @return
     */
    @RequestMapping("testByConfig")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn testByConfig(TNginxNode nginxNode){
        return nginxLogic.testByConfig(nginxNode);
    }

    /**
     * 测试已有节点情况
     * @param id
     * @return
     */
    @RequestMapping("testByNginxNodeId")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn testByNginxNodeId(Integer id){
        return nginxLogic.testByNginxNodeId(id);
    }


    @RequestMapping("getNginxNodeMornitor")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn getNginxNodeMornitor(TNginxNode nginxNode){
        return nginxLogic.getNginxNodeMornitor(nginxNode);
    }

    @RequestMapping("getNginxCheckStatus")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn getNginxCheckStatus(TNginxNode nginxNode){
        return nginxLogic.getNginxCheckStatus(nginxNode);
    }


    @RequestMapping("getNginxConfigs")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn getNginxConfigs(Integer nodeId,String configName){
        return nginxLogic.getNginxConfigs(nodeId,configName);
    }
    @RequestMapping("getServerConfigsNames")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn getServerConfigsNames(Integer nodeId){
        return nginxLogic.getServerConfigsNames(nodeId);
    }

    @RequestMapping("getNginxProcess")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn getNginxProcess(TNginxNode nginxNode){
        return nginxLogic.getNginxProcess(nginxNode);
    }

    @RequestMapping("nginxAction")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn startNginx(String apiUrl, String action,Integer nodeId){
        return nginxLogic.nginxAction(apiUrl,action,nodeId);
    }

    @RequestMapping("updateNginxConfig")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn updateNginxConfig(Integer nodeId,String fname,String content){
        return nginxLogic.updateNginxConfig(nodeId,fname,content);
    }

    /**
     * 创建nginx配置文件
     * @param nodeId
     * @param fname
     * @param content
     * @return
     */
    @RequestMapping("createNginxConfig")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn createNginxConfig(Integer nodeId,String fname,String content){
        return nginxLogic.createNginxConfig(nodeId,fname,content);
    }

    /**
     * 根据模板创建nginx配置文件
     * @param fname
     * @return
     */
    @RequestMapping("createNginxConfigByTemplate")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn createNginxConfigByTemplate(String fname, HttpServletRequest request){
        return nginxLogic.createNginxConfigByTemplate(fname,request);
    }

    @RequestMapping("deleteNginxConfig")
    @Permission(permission = PermissionEnum.SYSTEM_NGINX)
    public JsonReturn deleteNginxConfig(Integer nodeId,String fname){
        return nginxLogic.deleteNginxConfig(nodeId,fname);
    }
}
