package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.anno.Permission;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.logic.AttachmentLogic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("attachment")
public class AttachmentController {

    @Resource
    private AttachmentLogic attachmentLogic;

    @RequestMapping("upload")
    @Permission(permission = PermissionEnum.SYSTEM_MENU)
    public JsonReturn upload(MultipartFile file){
         return attachmentLogic.upload(file);
    }

    @RequestMapping("downloadByUuid")
    public void downloadByUuid(String uuid, HttpServletResponse response){
        attachmentLogic.downloadByUuid(uuid,response);
    }
}
