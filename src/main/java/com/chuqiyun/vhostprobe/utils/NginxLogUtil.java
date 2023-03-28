package com.chuqiyun.vhostprobe.utils;

import com.alibaba.fastjson2.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mryunqi
 * @date 2023/1/16
 */
public class NginxLogUtil {
    private static final Pattern PATTERN = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+) - (.*) \\[(.*)\\] \'(.*)\' (\\d+) (\\d+) \'(.*)\' \'(.*)\'");

    public static String nginxLogHandler(String user,String logLine){
        String str = logLine.replace("\"","'");
        Matcher matcher = PATTERN.matcher(str);
        JSONObject jsonObject = new JSONObject();
        JSONObject result = new JSONObject();
        if (matcher.matches()) {
            jsonObject.put("remoteAddr",matcher.group(1));
            jsonObject.put("remoteUser",matcher.group(2));
            jsonObject.put("timeLocal",matcher.group(3));
            jsonObject.put("request",matcher.group(4));
            jsonObject.put("status",matcher.group(5));
            jsonObject.put("bodyBytesSent",matcher.group(6));
            jsonObject.put("httpReferer",matcher.group(7));
            jsonObject.put("httpUserAgent",matcher.group(8));
            //jsonObject.put("httpXForwardedFor",matcher.group(9));
            result.put("user",user);
            result.put("event",jsonObject);
        }
        return result.toJSONString();
    }
}
