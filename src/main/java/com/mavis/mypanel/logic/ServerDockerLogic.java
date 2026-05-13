package com.mavis.mypanel.logic;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.mavis.mypanel.entity.TServerDocker;
import com.mavis.mypanel.entity.TServiceNode;
import com.mavis.mypanel.entity.TSystemAttachment;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.service.TServerDockerService;
import com.mavis.mypanel.service.TServiceNodeService;
import com.mavis.mypanel.util.MyDockerUtil;
import com.mavis.mypanel.util.StaticUtil;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
public class ServerDockerLogic {

    @Resource
    private TServerDockerService dockerService;

    @Resource
    private AttachmentLogic attachmentLogic;

    @Resource
    private TServiceNodeService serviceNodeService;


    public JsonReturn getDockerList(TServerDocker serverDocker) {
        LambdaQueryChainWrapper<TServerDocker> qw = dockerService.lambdaQuery();
        if(StringUtils.isNotBlank(serverDocker.getName())){
            qw.like(TServerDocker::getName, serverDocker.getName());
        }
        if(StringUtils.isNotBlank(serverDocker.getIp())){
            qw.like(TServerDocker::getIp, serverDocker.getIp());
        }
        List<TServerDocker> list = qw.list();
        return JsonReturn.success(list);
    }

