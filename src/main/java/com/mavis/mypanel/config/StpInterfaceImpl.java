package com.mavis.mypanel.config;

import cn.dev33.satoken.stp.StpInterface;
import com.mavis.mypanel.logic.UserLogic;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色获取实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserLogic userLogic;
    @Override
    public List<String> getPermissionList(Object loginId, String s) {
        return userLogic.getUserAllPermissions((String) loginId);
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        ArrayList<String> arr = new ArrayList<>();
        return arr;
    }
}
