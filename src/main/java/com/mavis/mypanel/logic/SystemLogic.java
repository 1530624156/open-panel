package com.mavis.mypanel.logic;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.mavis.mypanel.entity.TSystemMenu;
import com.mavis.mypanel.entity.TSystemParam;
import com.mavis.mypanel.entity.TSystemUserGroup;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.entity.vo.MenuVo;
import com.mavis.mypanel.service.TSystemMenuService;
import com.mavis.mypanel.service.TSystemParamService;
import com.mavis.mypanel.service.TSystemUserGroupService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SystemLogic {

    @Resource
    private TSystemMenuService menuService;
    @Resource
    private TSystemUserGroupService usetGroupService;
    @Resource
    private TSystemParamService systemParamService;

    /**
     * 获取菜单
     * @return
     */
    public JsonReturn getMenuList(Boolean getAll, String name) {
        if(getAll == null){
            getAll = false;
        }
        if(StringUtils.isNotBlank(name)){
            return getMenuByName(name);
        }

        QueryWrapper<TSystemMenu> qw = new QueryWrapper<>();
        qw.orderByAsc("sort");
        List<TSystemMenu> list = menuService.list(qw);

        List<String> permissionList = StpUtil.getPermissionList();

        //封装菜单
        ArrayList<MenuVo> menus = new ArrayList<>();
        for (TSystemMenu menu : list) {
            //判断权限
            if(!getAll && StringUtils.isNotBlank(menu.getPermission())){
                if(!permissionList.contains(menu.getPermission())){
                    continue;
                }
            }
            MenuVo menuVo = new MenuVo();
            BeanUtils.copyProperties(menu,menuVo);
            if(menuVo.getPid() == 0){
                //是目录
                //查询pid=当前id的菜单
                List<TSystemMenu> childs = list.stream().filter(item -> menu.getId().equals(item.getPid())).collect(Collectors.toList());
                if(!getAll){
                    //过滤没权限的
                    childs = childs.stream().filter(child ->
                        //菜单没有配置权限的，或者有权限的包含在当前权限中
                        StringUtils.isBlank(child.getPermission()) || permissionList.contains(child.getPermission())
                    ).collect(Collectors.toList());
                }
                menuVo.setSecendMenu(childs);
                menus.add(menuVo);
            }
        }
        return JsonReturn.success(menus);
    }

    /**
     * 根据名称查菜单
     * @param name
     * @return
     */
    private JsonReturn getMenuByName(String name) {
        List<TSystemMenu> list = menuService.lambdaQuery()
                .like(TSystemMenu::getName, name)
                .list();

        return JsonReturn.success(list);
    }


    public JsonReturn getPermissionList() {
        return JsonReturn.success(PermissionEnum.KEY_MAPS);
    }

    /**
     * 给groupId 添加权限列表
     * @param permissions
     * @param groupId
     * @return
     */
    public JsonReturn addGroupPermission(String permissions, Integer groupId) {
        JSONArray arr = JSON.parseArray(permissions);
        TSystemUserGroup group = usetGroupService.getById(groupId);
        if(group == null){
            return JsonReturn.errorMsg("用户组不存在");
        }
        String permissions_now = group.getPermissions();
        JSONArray arr_permissions_now;
        if(StringUtils.isBlank(permissions_now)){
            arr_permissions_now = new JSONArray();
        }else {
            arr_permissions_now = JSON.parseArray(permissions_now);
        }
        for (String permission : arr.toJavaList(String.class)) {
            //判断权限是否已经存在
            if(arr_permissions_now.contains(permission)){
                return JsonReturn.errorMsg(String.format("权限 %s 已存在", permission));
            }else {
                arr_permissions_now.add(permission);
            }
        }
        //更新
        group.setPermissions(arr_permissions_now.toJSONString());
        if(usetGroupService.updateById(group)){
            return JsonReturn.success("添加成功",arr_permissions_now);
        }else {
            return JsonReturn.errorMsg("添加失败");
        }
    }

    /**
     * 移除权限
     * @param permission
     * @param groupId
     * @return
     */
    public JsonReturn removeGroupPermission(String permission, Integer groupId) {
        TSystemUserGroup group = usetGroupService.getById(groupId);
        if(group == null){
            return JsonReturn.errorMsg("用户组不存在");
        }
        String permissions_now = group.getPermissions();
        JSONArray arr = JSON.parseArray(permissions_now);
        if(!arr.contains(permission)){
            return JsonReturn.errorMsg(String.format("权限 %s 不存在", permission));
        }
        arr.remove(permission);
        group.setPermissions(arr.toJSONString());
        if(usetGroupService.updateById(group)){
            return JsonReturn.success("移除成功",arr);
        }else {
            return JsonReturn.errorMsg("移除失败");
        }
    }

    /**
     * 修改菜单
     * @param menu
     * @return
     */
    public JsonReturn menuUpdate(TSystemMenu menu) {
        if(menu.getId() == null){
            return JsonReturn.errorMsg("菜单不存在");
        }
        if(menuService.updateById(menu)){
            return JsonReturn.successMsg("修改成功");
        }else {
            return JsonReturn.errorMsg("修改失败");
        }
    }

    /**
     * 添加菜单
     * @param menu
     * @return
     */
    public JsonReturn menuAdd(TSystemMenu menu) {
        if(menu.getPid() == null){
            menu.setPid(0);
        }
        if(menuService.save(menu)){
            return JsonReturn.successMsg("添加成功");
        }else {
            return JsonReturn.errorMsg("添加失败");
        }
    }

    public JsonReturn menuDelete(Integer id) {
        if(menuService.removeById(id)){
            return JsonReturn.successMsg("删除成功");
        }else {
            return JsonReturn.errorMsg("删除失败");
        }
    }

    //根据配置id获取配置项
    public JsonReturn getParamValue(String paramId) {
        LambdaQueryChainWrapper<TSystemParam> lambdaQuery = systemParamService.lambdaQuery();
        if(StringUtils.isNotBlank(paramId)){
            lambdaQuery.eq(TSystemParam::getParamId, paramId);
        }
        List<TSystemParam> list = lambdaQuery.list();
        return JsonReturn.success(list);
    }

    public JsonReturn updateParamValue(String paramId, String paramValue) {
        TSystemParam param = systemParamService.lambdaQuery().eq(TSystemParam::getParamId, paramId).one();
        param.setParamValue(paramValue);
        if(systemParamService.updateById(param)){
            return JsonReturn.successMsg("修改成功");
        }else {
            return JsonReturn.errorMsg("修改失败");
        }
    }

    /**
     * 获取省份列表
     *
     * @return
     */
    public List<String> provinceList() {
        TSystemParam param = systemParamService.lambdaQuery().eq(TSystemParam::getParamId, "PROVINCE_LIST").one();
        String paramValue = param.getParamValue();
        String[] list = paramValue.split(",");
        List<String> arr = Arrays.asList(list);
        return arr;
    }
}
