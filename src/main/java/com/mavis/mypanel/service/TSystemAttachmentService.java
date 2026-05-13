package com.mavis.mypanel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mavis.mypanel.entity.TSystemAttachment;

/**
 * (TSystemAttachment)表服务接口
 *
 * @author 
 * @since 2024-11-15 15:20:31
 */
public interface TSystemAttachmentService extends IService<TSystemAttachment> {

    TSystemAttachment getByUuid(String uuid);

}

