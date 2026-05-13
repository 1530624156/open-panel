package com.mavis.mypanel.entity.pojo;

import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;
@Data
public class SSHConnectInfo {
    /**
     * session
     */
    private WebSocketSession webSocketSession;

    /**
     * 站点信息
     */
    private String tagId;

    private String userId;
    private JSch jSch;
    private Channel channel;
    private Session session;
    private String encoded;
    private Sftp sftp;
    private long lastActiveTime;
}
