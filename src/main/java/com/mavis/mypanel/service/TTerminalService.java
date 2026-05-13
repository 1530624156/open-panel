package com.mavis.mypanel.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavis.mypanel.constant.ConstantPool;
import com.mavis.mypanel.entity.TServer;
import com.mavis.mypanel.entity.pojo.SSHConnectInfo;
import com.mavis.mypanel.entity.pojo.WebSSHData;
import com.mavis.mypanel.handler.RemoteWebSocketHandler;
import com.mavis.mypanel.logic.ServerLogic;
import com.mavis.mypanel.util.AESUtils;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TTerminalService extends RemoteWebSocketHandler {
    public static Map<String, SSHConnectInfo> sshMap = new ConcurrentHashMap<>();

    public static Map<String, TServer> webLoginMap = new ConcurrentHashMap<>();

    public static FIFOCache<String, SSHConnectInfo> webSshMap = CacheUtil.newFIFOCache(100);



    /**
     * 初始化连接
     * @param session
     */
    @Override
    public void initConnection(WebSocketSession session) throws JSchException {
        SSHConnectInfo sshConnectInfo = new SSHConnectInfo();
        sshConnectInfo.setWebSocketSession(session);
        sshConnectInfo.setJSch(new JSch());
        String uuid = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        sshConnectInfo.setUserId(uuid);

        // 将这个ssh连接信息放入map中
        sshMap.put(uuid, sshConnectInfo);
    }

    @Override
    public void recvHandle(String buffer, WebSocketSession session) {
        System.out.println("收到消息：" + buffer);
        String buffer1 = "";
        if (StrUtil.isBlank(buffer)) {
            return;
        }
        // 解密，失败则使用明文
        try {
            buffer1 = AESUtils.decrypt(buffer, ConstantPool.AES_PASSWORD);
        } catch (Exception e) {
            log.warn("解密失败，使用明文: {}", e.getMessage());
            buffer1 = buffer;
        }

        WebSSHData webSSHData;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            webSSHData = objectMapper.readValue(buffer1, WebSSHData.class);
        } catch (IOException e) {
            log.error("JSON转换异常: {}", e.getMessage());
            return;
        }
        String userId = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        SSHConnectInfo sshConnectInfo = sshMap.get(userId);

        if (ConstantPool.WEBSSH_OPERATE_CONNECT.equals(webSSHData.getOperate())) {
            // 登录推送到站端
            sshConnectInfo.setTagId(webSSHData.getTagId());
            // TODO 这里的线程池，同时只能10个连接
            ThreadUtil.execAsync(() -> {
                try {
                    connectToSSH(sshConnectInfo);
                } catch (Exception e) {
                    String msg = "ssh连接异常: " + e.getMessage();
                    log.error(msg);
                    this.sendMessage(sshConnectInfo.getWebSocketSession(), msg.getBytes());
                    close(sshConnectInfo);
                }
            });
        }else if (ConstantPool.WEBSSH_OPERATE_COMMAND.equals(webSSHData.getOperate())) {
            String command = webSSHData.getCommand();
            // 当前目录
            if (sshConnectInfo == null) {
                log.error("SSH连接信息不存在，请先建立连接");
                return;
            }
            Channel channel = sshConnectInfo.getChannel();
            if (channel == null || !channel.isConnected()) {
                log.warn("SSH通道未建立或已断开，命令无法发送");
                this.sendMessage(sshConnectInfo.getWebSocketSession(), "SSH通道未建立，请等待连接完成".getBytes());
                return;
            }
            // 发送指令到站端
            try {
                transToSSH(channel, command);
            } catch (IOException e) {
                log.error("webssh连接异常");
                log.error("异常信息:{}", e.getMessage());
                close(sshConnectInfo);
            }
        }else if (ConstantPool.WEBSSH_OPERATE_RESIZE.equals(webSSHData.getOperate())) {
            // 修改 尺寸
            log.info("webssh 调整尺寸");
            if (webSSHData.getCols() >= 80 && webSSHData.getRows() >= 24 && sshConnectInfo != null) {
                Channel channel = sshConnectInfo.getChannel();
                if (channel != null) {
                    int width = webSSHData.getCols() * 9;
                    int height = webSSHData.getRows() * 17;
                    ChannelShell channelShell = (ChannelShell) channel;
                    channelShell.setPtySize(webSSHData.getCols(), webSSHData.getRows(), width , height);
                    channel = channelShell;
                    sshConnectInfo.setChannel(channel);
                }
            }
        } else if (ConstantPool.WEBSSH_OPERATE_ENCODED.equals(webSSHData.getOperate())) {
            sshConnectInfo.setEncoded(webSSHData.getCommand());
        } else if (ConstantPool.WEBSSH_OPERATE_HEARTBEAT.equals(webSSHData.getOperate())) {
            // 心跳  通过 传过来的 时间戳 续时长。
            Long timestamp = webSSHData.getTimestamp();
            if (timestamp != null) {
                sshConnectInfo.setLastActiveTime(timestamp);
            }
        } else if (ConstantPool.WEBSSH_OPERATE_CLOSE.equals(webSSHData.getOperate())) {
            close(sshConnectInfo);
        } else {
            close(sshConnectInfo);
        }
    }

    /**
     * 使用jsch连接终端
     * @param sshConnectInfo
     * @throws JSchException
     * @throws IOException
     */
    private void connectToSSH(SSHConnectInfo sshConnectInfo) throws JSchException, IOException, NoSuchFieldException {
        // 获取运行时上下文  ServiceLogic
        ServerLogic serverLogic = SpringUtil.getBean(ServerLogic.class);
        // 建立新会话
        Session session  = serverLogic.getSshSessionByServer(webLoginMap.get(sshConnectInfo.getTagId()));
        if (session == null) {
            throw new JSchException("无法建立SSH会话，请检查服务器配置");
        }
        // 开启shell通道
        Channel channel = session.openChannel("shell");
        // 从继承类修改
        ChannelShell channelShell = (ChannelShell) channel;
        channelShell.setPtyType("xterm", 80, 24, 80*9 , 24*17);
        channel = channelShell;
        // 通道连接 超时时间10 s
        channel.connect(10000);
        // 设置channel - 必须在连接成功后才设置
        sshConnectInfo.setChannel(channel);
        sshConnectInfo.setSession(session);

        // 连接成功后发送初始命令激活shell，获取初始输出
        try {
            transToSSH(channel, "\r");
        } catch (IOException e) {
            log.error("发送初始化命令失败: {}", e.getMessage());
            throw e;
        }

        // 读取终端返回的信息流
        InputStream inputStream = channel.getInputStream();
        try {
            int i = 0;
            // 循环读取
            byte[] buffer = new byte[1024];
            // 如果没有数据来，线程会一直阻塞在这个地方等待数据。
            while ((i = inputStream.read(buffer)) != -1) {
                byte[] bytes = Arrays.copyOfRange(buffer, 0, i);
                if (StrUtil.isNotEmpty(sshConnectInfo.getEncoded())) {
                    String data = new String(bytes, sshConnectInfo.getEncoded());
                    bytes = data.getBytes();
                }
                sendMessage(sshConnectInfo.getWebSocketSession(), bytes);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 断开连接后关闭会话
            session.disconnect();
            channel.disconnect();
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }


    /**
     * 将消息转发到终端
     * @param channel
     * @param command
     * @throws IOException
     */
    private void transToSSH(Channel channel, String command) throws IOException {
        if (channel == null) {
            throw new IOException("SSH通道为空");
        }
        OutputStream outputStream = channel.getOutputStream();
        outputStream.write(command.getBytes());
        outputStream.flush();
    }
}
