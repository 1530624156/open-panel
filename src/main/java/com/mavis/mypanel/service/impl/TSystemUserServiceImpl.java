package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TSystemUserDao;
import com.mavis.mypanel.entity.TSystemUser;
import com.mavis.mypanel.entity.vo.UserGroupVo;
import com.mavis.mypanel.service.TSystemUserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (TSystemUser)表服务实现类
 *
 * @author 
 * @since 2024-11-15 10:27:59
 */
@Service("tSystemUserService")
public class TSystemUserServiceImpl extends ServiceImpl<TSystemUserDao, TSystemUser> implements TSystemUserService {

    @Override
    public List<UserGroupVo> getUserGroupList(String username,String name) {
        return baseMapper.getUserGroupList(username,name);
    }
}

