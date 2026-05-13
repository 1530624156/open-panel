package com.mavis.mypanel.entity.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单权限枚举类
 */
public enum PermissionEnum {

    SYSTEM_USER("system:user","用户管理"),
    SYSTEM_MENU("system:menu","菜单管理"),
    SYSTEM_NGINX("system:nginx","nginx管理"),
    SYSTEM_JENKINS("system:jenkins","Jenkins管理"),
    SYSTEM_SERVER("server:server","主机管理"),
    SYSTEM_REGISTRY("server:registry","仓库管理"),
    SYSTEM_SERVICE("system:service","服务管理");


    @Getter
    private String permission;

    @Getter
    private String remark;

    public static final Map<String,String> KEY_MAPS = Arrays.stream(values()).collect(Collectors.toMap(PermissionEnum::getPermission, PermissionEnum::getRemark));

    PermissionEnum(String permission, String remark) {
        this.permission = permission;
        this.remark = remark;
    }

}
