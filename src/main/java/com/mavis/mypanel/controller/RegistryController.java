package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.TRegistry;
import com.mavis.mypanel.entity.anno.Permission;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.logic.RegistryLogic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("registry")
public class RegistryController {
    @Resource
    private RegistryLogic registryLogic;

    @RequestMapping("getRegList")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn getRegList(TRegistry registry){
        return registryLogic.getRegList(registry);
    }

    @RequestMapping("addReg")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn addReg(TRegistry registry){
        return registryLogic.addReg(registry);
    }
    @RequestMapping("delRegById")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn delRegById(Integer id){
        return registryLogic.delRegById(id);
    }

    /**
     * 测试仓库状态
     * @param registry
     * @return
     */
    @RequestMapping("testReg")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn testReg(TRegistry registry){
        return registryLogic.testReg(registry);
    }

    @RequestMapping("editRegById")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn editRegById(TRegistry registry){
        return registryLogic.editRegById(registry);
    }

    @RequestMapping("getMirrorByReg")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn getMirrorByReg(TRegistry registry){
        return registryLogic.getMirrorByReg(registry);
    }
    @RequestMapping("getTagsByRegIdMirror")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn getTagsByRegIdMirror(Integer regId ,String mirror){
        return registryLogic.getTagsByRegIdMirror(regId,mirror);
    }

    /**
     * 获取所有mirror和对应tag信息
     * @param id
     * @return
     */
    @RequestMapping("getAllTagsInfo")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn getAllTagsInfo(Integer id){
        return registryLogic.getAllTagsInfo(id);
    }

    @RequestMapping("deleteMirrorTagsByDigest")
    @Permission(permission = PermissionEnum.SYSTEM_REGISTRY)
    public JsonReturn deleteMirrorTagsByDigest(Integer regId,String mirrorname,String digest){
    	return registryLogic.deleteMirrorTagsByDigest(regId,mirrorname,digest);
    }
}
