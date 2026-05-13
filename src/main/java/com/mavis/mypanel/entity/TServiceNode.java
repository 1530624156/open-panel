package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TServiceNode)表实体类
 *
 * @author 
 * @since 2025-03-19 18:46:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServiceNode extends Model<TServiceNode> {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer serviceId;

    private Integer dockerId;

    private String containerId;

    private String containerName;

    private String portMap;

    private Integer portWeb;

    private Integer portWebOut;

    private Integer maxCpu;
}

