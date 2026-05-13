package com.mavis.mypanel.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * @program: cx-mirror
 * @description: mybatis的拦截器类，用于打印sql语句，以及sql执行时间等信息，方便调试和优化sql语句，提高sql执行效率
 * @author: LiWei
 * @create: 2023-10-23 21:44
 **/
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class})
})
public class SqlLogInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0]; // 获取MappedStatement
        Object parameter = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;// 获取参数
        String sqlId = mappedStatement.getId(); // 获取sql语句的id
        BoundSql boundSql = mappedStatement.getBoundSql(parameter); // 获取BoundSql
        Configuration configuration = mappedStatement.getConfiguration(); // 获取Configuration
        Object returnValue;
        String sql = generateSql(configuration, boundSql); // 获取sql语句

        long end;
        long start;

        try {
            start = System.currentTimeMillis();
            returnValue = invocation.proceed();
            end = System.currentTimeMillis();
        } catch (InvocationTargetException | IllegalAccessException e) {
            logSqlError(sqlId, sql, e);
            return null;
        }

        logSql(sqlId, sql, returnValue, end - start, mappedStatement);


        return returnValue;
    }


    private void logSql(String sqlId, String sql, Object returnValue, long time, MappedStatement mappedStatement) {
        long count;
        String resultType = "";

        if (returnValue instanceof Number) {
            count = ((Number) returnValue).longValue();
            SqlCommandType commandType = mappedStatement.getSqlCommandType();

            if (SqlCommandType.INSERT.equals(commandType)) {
                resultType = "==> 插入了 \033[33m%s\033[m 条数据\n";
            } else if (SqlCommandType.UPDATE.equals(commandType)) {
                resultType = "==> 更新了 \033[33m%s\033[m 条数据\n";
            } else if (SqlCommandType.DELETE.equals(commandType)) {
                resultType = "==> 删除了 \033[33m%s\033[m 条数据\n";
            }
        } else {
            List<?> resultList = (List<?>) returnValue;
            if (resultList.size() == 1 && resultList.get(0) instanceof Number) {
                count = ((Number) resultList.get(0)).longValue();
                resultType = "==> 查询到一条数值为 \033[33m%s\033[m\n";
            } else {
                count = resultList.size();
                resultType = "==> 查询到 \033[33m%s\033[m 条数据\n";
            }
        }

        System.out.println();
        log.trace("\n" +
                "\033[32m----------------------------------SqlLogs ----------------------------------------\033[m\n" +
                String.format("==> 调用方法 : %s\n", sqlId) +
                String.format("==> sql : \033[33m%s\033[m\n", sql) +
                String.format(resultType, count) +
                String.format("==> 耗时 : %dms\n", time) +
                "\033[32m----------------------------------SqlLogs ----------------------------------------\033[m\n");
    }

    /**
     * 打印sql错误
     *
     * @param sqlId sqlId
     * @param sql   sql语句
     * @param e     异常
     */
    private void logSqlError(String sqlId, String sql, ReflectiveOperationException e) {
        log.error("\n" +
                "\033[31m----------------------------------SqlLogs ----------------------------------------\n" +
                "==> 调用方法 : " + sqlId + "\n" +
                "==> sql : " + sql + "\n" +
                "==> sql异常 : " + e.getClass().getSimpleName() + "\n" +
                "----------------------------------SqlLogs ----------------------------------------\033[m\n\n", e);
    }


    /**
     * 生成sql语句
     *
     * @param configuration 配置
     * @param boundSql      绑定的sql
     * @return sql语句
     */
    private static String generateSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject(); // 获取参数
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings(); // 获取参数映射
        String sql = boundSql.getSql().replaceAll("[\\s]+", " "); // 获取sql语句

        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            MetaObject metaObject = configuration.newMetaObject(parameterObject);

            for (ParameterMapping parameterMapping : parameterMappings) {
                String propertyName = parameterMapping.getProperty();

                if (metaObject.hasGetter(propertyName)) {
                    Object obj = metaObject.getValue(propertyName);
                    sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                } else if (boundSql.hasAdditionalParameter(propertyName)) {
                    Object obj = boundSql.getAdditionalParameter(propertyName);
                    sql = sql.replaceFirst("\\?", getParameterValue(obj));
                }
            }
        }

        // 将SQL语句转换为小写
        sql = sql.toLowerCase();
        // 使用正则表达式在逗号后面添加一个空格
        sql = sql.replaceAll(",(\\S)", ", $1");
        // 使用正则表达式替换多余的空格
        sql = sql.replaceAll("[\\s]+", " ");
        return sql;
    }

    private static String getParameterValue(Object obj) {
        if (obj == null) {
            return "NULL";
        } else if (obj instanceof String || obj instanceof Character) {
            return "'" + obj + "'";
        } else if (obj instanceof Date) {
            // 使用数据库支持的日期格式，例如 MySQL 的 'yyyy-MM-dd HH:mm:ss'
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "'" + formatter.format((Date) obj) + "'";
        } else {
            return obj.toString();
        }
    }


    /**
     * 获取参数的值
     *
     * @param target 参数
     * @return 参数的值
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置属性
     *
     * @param properties 属性
     */
    @Override
    public void setProperties(Properties properties) {
        // Handle properties if needed
    }
}
