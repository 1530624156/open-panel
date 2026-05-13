package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TJenkinsJob)表实体类
 *
 * @author 
 * @since 2025-05-29 16:50:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TJenkinsJob extends Model<TJenkinsJob> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String jobName;

    private String remark;


}

