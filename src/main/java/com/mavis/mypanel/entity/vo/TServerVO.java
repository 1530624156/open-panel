package com.mavis.mypanel.entity.vo;

import com.mavis.mypanel.entity.TService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServerVO extends TService {

    private String ip;

    private String port;

    private String name;

    private String tagId;

    private boolean reused;
}
