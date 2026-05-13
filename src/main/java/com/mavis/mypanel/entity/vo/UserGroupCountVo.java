package com.mavis.mypanel.entity.vo;

import com.mavis.mypanel.entity.TSystemUserGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupCountVo extends TSystemUserGroup {
    /**
     * 用户组-用户数
     */
    private Integer count;
}
