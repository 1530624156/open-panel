package com.mavis.mypanel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mavis.mypanel.entity.TRegistry;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (TRegistry)表数据库访问层
 *
 * @author 
 * @since 2024-08-21 17:32:08
 */
public interface TRegistryDao extends BaseMapper<TRegistry> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TRegistry> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TRegistry> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TRegistry> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TRegistry> entities);

}

