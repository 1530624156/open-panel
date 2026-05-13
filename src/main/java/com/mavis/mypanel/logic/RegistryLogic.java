package com.mavis.mypanel.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mavis.mypanel.entity.TRegistry;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.service.TRegistryService;
import com.mavis.mypanel.util.DockerRegistryUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RegistryLogic {

    @Resource
    private TRegistryService registryService;


    /**
     * 查仓库list
     * @param registry
     * @return
     */
    public JsonReturn getRegList(TRegistry registry) {
        QueryWrapper<TRegistry> qw = new QueryWrapper<>();
        if(StringUtils.isNotBlank(registry.getUrl())){
            qw.like("url", registry.getUrl());
        }
        if(registry.getStatus() != null){
            qw.eq("status", registry.getStatus());
        }
        return JsonReturn.success(registryService.list(qw));
    }

    /**
     * 添加仓库
     * @param registry
     * @return
     */
    public JsonReturn addReg(TRegistry registry) {
        //判断地址是否存在了
        QueryWrapper qw = new QueryWrapper();
        qw.eq("url", registry.getUrl());
        if(registryService.count(qw)>0){
            return JsonReturn.errorMsg("该地址已存在");
        }
        registry.setHost(registry.getUrl().replaceAll("https://","").replaceAll("http://",""));
        boolean f = registryService.save(registry);
        return f?JsonReturn.successMsg("添加成功"):JsonReturn.errorMsg("添加失败");
    }

    /**
     * 删除仓库
     * @param id
     * @return
     */
    public JsonReturn delRegById(Integer id) {
        boolean f = registryService.removeById(id);
        return f?JsonReturn.successMsg("删除成功"):JsonReturn.errorMsg("删除失败");
    }

    /**
     * 测试仓库状态
     * @param registry
     * @return
     */
    public JsonReturn testReg(TRegistry registry) {
        DockerRegistryUtil dockerRegistryUtil = new DockerRegistryUtil(registry.getUrl(), registry.getUsername(), registry.getPassword());
        List<String> mirrorList = dockerRegistryUtil.getMirrorList();
        if(CollectionUtils.isEmpty(mirrorList)){
            registry.setStatus(0);
            registryService.updateById(registry);
            return JsonReturn.errorMsg("连接失败");
        }else {
            registry.setStatus(1);
            registryService.updateById(registry);
            return JsonReturn.successMsg("连接成功");
        }
    }

    public JsonReturn editRegById(TRegistry registry) {
        //判断是否存在
        QueryWrapper<TRegistry> qw = new QueryWrapper<>();
        qw.eq("id",registry.getId());
        if(registryService.count(qw) == 0){
            return JsonReturn.errorMsg("该仓库不存在");
        }
        registry.setStatus(0);
        registry.setHost(registry.getUrl().replaceAll("https://","").replaceAll("http://",""));
        return registryService.updateById(registry)?JsonReturn.successMsg("修改成功"):JsonReturn.errorMsg("修改失败");
    }

    /**
     * 获取仓库下的镜像列表
     * @param registry
     * @return
     */
    public JsonReturn getMirrorByReg(TRegistry registry) {
        DockerRegistryUtil dockerRegistryUtil = new DockerRegistryUtil(registry.getUrl(), registry.getUsername(), registry.getPassword());
        return JsonReturn.success(dockerRegistryUtil.getMirrorList());
    }

    /**
     //* 获取所有镜像和tag信息
     * @param id
     * @return
     */
    public JsonReturn getAllTagsInfo(Integer id) {
        TRegistry registry = registryService.getById(id);
        if(registry == null){
            return JsonReturn.errorMsg("仓库不存在");
        }
        DockerRegistryUtil dockerRegistryUtil = new DockerRegistryUtil(registry.getUrl(), registry.getUsername(), registry.getPassword());
        JSONObject infoList = dockerRegistryUtil.getInfoList();
        return JsonReturn.success(infoList);
    }

    /**
     * 根据mirror获取tags
     *
     * @param regId
     * @param mirror
     * @return
     */
    public JsonReturn getTagsByRegIdMirror(Integer regId, String mirror) {
        TRegistry registry = registryService.getById(regId);
        if(registry != null){
            DockerRegistryUtil dockerRegistryUtil = new DockerRegistryUtil(registry.getUrl(), registry.getUsername(), registry.getPassword());
            JSONArray arr = dockerRegistryUtil.getMirrorTagsInfo(mirror);
            return JsonReturn.success(arr);
        }
        return JsonReturn.errorMsg("获取失败");
    }


    /**
     * 根据仓库url 和TagName 获取所有Tag
     *
     * @return
     */
    public JsonReturn getTagsByRegURLMirror(String url, String tagName) {
        TRegistry registry = registryService.lambdaQuery().eq(TRegistry::getHost, url).one();
        if(registry != null){
            DockerRegistryUtil dockerRegistryUtil = new DockerRegistryUtil(registry.getUrl(), registry.getUsername(), registry.getPassword());
            JSONArray arr = dockerRegistryUtil.getMirrorTagsInfo(tagName);
            return JsonReturn.success(arr);
        }
        return JsonReturn.errorMsg("获取失败");
    }

    public JsonReturn deleteMirrorTagsByDigest(Integer regId, String mirrorname, String digest) {
        TRegistry registry = registryService.getById(regId);
        if(registry == null){
            return JsonReturn.errorMsg("仓库不存在");
        }
        DockerRegistryUtil dockerRegistryUtil = new DockerRegistryUtil(registry.getUrl(), registry.getUsername(), registry.getPassword());
        boolean f = dockerRegistryUtil.deleteMirrorTagsByDigest(mirrorname, digest);
        return f?JsonReturn.successMsg("删除成功"):JsonReturn.errorMsg("删除失败");
    }

    public TRegistry getByHost(String host) {
        TRegistry reg = registryService.lambdaQuery().eq(TRegistry::getHost, host).one();
        return reg;
    }

}
