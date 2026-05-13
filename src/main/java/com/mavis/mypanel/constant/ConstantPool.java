package com.mavis.mypanel.constant;

public class ConstantPool {
    /**
     * 加密密钥 (128位，16字节)
     */
    public static final String AES_PASSWORD = "mypanel_aes_key1";
    /**
     * 随机生成uuid的key名
     */
    public static final String USER_UUID_KEY = "user_uuid";

    /**
     * 连接指令：连接
     */
    public static final String WEBSSH_OPERATE_CONNECT = "connect";

    /**
     * 发送指令：命令
     */
    public static final String WEBSSH_OPERATE_COMMAND = "command";

    /**
     * 编码
     */
    public static final String WEBSSH_OPERATE_ENCODED = "encoded";

    /**
     * 调整size
     */
    public static final String WEBSSH_OPERATE_RESIZE = "resize";

    /**
     * 心跳检测
     */
    public static final String WEBSSH_OPERATE_HEARTBEAT = "heartbeat";


    /**
     * 发送指令：关闭
     */
    public static final String WEBSSH_OPERATE_CLOSE = "close";
}
