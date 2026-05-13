package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TSystemParamDao;
import com.mavis.mypanel.entity.TSystemParam;
import com.mavis.mypanel.service.TSystemParamService;
import org.springframework.stereotype.Service;

/**
 * (TSystemParam)表服务实现类
 *
 * @author 
 * @since 2024-12-04 10:12:14
 */
@Service("tSystemParamService")
public class TSystemParamServiceImpl extends ServiceImpl<TSystemParamDao, TSystemParam> implements TSystemParamService {

    @Override
    public TSystemParam getByParamId(String paramId) {
        return this.lambdaQuery().eq(TSystemParam::getParamId, paramId).one();
    }

    @Override
    public boolean updateByParamId(TSystemParam tSystemParam) {
        return this.lambdaUpdate().eq(TSystemParam::getParamId, tSystemParam.getParamId()).update(tSystemParam);
    }
}