    public JsonReturn deleteDockerNodeById(Integer id) {
        boolean f = dockerService.removeById(id);
        return f ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    public JsonReturn addDockerNode(TServerDocker serverDocker) {
        Long c = dockerService.lambdaQuery().eq(TServerDocker::getName, serverDocker.getName()).count();
        if(c > 0){
            return JsonReturn.errorMsg("该节点名称已存在");
        }
        return dockerService.save(serverDocker) ? JsonReturn.successMsg("添加成功") : JsonReturn.errorMsg("添加失败");
    }

    public JsonReturn editDockerNode(TServerDocker serverDocker) {
        TServerDocker node = dockerService.lambdaQuery().eq(TServerDocker::getName, serverDocker.getName()).one();
        if(node != null && !node.getId().equals(serverDocker.getId())){
            return JsonReturn.errorMsg("该节点名称已存在");
        }
        boolean f = dockerService.updateById(serverDocker);
        if(f){
            //更新后，刷新tls路径
            if(serverDocker.getTls() == 1){
                if(!createOrUpdateTlsDir(serverDocker)){
                    return JsonReturn.errorMsg("更新TLS失败");
                }
            }
            return JsonReturn.successMsg("修改成功");
        }else {
            return JsonReturn.errorMsg("修改失败");
        }
    }

    /**
     * 测试dockerapi接口连接情况
     * @param serverDocker
     * @return
     */
    public JsonReturn testDockerNode(TServerDocker serverDocker) {
        String ip = serverDocker.getIp();
        String port = serverDocker.getPort();
        if(StringUtils.isBlank(ip) || StringUtils.isBlank(port)){
            return JsonReturn.errorMsg("ip或端口不能为空");
        }
        DefaultDockerClientConfig config = getDockerConfigByServerDocker(serverDocker);
        if(config == null){
            return JsonReturn.errorMsg("docker配置失败");
        }
        Info info = MyDockerUtil.getDockerDeamonInfo(config);
        if(info == null){
            return JsonReturn.errorMsg("连接失败");
        }
        return JsonReturn.success(info);
    }

    public DefaultDockerClientConfig getDockerConfigByServerDocker(TServerDocker serverDocker){
        DefaultDockerClientConfig config = null;
        //有无tls
        if(serverDocker.getTls() == 0){
            config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(String.format("tcp://%s:%s", serverDocker.getIp(), serverDocker.getPort()))
                    .build();
        }else {
            //生成tls文件路径
            if(!createOrUpdateTlsDir(serverDocker)){
                return null;
            }
            config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerTlsVerify(true)
                    .withDockerCertPath(StaticUtil.mypanel_docker_tls_root+serverDocker.getId())
                    .withDockerHost(String.format("tcp://%s:%s", serverDocker.getIp(), serverDocker.getPort()))
                    .build();
        }
        return config;
    }

    /**
     * 创建或更新docker节点TLS证书文件
     *
     * @param serverDocker
     * @return
     */
    private boolean createOrUpdateTlsDir(TServerDocker serverDocker) {
        if(StringUtils.isAnyBlank(serverDocker.getTlsCaAttachUuid(), serverDocker.getTlsCertAttachUuid(), serverDocker.getTlsKeyAttachUuid())){
            return false;
        }

        //判断docker节点有没有证书路径
        String path = StaticUtil.mypanel_docker_tls_root + serverDocker.getId();
        File dir = new File(path);
        if(dir.exists()){
            //存在证书
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }else {
            //不存在，创建路径
            dir.mkdirs();
        }

        //拷贝覆盖
        TSystemAttachment ca = attachmentLogic.getByUuid(serverDocker.getTlsCaAttachUuid());
        File ca_file = new File(StaticUtil.mypanel_save_path + ca.getUuid() + ca.getSuffix());
        FileUtil.copy(ca_file,new File(dir,"ca.pem"),true);

        TSystemAttachment cert = attachmentLogic.getByUuid(serverDocker.getTlsCertAttachUuid());
        File cert_file = new File(StaticUtil.mypanel_save_path + cert.getUuid() + cert.getSuffix());
        FileUtil.copy(cert_file,new File(dir,"cert.pem"),true);

        TSystemAttachment key = attachmentLogic.getByUuid(serverDocker.getTlsKeyAttachUuid());
        File key_file = new File(StaticUtil.mypanel_save_path + key.getUuid() + key.getSuffix());
        FileUtil.copy(key_file,new File(dir,"key.pem"),true);

        return true;
    }

    //定时刷新docker节点信息
    public void spanDockerNodeInfo() {
        List<TServerDocker> list = dockerService.list();
        CountDownLatch downLatch = new CountDownLatch(list.size());
        for (TServerDocker docker : list) {
            new Thread(() -> {
                DefaultDockerClientConfig config = getDockerConfigByServerDocker(docker);
                Info info = MyDockerUtil.getDockerDeamonInfo(config);
                if(info == null){
                    docker.setStatus(0);
                }else{
                    System.out.println(info);
                    //镜像数
                    docker.setImagesNum(info.getImages());
                    docker.setCpuNum(info.getNCPU());
                    docker.setMemTotal(info.getMemTotal().toString());
                    docker.setArchType(info.getArchitecture());
                    //容器信息
                    docker.setContainerUpNum(info.getContainersRunning());
                    docker.setContainerDownNum(info.getContainersStopped());
                    //主机状态
                    docker.setStatus(1);
                }
                downLatch.countDown();
            }).start();
        }
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dockerService.updateBatchById(list);
    }


    public JsonReturn respanDockerStatus() {
        spanDockerNodeInfo();
        return JsonReturn.successMsg("刷新成功");
    }

    /**
     * 获取docker节点常见信息
     * @param serverDocker
     * @return
     */
    public JsonReturn getDockerNodeInfo(TServerDocker serverDocker) {
        List<TServerDocker> list = dockerService.lambdaQuery().select(
                TServerDocker::getId,
                TServerDocker::getIp,
                TServerDocker::getCpuNum,
                TServerDocker::getMemTotal,
                TServerDocker::getContainerUpNum).list();
        return JsonReturn.success(list);
    }

    public JsonReturn getDockerNodeUsedPort(Integer id) {
        List<TServiceNode> list = serviceNodeService.lambdaQuery().eq(TServiceNode::getDockerId, id).list();
        ArrayList<Object> arr = new ArrayList<>();
        for (TServiceNode tServiceNode : list) {
            String portMap = tServiceNode.getPortMap();
            if(StringUtils.isNotBlank(portMap)){
                JSONObject obj = JSON.parseObject(portMap);
                arr.addAll(obj.keySet());
            }
        }
        arr.sort((o1, o2) -> Integer.parseInt(o1.toString()) - Integer.parseInt(o2.toString()));
        return JsonReturn.success(arr);
    }

    public TServerDocker getById(Integer dockerId) {
        return dockerService.getById(dockerId);
    }

    public List<TServerDocker> list() {
        return dockerService.list();
    }

    public List<TServerDocker> getByIds(List<Integer> dockerIds) {
        List<TServerDocker> list = dockerService.lambdaQuery().in(TServerDocker::getId, dockerIds).list();
        return list;
    }
}
