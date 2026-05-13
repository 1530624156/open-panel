package com.mavis.mypanel.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.mavis.mypanel.entity.pojo.SSHConnectInfo;

import java.util.Iterator;
import java.util.Map;

import static com.mavis.mypanel.service.TTerminalService.sshMap;

@Slf4j
@Component
public class SSHSessionTimeoutChecker {

    // 会话超时时间：60 秒
    private static final long SESSION_TIMEOUT = 60 * 1000;
    @Scheduled(fixedDelay = 60000) // 每 60 秒执行一次
    public void checkSessionTimeouts() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<String, SSHConnectInfo>> iterator = sshMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SSHConnectInfo> entry = iterator.next();
            SSHConnectInfo sshConnectInfo = entry.getValue();

            if (now - sshConnectInfo.getLastActiveTime() > SESSION_TIMEOUT) {
                String sessionKey = entry.getKey();
                log.warn("SSH 会话 [{}] 超时，准备关闭", sessionKey);
                try {
                    if (sshConnectInfo.getWebSocketSession() != null && sshConnectInfo.getWebSocketSession().isOpen()) {
                        sshConnectInfo.getWebSocketSession().close();
                    }
                    if (sshConnectInfo.getChannel() != null && sshConnectInfo.getChannel().isConnected()) {
                        sshConnectInfo.getChannel().disconnect();
                    }
                    if (sshConnectInfo.getSession() != null && sshConnectInfo.getSession().isConnected()) {
                        sshConnectInfo.getSession().disconnect();
                    }
                    iterator.remove();
                    log.info("SSH 会话 [{}] 已关闭并移除", sessionKey);
                } catch (Exception e) {
                    log.error("关闭 SSH 会话 [{}] 时出错", sessionKey, e);
                }
            }
        }
    }
}
