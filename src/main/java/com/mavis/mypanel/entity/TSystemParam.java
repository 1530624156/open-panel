package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TSystemParam)表实体类
 *
 * @author 
 * @since 2024-12-04 10:12:13
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TSystemParam extends Model<TSystemParam> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String paramId;

    private String paramValue;

    private String paramName;

    public TSystemParam(String paramId, String paramValue, String paramName) {
        this.paramId = paramId;
        this.paramValue = paramValue;
        this.paramName = paramName;
    }
}

