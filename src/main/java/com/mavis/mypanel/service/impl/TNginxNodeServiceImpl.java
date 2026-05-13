package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TNginxNodeDao;
import com.mavis.mypanel.entity.TNginxNode;
import com.mavis.mypanel.service.TNginxNodeService;
import org.springframework.stereotype.Service;

/**
 * (TNginxNode)表服务实现类
 *
 * @author 
 * @since 2024-11-22 14:07:27
 */
@Service("tNginxNodeService")
public class TNginxNodeServiceImpl extends ServiceImpl<TNginxNodeDao, TNginxNode> implements TNginxNodeService {

}

