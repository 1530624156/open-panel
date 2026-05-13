package com.mavis.mypanel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mavis.mypanel.entity.TSystemUserGroup;
import com.mavis.mypanel.entity.vo.UserGroupCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (TSystemUserGroup)表数据库访问层
 *
 * @author 
 * @since 2024-11-19 19:43:18
 */
public interface TSystemUserGroupDao extends BaseMapper<TSystemUserGroup> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TSystemUserGroup> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TSystemUserGroup> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TSystemUserGroup> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TSystemUserGroup> entities);


 List<UserGroupCountVo> selectUserGroupCount();
}

