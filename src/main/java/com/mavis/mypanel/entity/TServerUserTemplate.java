package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TServerUserTemplate)表实体类
 *
 * @author 
 * @since 2025-02-12 18:30:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServerUserTemplate extends Model<TServerUserTemplate> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    /**
     * 账号类型 1-账号密码 2-私钥
     */
    private Integer type;

    private String username;

    private String password;

    private String pemAttachmentUuid;

    private String remark;


}

