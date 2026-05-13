package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TSystemMenu)表实体类
 *
 * @author 
 * @since 2024-11-15 13:48:36
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TSystemMenu extends Model<TSystemMenu> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String href;

    private String icon;

    private String iconActive;

    private Integer pid;

    private Integer sort;

    /**
     * 菜单权限标识
     */
    private String permission;

}

