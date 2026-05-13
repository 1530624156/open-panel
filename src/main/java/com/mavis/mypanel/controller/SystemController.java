package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.TSystemMenu;
import com.mavis.mypanel.entity.anno.Permission;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.logic.SystemLogic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("system")
public class SystemController {


    @Resource
    private SystemLogic systemLogic;

    @RequestMapping("menu/list")
    public JsonReturn menuList(Boolean getAll,String name) {
        return systemLogic.getMenuList(getAll,name);
    }

    @RequestMapping("menu/update")
    @Permission(permission = PermissionEnum.SYSTEM_MENU)
    public JsonReturn menuUpdate(TSystemMenu menu) {
        return systemLogic.menuUpdate(menu);
    }

    @RequestMapping("menu/add")
    @Permission(permission = PermissionEnum.SYSTEM_MENU)
    public JsonReturn menuAdd(TSystemMenu menu) {
        return systemLogic.menuAdd(menu);
    }
    @RequestMapping("menu/delete")
    @Permission(permission = PermissionEnum.SYSTEM_MENU)
    public JsonReturn menuDelete(Integer id) {
        return systemLogic.menuDelete(id);
    }

    /**
     * 获取权限列表
     */
    @RequestMapping("permission/list")
    public JsonReturn permissionList() {
        return systemLogic.getPermissionList();
    }

    /**
     * 添加group的权限
     * @param permissions
     * @param groupId
     * @return
     */
    @RequestMapping("permission/addGroupPermission")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn addGroupPermission(String permissions, Integer groupId) {
        return systemLogic.addGroupPermission(permissions, groupId);
    }

    /**
     * 删除group的权限
     */
    @RequestMapping("permission/removeGroupPermission")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn removeGroupPermission(String permission, Integer groupId) {
        return systemLogic.removeGroupPermission(permission, groupId);
    }

    //系统配置
    @RequestMapping("getParamValue")
    @Permission(permission = PermissionEnum.SYSTEM_MENU)
    public JsonReturn getParamValue(String paramId) {
        return systemLogic.getParamValue(paramId);
    }

    @RequestMapping("updateParamValue")
    @Permission(permission = PermissionEnum.SYSTEM_MENU)
    public JsonReturn updateParamValue(String paramId,String paramValue) {
        return systemLogic.updateParamValue(paramId,paramValue);
    }


}
