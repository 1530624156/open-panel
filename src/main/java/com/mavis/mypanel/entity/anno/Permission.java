package com.mavis.mypanel.entity.anno;

import com.mavis.mypanel.entity.enums.PermissionEnum;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Permission {
    PermissionEnum permission();
}