package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TServerDockerDao;
import com.mavis.mypanel.entity.TServerDocker;
import com.mavis.mypanel.service.TServerDockerService;
import org.springframework.stereotype.Service;

/**
 * (TServerDocker)表服务实现类
 *
 * @author 
 * @since 2025-02-14 16:37:29
 */
@Service("tServerDockerService")
public class TServerDockerServiceImpl extends ServiceImpl<TServerDockerDao, TServerDocker> implements TServerDockerService {

}

