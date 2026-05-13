package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TServerDao;
import com.mavis.mypanel.entity.TServer;
import com.mavis.mypanel.service.TServerService;
import org.springframework.stereotype.Service;

/**
 * (TServer)表服务实现类
 *
 * @author 
 * @since 2025-02-11 13:43:58
 */
@Service("tServerService")
public class TServerServiceImpl extends ServiceImpl<TServerDao, TServer> implements TServerService {

}

