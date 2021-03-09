package com.mafgwo.swagger.manage.server.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author tanghc
 */
public class ConfUtil {

    private static final String CONF_FILE = "conf.json";
    private static final String filepath = System.getProperty("user.dir") + "/conf/" + CONF_FILE;


    public static final String KEY_AUTH_USERNAME = "swagger.basic.username";
    public static final String KEY_AUTH_PASSWORD = "swagger.basic.password";

    static {
        File confFile = new File(filepath);
        if (!confFile.exists()) {
            try {
                FileUtils.write(confFile, "{}", StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("初始化配置文件失败", e);
            }
        }
    }

    public static synchronized String getValue(String group, String key) {
        JSONObject root = getRoot();
        JSONObject groupJson = root.getJSONObject(group);
        return groupJson == null ? null : groupJson.getString(key);
    }

    public static synchronized JSONObject getConfig(String group) {
        JSONObject root = getRoot();
        return root.getJSONObject(group);
    }


    public static synchronized void setValue(String group, Map<String, ?> config) {
        setValue(group, groupJson -> groupJson.putAll(config));
    }

    public static synchronized void setValue(String group, String key, Object value) {
        setValue(group, groupJson -> groupJson.put(key, value));
    }

    private static synchronized void setValue(String group, Consumer<JSONObject> consumer) {
        try {
            JSONObject root = getRoot();
            JSONObject groupJson = root.getJSONObject(group);
            if (groupJson == null) {
                groupJson = new JSONObject();
                root.put(group, groupJson);
            }
            consumer.accept(groupJson);
            FileUtils.write(new File(filepath), root.toJSONString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject getRoot() {
        String json = null;
        try {
            json = FileUtils.readFileToString(new File(filepath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件失败", e);
        }
        return JSON.parseObject(json);
    }
}
