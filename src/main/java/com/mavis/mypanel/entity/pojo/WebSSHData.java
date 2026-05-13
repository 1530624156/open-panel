package com.mavis.mypanel.entity.pojo;

import lombok.Data;

@Data
public class WebSSHData {
    // 操作
    private String operate;
    // 站点信息
    private String tagId;
    // 命令
    private String command = "";

    private Integer cols ;

    private Integer rows ;

    // timestamp
    private Long timestamp;
}
