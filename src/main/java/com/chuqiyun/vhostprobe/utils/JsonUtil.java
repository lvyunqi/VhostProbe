package com.chuqiyun.vhostprobe.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * @author mryunqi
 * @date 2023/1/16
 */
public class JsonUtil {
    private static final String ROOT_PATH = System.getProperty("user.dir")+File.separator+"data.json";
    public static String readJSON() throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(ROOT_PATH))));

        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString().replaceAll("\r\n", "").replaceAll(" +", "");
    }

    public static void updateJSON(HashMap<String, Object> map) throws Exception {

        JSONObject jsonObject = JSON.parseObject(readJSON());
        JSONArray links = jsonObject.getJSONArray("file");

        for (int i = 0; i < links.size(); i++) {
            JSONObject item1 = links.getJSONObject(i);
            if (item1.getString("name").equalsIgnoreCase((String) map.get("name"))) {
                item1.put("size", map.get("size"));
            }
        }

        writeJson(links);

    }

    public static void insertJSON(HashMap<String, Object> map) throws Exception {

        JSONObject jsonObject = JSON.parseObject(readJSON());
        JSONArray links = jsonObject.getJSONArray("file");
        JSONObject item = new JSONObject();
        item.put("name",map.get("name"));
        item.put("size",map.get("size"));
        links.add(item);
        writeJson(links);

    }

    private static void writeJson(JSONArray links) {
        JSONObject nItem = new JSONObject();
        nItem.put("file", links);
        String nContent = JSON.toJSONString(nItem);


        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(ROOT_PATH))));
            bw.write("");
            bw.write(nContent);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert bw != null;
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
