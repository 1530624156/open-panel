package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TNginxNode)表实体类
 *
 * @author 
 * @since 2024-11-22 14:07:27
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TNginxNode extends Model<TNginxNode> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String apiUrl;

    private String deployType;

    private String execPath;

    private String confPath;

    private String dockerName;

    private String remark;

    /**
     * nginx服务地址
     */
    private String serviceUrl;

    /**
     * 状态 -1-未知 0-异常 1-正常
     */
    private Integer status;

    /**
     * api版本
     */
    private String apiVersion;

    /**
     * 健康检测路径 /check_status
     */
    private String checkStatus;

    /**
     * check_status 基础验证-用户
     */
    private String checkStatusUsername;

    /**
     * check_status 基础验证-密码
     */
    private String checkStatusPassword;

}

