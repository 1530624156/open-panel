package com.mavis.mypanel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mavis.mypanel.entity.TSystemUserGroup;
import com.mavis.mypanel.entity.vo.UserGroupCountVo;

import java.util.List;

/**
 * (TSystemUserGroup)表服务接口
 *
 * @author 
 * @since 2024-11-19 19:43:19
 */
public interface TSystemUserGroupService extends IService<TSystemUserGroup> {

    List<UserGroupCountVo> selectUserGroupCount();
}

