package com.mavis.mypanel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mavis.mypanel.entity.TSystemUser;
import com.mavis.mypanel.entity.vo.UserGroupVo;

import java.util.List;

/**
 * (TSystemUser)表服务接口
 *
 * @author 
 * @since 2024-11-15 10:27:59
 */
public interface TSystemUserService extends IService<TSystemUser> {

    public List<UserGroupVo> getUserGroupList(String username,String name);
}

