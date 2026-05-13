package com.mavis.mypanel.logic;

import com.mavis.mypanel.entity.vo.JsonReturn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OtherLogic {

    @Resource
    private SystemLogic systemLogic;


    public JsonReturn provinceList() {
        return JsonReturn.success(systemLogic.provinceList());
    }
}
