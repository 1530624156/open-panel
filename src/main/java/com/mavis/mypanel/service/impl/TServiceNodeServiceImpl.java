package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TServiceNodeDao;
import com.mavis.mypanel.entity.TServiceNode;
import com.mavis.mypanel.entity.vo.TServiceNodeNumVo;
import com.mavis.mypanel.entity.vo.TServiceNodeVo;
import com.mavis.mypanel.service.TServiceNodeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (TServiceNode)表服务实现类
 *
 * @author 
 * @since 2025-03-19 18:46:44
 */
@Service("tServiceNodeService")
public class TServiceNodeServiceImpl extends ServiceImpl<TServiceNodeDao, TServiceNode> implements TServiceNodeService {

    @Override
    public List<TServiceNodeVo> getServiceNodeVo(String unitName, Integer serviceGroupId, String serviceAlias) {
        return this.baseMapper.selectServiceNodeVo(unitName,serviceGroupId,serviceAlias);
    }

    @Override
    public List<TServiceNodeNumVo> getServiceNodeNum() {
        return this.baseMapper.getServiceNodeNum();
    }


}

