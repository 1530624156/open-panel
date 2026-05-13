package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TJenkinsJobDao;
import com.mavis.mypanel.entity.TJenkinsJob;
import com.mavis.mypanel.service.TJenkinsJobService;
import org.springframework.stereotype.Service;

/**
 * (TJenkinsJob)表服务实现类
 *
 * @author 
 * @since 2025-05-29 16:50:05
 */
@Service("tJenkinsJobService")
public class TJenkinsJobServiceImpl extends ServiceImpl<TJenkinsJobDao, TJenkinsJob> implements TJenkinsJobService {

}

