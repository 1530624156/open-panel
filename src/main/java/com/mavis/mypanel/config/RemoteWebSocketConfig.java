package com.mavis.mypanel.config;

import com.mavis.mypanel.service.TTerminalService;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import com.mavis.mypanel.constant.ConstantPool;
import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocket
@Log
public class RemoteWebSocketConfig implements WebSocketConfigurer {

    public RemoteWebSocketConfig(){
        log.info("开启websocket");
    }

    @Resource
    TTerminalService tTerminalService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tTerminalService, "/ws/terminal")
                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*");
    }

    /**
     * websocket 拦截器主要设置用户id,后期加，暂不处理
     */
    public static class WebSocketInterceptor implements HandshakeInterceptor {

        /**
         * Handler处理前调用
         *
         * @param serverHttpRequest
         * @param serverHttpResponse
         * @param webSocketHandler
         * @param map
         * @return
         */
        @Override
        public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) {
            if (serverHttpRequest instanceof ServletServerHttpRequest) {
                // 生成一个UUID
                String uuid = UUID.randomUUID().toString().replace("-","");
                // 将uuid放到websocketsession中
                map.put(ConstantPool.USER_UUID_KEY, uuid);
                return true;
            }
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                   ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

        }
    }
}