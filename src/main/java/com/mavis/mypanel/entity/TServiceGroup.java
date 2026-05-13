package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TServiceGroup)表实体类
 *
 * @author 
 * @since 2025-03-03 20:37:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServiceGroup extends Model<TServiceGroup> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String alias;

    private String remark;
}

