package com.mavis.mypanel.entity.vo;

import com.mavis.mypanel.entity.TService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServiceVo extends TService {
    private String unitName;
    private String groupName;

    /**
     * 节点数量
     */
    private Integer nodeNum;
}
