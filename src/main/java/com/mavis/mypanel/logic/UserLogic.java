package com.mavis.mypanel.logic;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mavis.mypanel.entity.TSystemUser;
import com.mavis.mypanel.entity.TSystemUserGroup;
import com.mavis.mypanel.entity.TSystemUserGroupBind;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.service.TSystemUserGroupBindService;
import com.mavis.mypanel.service.TSystemUserGroupService;
import com.mavis.mypanel.service.TSystemUserService;
import com.mavis.mypanel.util.QueryWrapperBuffer;
import com.mavis.mypanel.util.StaticUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserLogic {

    @Resource
    private TSystemUserService userService;

    @Resource
    private TSystemUserGroupService userGroupService;

    @Resource
    private TSystemUserGroupBindService userGroupBindService;

    public JsonReturn login(String username, String password) {
        QueryWrapper<TSystemUser> qw = new QueryWrapper<>();
        qw.eq("username", username);
        TSystemUser user = userService.getOne(qw);
        if (user == null) {
            return JsonReturn.errorMsg("用户名不存在");
        }
        if (!user.getPassword().equals(password)) {
            return JsonReturn.errorMsg("密码错误");
        }

        //登录
        StpUtil.login(user.getId());
        return JsonReturn.success("登录成功", user);
    }

    /**
     * 获取用户
     * @param user
     * @return
     */
    public JsonReturn list(TSystemUser user) {
        QueryWrapper qw = QueryWrapperBuffer.getByNotNullField(user);
        return JsonReturn.success(userService.list(qw));
    }

    /**
     * 添加用户
     * @param user
     * @return
     */
    public JsonReturn addUser(TSystemUser user) {
        long c = userService.count(new QueryWrapper<TSystemUser>().eq("username", user.getUsername()));
        if(c > 0){
            return JsonReturn.errorMsg("用户名已存在");
        }
        boolean f = userService.save(user);
        return f ? JsonReturn.successMsg("添加成功") : JsonReturn.errorMsg("添加失败");
    }

    public JsonReturn deleteById(Integer id) {
        TSystemUser user = userService.getById(id);
        if(user == null){
            return JsonReturn.errorMsg("用户不存在");
        }
        return userService.removeById(id) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    public JsonReturn editUser(TSystemUser user) {
        TSystemUser u = userService.getById(user.getId());
        if(u == null){
            return JsonReturn.errorMsg("用户不存在");
        }
        return userService.updateById(user) ? JsonReturn.successMsg("修改成功") : JsonReturn.errorMsg("修改失败");
    }

    public JsonReturn getUserGroups(TSystemUserGroup userGroup) {
        QueryWrapper<TSystemUserGroup> qw = QueryWrapperBuffer.getByNotNullField(userGroup);
        List<TSystemUserGroup> list = userGroupService.list(qw);
        return JsonReturn.success(list);
    }

    public JsonReturn getUserGroupCount(){
        return JsonReturn.success(userGroupService.selectUserGroupCount());
    }

    public JsonReturn getUserByGroupId(Integer groupId) {
        QueryWrapper<TSystemUserGroupBind> qw = new QueryWrapper<>();
        qw.eq("group_id", groupId);
        qw.select("user_id");
        List<TSystemUserGroupBind> list = userGroupBindService.list(qw);
        if(CollectionUtil.isEmpty(list)){
            return JsonReturn.success(new ArrayList<>());
        }
        List<Integer> userIds = list.stream().map(TSystemUserGroupBind::getUserId).collect(Collectors.toList());

        QueryWrapper<TSystemUser> qw_user = new QueryWrapper<>();
        qw_user.in("id", userIds);
        List<TSystemUser> users = userService.list(qw_user);
        return JsonReturn.success(users);
    }

    /**
     * 为用户组添加用户
     * @param userIds
     * @param groupId
     * @return
     */
    public JsonReturn addUser2Group(String userIds, Integer groupId) {
        String[] uids = userIds.split(",");

        ArrayList<TSystemUserGroupBind> saves = new ArrayList<>();
        for (String uid : uids) {
            Long c = userGroupBindService.lambdaQuery()
                    .eq(TSystemUserGroupBind::getUserId, uid)
                    .eq(TSystemUserGroupBind::getGroupId, groupId)
                    .count();
            if(c > 0){
                TSystemUser u = userService.getById(uid);
                return JsonReturn.errorMsg(String.format("已存在用户[%s]", u.getName()));
            }
            TSystemUserGroupBind bind = new TSystemUserGroupBind();
            bind.setUserId(Integer.valueOf(uid));
            bind.setGroupId(groupId);
            saves.add(bind);
        }
        if(userGroupBindService.saveBatch(saves)){
            return JsonReturn.successMsg("添加成功");
        }else {
            return JsonReturn.errorMsg("添加失败");
        }
    }

    public JsonReturn removeUserFromGroup(Integer userId, Integer groupId) {
        boolean f = userGroupBindService.lambdaUpdate()
                .eq(TSystemUserGroupBind::getUserId, userId)
                .eq(TSystemUserGroupBind::getGroupId, groupId)
                .remove();
        return f ? JsonReturn.successMsg("移出成功") : JsonReturn.errorMsg("移出失败");
    }

    public JsonReturn getUserGroupList(String username,String name) {
        return JsonReturn.success(userService.getUserGroupList(username,name));
    }


    /**
     * 添加一个用户组
     * @param userGroup
     * @return
     */
    public JsonReturn addUserGroup(TSystemUserGroup userGroup) {
        long c = userGroupService.count(new QueryWrapper<TSystemUserGroup>().eq("name", userGroup.getName()));
        if(c > 0){
            return JsonReturn.errorMsg("用户组名已存在");
        }
        boolean f = userGroupService.save(userGroup);
        return f ? JsonReturn.successMsg("添加成功") : JsonReturn.errorMsg("添加失败");
    }

    public List<String> getUserAllPermissions(String userId){
        //获取绑定关系
        List<TSystemUserGroupBind> groupBinds = userGroupBindService.lambdaQuery()
                .eq(TSystemUserGroupBind::getUserId, userId)
                .select(TSystemUserGroupBind::getGroupId).list();
        List<Integer> groupIds = groupBinds.stream().map(TSystemUserGroupBind::getGroupId).collect(Collectors.toList());
        //查这些group信息
        List<TSystemUserGroup> groups = userGroupService.lambdaQuery()
                .in(TSystemUserGroup::getId, groupIds)
                .select(TSystemUserGroup::getPermissions)
                .list();
        //所有权限拼在一起
        ArrayList<String> arr = new ArrayList<>();
        for (TSystemUserGroup group : groups) {
            List<String> groupPermission = JSON.parseArray(group.getPermissions()).toJavaList(String.class);
            arr.addAll(groupPermission);
        }
        List<String> list = arr.stream().distinct().collect(Collectors.toList());
        return list;
    }

    /**
     * 删除用户组
     * @param groupid
     * @return
     */
    public JsonReturn deleteUserGroup(Integer groupid) {
        Long c = userGroupBindService.lambdaQuery()
                .eq(TSystemUserGroupBind::getGroupId, groupid)
                .count();
        if(c > 0){
            return JsonReturn.errorMsg("该用户组下有用户，无法删除");
        }
        return userGroupService.removeById(groupid) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    /**
     * 用户登出
     * @return
     */
    public JsonReturn logout(HttpServletResponse response) {
        StpUtil.logout();

        //判断有没有二级路径
        String secend = StaticUtil.getProp("server.servlet.context-path");
        try {
            if(StringUtils.isNotBlank(secend)){
                response.sendRedirect(secend+"/index.html");
            }else {
                response.sendRedirect("/index.html");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
