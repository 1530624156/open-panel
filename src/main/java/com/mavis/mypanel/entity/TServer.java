package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TServer)表实体类
 *
 * @author 
 * @since 2025-02-11 13:43:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServer extends Model<TServer> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String ip;

    private String port;

    private String name;

    private String cpu;

    private String ram;

    private String tags;

    private String remark;

    private Integer userTemplateId;

}

