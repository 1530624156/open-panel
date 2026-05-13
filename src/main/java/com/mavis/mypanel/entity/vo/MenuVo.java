package com.mavis.mypanel.entity.vo;

import com.mavis.mypanel.entity.TSystemMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuVo extends TSystemMenu {
    private List<TSystemMenu> secendMenu;
}
