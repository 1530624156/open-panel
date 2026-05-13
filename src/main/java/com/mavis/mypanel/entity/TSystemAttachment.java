package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TSystemAttachment)表实体类
 *
 * @author 
 * @since 2024-11-15 15:20:31
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TSystemAttachment extends Model<TSystemAttachment> {

    private String uuid;

    private Long size;

    private String filename;

    private String suffix;

    private Long createBy;

    /**
     * 文件哈希值
     */
    private String md5;

}

