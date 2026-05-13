package com.mavis.mypanel.handler;

import com.mavis.mypanel.constant.ConstantPool;
import com.mavis.mypanel.entity.pojo.SSHConnectInfo;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;

import java.io.IOException;

import static com.mavis.mypanel.service.TTerminalService.sshMap;


@Slf4j
public class RemoteWebSocketHandler implements WebSocketHandler {
    private final Object sendLock = new Object();

    /**
     * 用户连接上WebSocket的回调
     * @param webSocketSession
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws JSchException {
        initConnection(webSocketSession);
    }

    /**
     * 收到消息的回调
     * @param webSocketSession
     * @param webSocketMessage
     */
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws IOException {
        if (webSocketMessage instanceof TextMessage) {
            // 调用service接收消息
            recvHandle(((TextMessage) webSocketMessage).getPayload(), webSocketSession);
        } else if (webSocketMessage instanceof BinaryMessage) {

        } else if (webSocketMessage instanceof PongMessage) {

        } else {
            log.info("Unexpected WebSocket message type: " + webSocketMessage);
        }
    }

    /**
     * 出现错误的回调
     * @param webSocketSession
     * @param throwable
     */
    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        log.error("数据传输错误");
    }

    /**
     * 连接关闭的回调
     * @param webSocketSession
     * @param closeStatus
     */
    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        // 调用service关闭连接
//        close(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 初始化连接
     */
    protected void initConnection(WebSocketSession webSocketSession) throws JSchException {

    }

    /**
     * 处理客户段发的数据
     */
    protected void recvHandle(String message, WebSocketSession webSocketSession) throws IOException {

    }

    /**
     * 数据写回前端 for websocket
     */
    public void sendMessage(WebSocketSession session, byte[] buffer) {
        try {
            synchronized (sendLock) {
                if (session.isOpen()) {
                    log.info("发送消息：" + new String(buffer));
                    // 加密
//                    String encrypt = AESUtils.encrypt(new String(buffer), ConstantPool.AES_PASSWORD);
                    TextMessage textMessage = new TextMessage(buffer);
                    session.sendMessage(textMessage);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭终端
     * @param sshConnectInfo
     */
    public void close(SSHConnectInfo sshConnectInfo) {
        WebSocketSession webSocketSession = sshConnectInfo.getWebSocketSession();
        String userId = String.valueOf(webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY));
        sshMap.remove(userId);
        Session sshSession = sshConnectInfo.getSession();
        Channel channel = sshConnectInfo.getChannel();
        try {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.close();
            }
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
