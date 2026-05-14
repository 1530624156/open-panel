package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TSyslogDao;
import com.mavis.mypanel.entity.TSyslog;
import com.mavis.mypanel.service.TSyslogService;
import org.springframework.stereotype.Service;

@Service("tSyslogService")
public class TSyslogServiceImpl extends ServiceImpl<TSyslogDao, TSyslog> implements TSyslogService {
}
