package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TSystemAttachmentDao;
import com.mavis.mypanel.entity.TSystemAttachment;
import com.mavis.mypanel.service.TSystemAttachmentService;
import org.springframework.stereotype.Service;

/**
 * (TSystemAttachment)表服务实现类
 *
 * @author 
 * @since 2024-11-15 15:20:31
 */
@Service("tSystemAttachmentService")
public class TSystemAttachmentServiceImpl extends ServiceImpl<TSystemAttachmentDao, TSystemAttachment> implements TSystemAttachmentService {

    @Override
    public TSystemAttachment getByUuid(String uuid) {
        return this.lambdaQuery().eq(TSystemAttachment::getUuid, uuid).one();
    }
}

