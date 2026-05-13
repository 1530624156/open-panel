package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TRegistryDao;
import com.mavis.mypanel.entity.TRegistry;
import com.mavis.mypanel.service.TRegistryService;
import org.springframework.stereotype.Service;

/**
 * (TRegistry)表服务实现类
 *
 * @author 
 * @since 2024-08-21 17:32:08
 */
@Service("tRegistryService")
public class TRegistryServiceImpl extends ServiceImpl<TRegistryDao, TRegistry> implements TRegistryService {

}

