package com.mavis.mypanel.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MyDockerUtil {

    //cpu主频基础倍数
    private static final long NANO_CPUS = 1000000000L;

    public static Info getDockerDeamonInfo(DefaultDockerClientConfig dockerClientConfig){
        DockerClient dockerClient = DockerClientBuilder.getInstance(dockerClientConfig).build();
        Info info = null;
        try {
            info = dockerClient.infoCmd().exec();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return info;
    }


    /**
     * 拉取镜像
     * @return
     */
    public static boolean pullImage(DefaultDockerClientConfig config,String imageName,String registryUsername,String registryPassword){
        AtomicBoolean f = new AtomicBoolean(false);
        execDockerCmd(config,(client -> {
            try {
                AuthConfig authConfig = new AuthConfig();
                authConfig.withUsername(registryUsername);
                authConfig.withPassword(registryPassword);
                PullImageCmd req = client.pullImageCmd(imageName).withAuthConfig(authConfig);

                PullImageResultCallback res = new PullImageResultCallback();
                req.exec(res);
                res.awaitCompletion();
            } catch (Exception e) {
                return;
            }
            f.set(true);
        }));
        return f.get();
    }

    /**
     * 删除镜像
     * @return
     */
    public static boolean removeImage(String imageName,DefaultDockerClientConfig config){
        AtomicBoolean f = new AtomicBoolean(false);
        execDockerCmd(config,(client -> {
            try {
                client.removeImageCmd(imageName).exec();
            } catch (Exception e) {
                return;
            }
            f.set(true);
        }));
        return f.get();
    }

    /**
     * 创建容器
     * @return dockerId
     */
    public static String addDockerContainerWithoutStart(DefaultDockerClientConfig config,String containerName, String imageName, Map<Integer,Integer> portBind,Map<String,String> mountMap, Integer nanoCpus){
        Long finalNanoCpus = nanoCpus * NANO_CPUS;
        AtomicReference<String> id = new AtomicReference<>(null);
        execDockerCmd(config,(client -> {
            CreateContainerCmd ccm = client.createContainerCmd(imageName);
            ccm.withName(containerName);
            HostConfig hostConfig = HostConfig.newHostConfig();
            //绑定端口
            if(portBind != null){
                ArrayList<PortBinding> list = new ArrayList<>();
                ArrayList<ExposedPort> exposedPorts = new ArrayList<>();
                for (Integer key : portBind.keySet()) {
                    //端口绑定关系
                    list.add(PortBinding.parse(key + ":" + portBind.get(key)));
                    exposedPorts.add(ExposedPort.parse(portBind.get(key) + "/tcp"));
                }
                //创建网络配置
                ccm.withExposedPorts(exposedPorts);
                hostConfig.withPortBindings(list);
            }
            //创建CPU限制配置
            hostConfig.withNanoCPUs(finalNanoCpus);
            //挂载路径
            if(mountMap != null){
                ArrayList<Bind> binds = new ArrayList<>();
                for (String key : mountMap.keySet()) {
                    binds.add(Bind.parse(String.format("%s:%s",
                            key, mountMap.get(key))));
                }
                hostConfig.withBinds(binds);
            }
            CreateContainerResponse response = ccm.withHostConfig(hostConfig).exec();
            ccm.close();
            id.set(response.getId());
        }));
         return id.get();
    }

    /**
     * 通用执行docker方法
     * @param config
     * @param callback
     */
    private static void execDockerCmd(DefaultDockerClientConfig config,ExecDockerCallback callback){
        //获取客户端
        DockerClient client = DockerClientBuilder.getInstance(config).build();
        if(client != null){
            callback.exec(client);
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            callback.exec(null);
        }
    }

    /**
     * 根据容器id删除容器
     * @return
     */
    public static boolean removeDockerContainerById(DefaultDockerClientConfig config,String id){
        AtomicBoolean f = new AtomicBoolean(false);
        execDockerCmd(config,(client -> {
            try {
                client.removeContainerCmd(id).exec();
            } catch (Exception e) {
                return;
            }
            f.set(true);
        }));
        return f.get();
    }

    public static Long inspectDockerContainerById(DefaultDockerClientConfig config,String id){
        AtomicReference<Long> exitCodeLong = new AtomicReference<>(null);
        execDockerCmd(config,(client -> {
            try {
                InspectContainerResponse exec = client.inspectContainerCmd(id).exec();
                exitCodeLong.set(exec.getState().getExitCodeLong());
                System.out.println(exec);
            } catch (Exception e) {
                return;
            }
        }));
        return exitCodeLong.get();
    }


    /**
     * 根据容器id停止容器
     * @param containerId
     * @return
     */
    public static boolean killDockerContainerById(DefaultDockerClientConfig config,String containerId){
        AtomicReference<Boolean> flag = new AtomicReference<>(false);
        execDockerCmd(config,(client -> {
            try {
                client.killContainerCmd(containerId).exec();
            } catch (Exception e) {
                return;
            }
            flag.set(true);
        }));
        return flag.get();
    }

    /**
     * 根据容器id启动容器
     * @param containerId
     * @return
     */
    public static boolean startDockerContainerById(DefaultDockerClientConfig config, String containerId){
        AtomicBoolean f = new AtomicBoolean(false);
        execDockerCmd(config,(client -> {
            try {
                client.startContainerCmd(containerId).exec();
            } catch (Exception e) {
                return;
            }
            f.set(true);
        }));
        return f.get();
    }

    public static boolean restartDockerContainerById(DefaultDockerClientConfig config, String containerId){
        AtomicReference<Boolean> flag = new AtomicReference<>(false);
        execDockerCmd(config,(client -> {
            try {
                client.restartContainerCmd(containerId).exec();
            } catch (Exception e) {
                return;
            }
            flag.set(true);
        }));
        return flag.get();
    }

    public static List<String> getLogById(DefaultDockerClientConfig config, String containerId,Integer tail){
        final List<String> logs = new ArrayList<>();
        MyDockerUtil.execDockerCmd(config,client -> {
            LogContainerCmd cmd = client.logContainerCmd(containerId);

            cmd.withStdOut(true).withStdErr(true);
            cmd.withTail(tail);
            try {
                cmd.exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        logs.add(item.toString());
                    }
                }).awaitCompletion(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return logs;
    }


    public static void main(String[] args) {

        //DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        //        .withDockerCertPath("D:\\openssl")
        //        .withDockerHost("tcp://192.168.40.118:12375")
        //        .withDockerTlsVerify(true)
        //        .build();

        //DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        //        .withDockerHost("tcp://10.254.2.8:12375")
        //        .build();
        //
        //DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        //Info info = dockerClient.infoCmd().exec();
        //System.out.println(info);

        System.out.println("202502021".compareTo("20250202"));
    }



}


/**
 * 执行docker回调
 */
interface ExecDockerCallback{
    void exec(DockerClient client);
}

