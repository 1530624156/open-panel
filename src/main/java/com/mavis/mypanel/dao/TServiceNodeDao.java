package com.mavis.mypanel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mavis.mypanel.entity.TServiceNode;
import com.mavis.mypanel.entity.vo.TServiceNodeNumVo;
import com.mavis.mypanel.entity.vo.TServiceNodeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (TServiceNode)表数据库访问层
 *
 * @author 
 * @since 2025-03-19 18:46:44
 */
public interface TServiceNodeDao extends BaseMapper<TServiceNode> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TServiceNode> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TServiceNode> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TServiceNode> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TServiceNode> entities);

    List<TServiceNodeVo> selectServiceNodeVo(@Param("unitName") String unitName, @Param("serviceGroupId") Integer serviceGroupId,@Param("serviceAlias") String serviceAlias);

    List<TServiceNodeNumVo> getServiceNodeNum();
}

