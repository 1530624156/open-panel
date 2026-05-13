package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TSystemUserGroupBind)表实体类
 *
 * @author 
 * @since 2024-11-19 19:43:19
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TSystemUserGroupBind extends Model<TSystemUserGroupBind> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer groupId;


}

