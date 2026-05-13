package com.mavis.mypanel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mavis.mypanel.entity.TSystemParam;

/**
 * (TSystemParam)表服务接口
 *
 * @author 
 * @since 2024-12-04 10:12:14
 */
public interface TSystemParamService extends IService<TSystemParam> {

    TSystemParam getByParamId(String paramId);

    boolean updateByParamId(TSystemParam tSystemParam);

}

