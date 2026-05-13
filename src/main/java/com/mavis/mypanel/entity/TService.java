package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TService)表实体类
 *
 * @author 
 * @since 2025-02-28 18:31:00
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TService extends Model<TService> {


    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer unitId;

    private Integer groupId;

    private String alias;

    private String name;

    private String imageName;

    /**
     * 服务端口数组（需要映射的端口）
     */
    private String portList;

    /**
     * web服务端口（默认服务端口）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer portWeb;

    private Integer maxCpu;

    private String mountMap;
}

