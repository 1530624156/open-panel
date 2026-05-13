package com.mavis.mypanel.logic;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.mavis.mypanel.entity.TServerUserTemplate;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.service.TServerUserTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 账号模板逻辑层
 */
@Component
public class UserTemplateLogic {

    @Resource
    private TServerUserTemplateService userTemplateService;


    public JsonReturn getUserTemplateList(TServerUserTemplate serverUserTemplate) {
        LambdaQueryChainWrapper<TServerUserTemplate> qw = userTemplateService.lambdaQuery();
        if(StringUtils.isNotBlank(serverUserTemplate.getName())){
            qw.like(TServerUserTemplate::getName, serverUserTemplate.getName());
        }
        List<TServerUserTemplate> list = qw.list();
        return JsonReturn.success(list);
    }

    public JsonReturn addUserTemplate(TServerUserTemplate serverUserTemplate) {
        LambdaQueryChainWrapper<TServerUserTemplate> qw = userTemplateService.lambdaQuery();
        Long c = qw.eq(TServerUserTemplate::getName, serverUserTemplate.getName()).count();
        if(c > 0){
            return JsonReturn.errorMsg("该模板名称已存在");
        }
        return userTemplateService.save(serverUserTemplate) ? JsonReturn.successMsg("添加成功") : JsonReturn.errorMsg("添加失败");
    }

    public JsonReturn deleteUserTemplate(Integer id) {
        return userTemplateService.removeById(id) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    public JsonReturn updateUserTemplate(TServerUserTemplate serverUserTemplate) {
        Long c = userTemplateService.lambdaQuery().eq(TServerUserTemplate::getId, serverUserTemplate.getId()).count();
        if(c == 0){
            return JsonReturn.errorMsg("未找到该模板");
        }
        return userTemplateService.updateById(serverUserTemplate) ? JsonReturn.successMsg("修改成功") : JsonReturn.errorMsg("修改失败");
    }

    public TServerUserTemplate getById(Integer userTemplateId) {
        return userTemplateService.getById(userTemplateId);
    }
}
