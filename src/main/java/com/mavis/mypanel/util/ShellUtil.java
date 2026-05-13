package com.mavis.mypanel.util;

import cn.hutool.core.util.RuntimeUtil;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

/**
 * 命令行工具
 */
public class ShellUtil {


    public static String runShell(String command){
        System.out.println("【SHELL】" + command);
        // 生成uuid
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 1.通过IO在当前执行目录下创建一个sh文件
        String shPath = System.getProperty("user.dir") + File.separatorChar + uuid + ".sh";
        // 2.写入sh文件
        MyIoUtil.writeByteToFile(command.getBytes(StandardCharsets.UTF_8), shPath);
        // 3.执行sh文件
        String[] cmdS = new String[]{"sh", shPath};
        String txt = "";
        try {
            txt = RuntimeUtil.execForStr(cmdS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("【SHELL - RES】" + txt);
        // 4.删除sh文件
        File file = new File(shPath);
        MyIoUtil.deleteFileOrDir(file);
        return txt;
    }

    /**
     * 获取ssh连接会话
     *
     * @param ip
     * @param port
     * @param username
     * @param password
     * @return
     */
    public static Session getSshSession(String ip, String port, String username, String password){
        try {
            JSch jSch = new JSch();
            Session session = jSch.getSession(username, ip, Integer.parseInt(port));
            session.setPassword(password);
            Properties config = new Properties();
            // 去掉首次连接确认
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setTimeout(60000);
            session.connect();
            if(session.isConnected()){
                return session;
            }
        } catch (JSchException e) {
        }
        return null;
    }

    /**
     * 获取ssh连接会话-pem
     * @param ip
     * @param port
     * @param pemPath
     * @param username
     * @param password
     * @return
     */
    public static Session getSshSession(String ip, String port,String username, String pemPath, String password){
        try {
            JSch jSch = new JSch();
            if(StringUtils.isBlank(password)){
                jSch.addIdentity(pemPath);
            }else {
                jSch.addIdentity(pemPath,password);
            }
            // 根据主机账号、ip、端口获取一个Session对象
            Session session = jSch.getSession(username, ip, Integer.valueOf(port));

            // 去掉首次连接确认
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setTimeout(60000);
            session.connect();
            if(session.isConnected()){
                return session;
            }
        } catch (JSchException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static String sshShell(Session session,String shell) throws IOException, JSchException {
        ChannelExec channel = (ChannelExec)session.openChannel("exec");
        InputStream in = channel.getInputStream();
        channel.setCommand(shell);
        channel.connect();
        String res = MyIoUtil.readInputStreamAsString(in);
        channel.disconnect();
        in.close();
        return res;
    }

    public static void main(String[] args) throws JSchException {
        //Session session = ShellUtil.getSshSession("pub.by-shadow.cn", "22222", "root", "H:\\超星\\yanglilun.pem", "a14978435");
        //System.out.println(session);
    }

}
