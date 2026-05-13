package com.mavis.mypanel.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.mavis.mypanel.entity.TSystemMenu;

/**
 * (TSystemMenu)表数据库访问层
 *
 * @author 
 * @since 2024-11-15 13:48:36
 */
public interface TSystemMenuDao extends BaseMapper<TSystemMenu> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TSystemMenu> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TSystemMenu> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TSystemMenu> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TSystemMenu> entities);

}

