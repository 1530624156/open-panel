package com.mavis.mypanel.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.mavis.mypanel.entity.TServiceGroup;

/**
 * (TServiceGroup)表数据库访问层
 *
 * @author 
 * @since 2025-03-03 20:37:02
 */
public interface TServiceGroupDao extends BaseMapper<TServiceGroup> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TServiceGroup> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TServiceGroup> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TServiceGroup> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TServiceGroup> entities);

}

