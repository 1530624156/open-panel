package com.mavis.mypanel.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TServerDocker)表实体类
 *
 * @author 
 * @since 2025-02-20 19:41:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServerDocker extends Model<TServerDocker> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String ip;

    private String port;

    private Integer tls;

    private String tlsCaAttachUuid;

    private String tlsCertAttachUuid;

    private String tlsKeyAttachUuid;

    private String remark;

    private Integer status;

    private Integer containerUpNum;

    private Integer containerDownNum;

    private Integer imagesNum;

    private Integer cpuNum;

    private String memTotal;

    private String archType;


}

