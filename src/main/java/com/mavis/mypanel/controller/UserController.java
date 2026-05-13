package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.TSystemUser;
import com.mavis.mypanel.entity.TSystemUserGroup;
import com.mavis.mypanel.entity.anno.Permission;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.logic.UserLogic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserLogic userLogic;

    @RequestMapping("login")
    public JsonReturn login(String username, String password) {
        return userLogic.login(username, password);
    }

    @RequestMapping("logout")
    public JsonReturn logout(HttpServletResponse response) {
        return userLogic.logout(response);
    }

    @RequestMapping("list")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn list(TSystemUser user) {
        return userLogic.list(user);
    }

    /**
     * 获取用户列表，带groups
     * @return
     */
    @RequestMapping("getUserGroupList")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn getUserGroupList(String username,String name) {
        return userLogic.getUserGroupList(username,name);
    }


    @RequestMapping("addUser")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn addUser(TSystemUser user) {
        return userLogic.addUser(user);
    }

    @RequestMapping("deleteById")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn deleteById(Integer id) {
        return userLogic.deleteById(id);
    }

    @RequestMapping("editUser")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn editUser(TSystemUser user) {
        return userLogic.editUser(user);
    }


    /**
     * 添加一个用户组
     * @param userGroup
     * @return
     */
    @RequestMapping("addUserGroup")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn addUserGroup(TSystemUserGroup userGroup) {
        return userLogic.addUserGroup(userGroup);
    }

    /**
     * 获取用户组
     * @param userGroup
     * @return
     */
    @RequestMapping("getUserGroups")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn getUserGroups(TSystemUserGroup userGroup) {
        return userLogic.getUserGroups(userGroup);
    }

    /**
     * 获取用户组-统计数
     * @return
     */
    @RequestMapping("getUserGroupCount")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn getUserGroupCount() {
        return userLogic.getUserGroupCount();
    }

    @RequestMapping("getUserByGroupId")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn getUserByGroupId(Integer groupId) {
        return userLogic.getUserByGroupId(groupId);
    }

    /**
     * 添加用户到用户组
     * @param userIds userid逗号分隔
     */
    @RequestMapping("addUser2Group")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn addUser2Group(String userIds, Integer groupId) {
        return userLogic.addUser2Group(userIds, groupId);
    }

    /**
     * 移除用户
     */
    @RequestMapping("removeUserFromGroup")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn removeUserFromGroup(Integer userId, Integer groupId) {
        return userLogic.removeUserFromGroup(userId, groupId);
    }

    @RequestMapping("deleteUserGroup")
    @Permission(permission = PermissionEnum.SYSTEM_USER)
    public JsonReturn deleteUserGroup(Integer id) {
        return userLogic.deleteUserGroup(id);
    }
}
