package com.mavis.mypanel.entity.vo;

import com.mavis.mypanel.entity.TSystemUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupVo extends TSystemUser {

    /**
     * 用户所有用户组
     */
    private String groups;
}
