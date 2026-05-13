package com.mavis.mypanel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mavis.mypanel.dao.TSystemUserGroupDao;
import com.mavis.mypanel.entity.TSystemUserGroup;
import com.mavis.mypanel.entity.vo.UserGroupCountVo;
import com.mavis.mypanel.service.TSystemUserGroupService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (TSystemUserGroup)表服务实现类
 *
 * @author 
 * @since 2024-11-19 19:43:19
 */
@Service("tSystemUserGroupService")
public class TSystemUserGroupServiceImpl extends ServiceImpl<TSystemUserGroupDao, TSystemUserGroup> implements TSystemUserGroupService {

    @Override
    public List<UserGroupCountVo> selectUserGroupCount() {
        return baseMapper.selectUserGroupCount();
    }
}

