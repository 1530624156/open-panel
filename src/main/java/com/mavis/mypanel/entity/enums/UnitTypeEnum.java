package com.mavis.mypanel.entity.enums;


import lombok.Getter;

public enum UnitTypeEnum {

    PERSONAL(1,"个人"),
    TEAM(2,"团队"),
    COMPANY(3,"企业");

    @Getter
    private int code;

    @Getter
    private String name;

    UnitTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
