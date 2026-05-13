package com.mavis.mypanel.entity.vo;

import com.mavis.mypanel.entity.TServiceNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务节点Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServiceNodeVo extends TServiceNode {
    private String dockerName;
    private String dockerIp;
    private String serviceAlias;
    private String serviceName;
    private String unitName;
    private String serviceGroupName;
    private Integer serviceGroupId;
}
