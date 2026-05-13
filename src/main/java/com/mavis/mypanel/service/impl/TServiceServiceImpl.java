package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TServiceDao;
import com.mavis.mypanel.entity.TService;
import com.mavis.mypanel.entity.vo.TServiceVo;
import com.mavis.mypanel.service.TServiceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (TService)表服务实现类
 *
 * @author 
 * @since 2025-02-28 18:31:00
 */
@Service("tServiceService")
public class TServiceServiceImpl extends ServiceImpl<TServiceDao, TService> implements TServiceService {

    @Override
    public List<TServiceVo> selectServiceVoList(String name, String alias, Integer unitId, Integer groupId) {
        return baseMapper.selectServiceVoList(name, alias,unitId,groupId);
    }
}

