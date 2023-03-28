package com.chuqiyun.vhostprobe.config;

import com.chuqiyun.vhostprobe.entity.WebsocketClientConfiguration;
import com.chuqiyun.vhostprobe.service.WebsocketRunClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mryunqi
 * @date 2023/1/16
 */
@Slf4j
@EnableAsync
@Configuration
public class WebsocketMultipleBeanConfig {

    @Resource(name = "taskExecutor")
    private TaskExecutor executor;
    @Bean
    public Map<String, WebsocketRunClient> websocketRunClientMap(WebsocketClientConfiguration websocketClientConfiguration){

        Map<String, WebsocketRunClient> retMap = new HashMap<>(5);

        List<WebsocketClientConfiguration.ServerProperties> config = websocketClientConfiguration.getConfig();

        for (WebsocketClientConfiguration.ServerProperties serverProperties : config) {

            String wsUrl = serverProperties.getWsUrl();
            String wsName = serverProperties.getWsName();
            Boolean enableReconnection = serverProperties.getEnableReconnection();
            Boolean enableHeartbeat = serverProperties.getEnableHeartbeat();
            Integer heartbeatInterval = serverProperties.getHeartbeatInterval();

            try {
                WebsocketRunClient websocketRunClient = new WebsocketRunClient(new URI(wsUrl),wsName);
                websocketRunClient.connect();
                websocketRunClient.setConnectionLostTimeout(0);

                executor.execute(() -> {
                    while (true){
                        try {
                            Thread.sleep(heartbeatInterval);
                            if(enableHeartbeat){
                                websocketRunClient.send("[Probe "+wsName+"] 心跳检测");
                                log.info("[Probe {}] 心跳检测",wsName);
                            }
                        } catch (Exception e) {
                            log.error("[Probe {}] 发生异常{}",wsName,e.getMessage());
                            try {
                                if(enableReconnection){
                                    log.info("[Probe {}] 重新连接",wsName);
                                    websocketRunClient.reconnect();
                                    websocketRunClient.setConnectionLostTimeout(0);
                                }
                            }catch (Exception ex){
                                log.error("[Probe {}] 重连异常,{}",wsName,ex.getMessage());
                            }
                        }
                    }
                });

                retMap.put(wsName,websocketRunClient);
            } catch (URISyntaxException ex) {
                log.error("[Probe {}] 连接异常,{}",wsName,ex.getMessage());
            }
        }
        return retMap;

    }



}
