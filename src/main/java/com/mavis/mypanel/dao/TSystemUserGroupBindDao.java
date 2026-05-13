package com.mavis.mypanel.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.mavis.mypanel.entity.TSystemUserGroupBind;

/**
 * (TSystemUserGroupBind)表数据库访问层
 *
 * @author 
 * @since 2024-11-19 19:43:19
 */
public interface TSystemUserGroupBindDao extends BaseMapper<TSystemUserGroupBind> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TSystemUserGroupBind> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TSystemUserGroupBind> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TSystemUserGroupBind> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TSystemUserGroupBind> entities);

}

