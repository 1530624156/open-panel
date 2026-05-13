package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * (TUnit)表实体类
 *
 * @author 
 * @since 2025-02-25 17:26:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TUnit extends Model<TUnit> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String aliasName;

    private String tag;

    private Date insertTime;

    private Date updateTime;

    private String remark;

    /**
     * 租户类型 UnitTypeEnum
     */
    private Integer type;

    /**
     * 省
     */
    private String province;

}

