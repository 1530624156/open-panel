package com.mavis.mypanel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mavis.mypanel.entity.TServer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (TServer)表数据库访问层
 *
 * @author 
 * @since 2025-02-11 13:43:58
 */
public interface TServerDao extends BaseMapper<TServer> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TServer> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TServer> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TServer> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TServer> entities);

}

