package com.mavis.mypanel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mavis.mypanel.entity.TServiceNode;
import com.mavis.mypanel.entity.vo.TServiceNodeNumVo;
import com.mavis.mypanel.entity.vo.TServiceNodeVo;

import java.util.List;

/**
 * (TServiceNode)表服务接口
 *
 * @author 
 * @since 2025-03-19 18:46:44
 */
public interface TServiceNodeService extends IService<TServiceNode> {

    List<TServiceNodeVo> getServiceNodeVo(String unitName, Integer serviceGroupId, String serviceAlias);

    List<TServiceNodeNumVo> getServiceNodeNum();
}

