package com.chuqiyun.vhostprobe.service;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * @author mryunqi
 * @date 2023/1/16
 */
@Slf4j
public class WebsocketRunClient extends WebSocketClient {
    /**
     * websocket连接名称
     */
    private String wsName;


    public WebsocketRunClient(URI serverUri, String wsName) {
        super(serverUri);
        this.wsName = wsName;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("[Probe {}] Client连接成功",wsName);
    }

    @Override
    public void onMessage(String msg) {
        log.info("[Probe {}] 收到String消息：{}",wsName,msg);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        log.info("[Probe {}] 收到ByteBuffer消息：{}",wsName,bytes);
    }


    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("[Probe {}] 客户端关闭",wsName);
        //System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onError(Exception e) {
        log.info("[Probe {}] Client出现异常, 异常原因为：{}",wsName,e.getMessage());
    }


}
