package com.mavis.mypanel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mavis.mypanel.entity.TService;
import com.mavis.mypanel.entity.vo.TServiceVo;

import java.util.List;

/**
 * (TService)表服务接口
 *
 * @author 
 * @since 2025-02-28 18:31:00
 */
public interface TServiceService extends IService<TService> {

    List<TServiceVo> selectServiceVoList(String name, String alias, Integer unitId, Integer groupId);

}

