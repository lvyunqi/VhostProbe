package com.chuqiyun.vhostprobe.listener;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.chuqiyun.vhostprobe.service.WebsocketRunClient;
import com.chuqiyun.vhostprobe.utils.JsonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.chuqiyun.vhostprobe.utils.NginxLogUtil.nginxLogHandler;

/**
 * @author mryunqi
 * @date 2023/1/16
 */
@Component
@EnableAsync
@Slf4j
public class FileListener extends FileAlterationListenerAdaptor {
    @Value("${chuqiyun.websocket.client.config[0].wsName}")
    private String wsName;

    @Autowired
    private Map<String, WebsocketRunClient> websocketRunClientMap;


    /**
     * 文件修改
     */
    @Override
    public void onFileChange(File file) {
        log.info("[修改]:" + file.getAbsolutePath());
        //读取文件内容
        try {
            JSONObject dataJson = JSONObject.parseObject(JsonUtil.readJSON());
            JSONArray arrayList = dataJson.getJSONArray("file");
            HashMap<String, Long> map = (HashMap<String, Long>) arrayList.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            object -> {
                                JSONObject item = (JSONObject) object;
                                return item.getString("name");
                            },
                            object -> {
                                JSONObject item = (JSONObject) object;
                                return item.getLong("size");
                            }
                    ));
            String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
            long size = map.getOrDefault(fileName, 0L);
            RandomAccessFile randomFile = new RandomAccessFile(file, "r");
            randomFile.seek(size);
            WebsocketRunClient websocketRunClient = websocketRunClientMap.get(wsName);
            String tmp = null;
            while ((tmp = randomFile.readLine()) != null) {
                websocketRunClient.send(nginxLogHandler(fileName,tmp));
            }
            HashMap<String,Object> newMap = new HashMap<>();
            if (map.containsKey(fileName)){
                newMap.put("name", fileName);
                newMap.put("size", randomFile.length());
                JsonUtil.updateJSON(newMap);
            }else {
                newMap.put("name", fileName);
                newMap.put("size", randomFile.length());
                JsonUtil.insertJSON(newMap);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件创建
     */
    @SneakyThrows
    @Override
    public void onFileCreate(File file) {
        log.info("[新建]:" + file);
        log.info("[新建]:" + file.getPath());
        log.info("[新建]:" + file.getAbsolutePath());
    }

    /**
     * 文件删除
     */
    @Override
    public void onFileDelete(File file) {
        log.info("[删除]:" + file.getAbsolutePath());
    }

    /**
     * 目录创建
     */
    @Override
    public void onDirectoryCreate(File directory) {
        log.info("[新建]:" + directory.getAbsolutePath());
    }

    /**
     * 目录修改
     */
    @Override
    public void onDirectoryChange(File directory) {
        log.info("[修改]:" + directory.getAbsolutePath());
    }

    /**
     * 目录删除
     */
    @Override
    public void onDirectoryDelete(File directory) {
        log.info("[删除]:" + directory.getAbsolutePath());
    }

    @Override
    public void onStart(FileAlterationObserver observer) {
        // TODO Auto-generated method stub
        super.onStart(observer);
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        // TODO Auto-generated method stub
        super.onStop(observer);
    }

}
