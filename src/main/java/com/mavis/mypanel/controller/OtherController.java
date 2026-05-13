package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.logic.OtherLogic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("other")
public class OtherController {

    @Resource
    private OtherLogic otherLogic;

    @RequestMapping("provinceList")
    public JsonReturn provinceList(){
        return otherLogic.provinceList();
    }

}
