package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TRegistry)表实体类
 *
 * @author 
 * @since 2024-08-21 17:32:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TRegistry extends Model<TRegistry> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String url;

    private String username;

    private String password;

    /**
     * 仓库状态
     */
    private Integer status;

    private String host;
}

