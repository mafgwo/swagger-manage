package com.mafgwo.swagger.manage.server.service;

import com.mafgwo.swagger.manage.server.entity.SystemConfig;
import com.mafgwo.swagger.manage.server.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author tanghc
 */
@Service
public class SystemConfigService {

    public static final int TYPE_COMMON = 0;
    public static final int TYPE_HEADERS = 1;
    public static final String KEY_ALLOW_METHODS = "allow-methods";

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    public static String getDebugHostKey(int projectId) {
        return "debughost_" + projectId;
    }

    public String getAllowMethods() {
        return getCommonConfigValue(SystemConfigService.KEY_ALLOW_METHODS, "GET").toUpperCase();
    }

    public String getDebugHost(int projectId) {
        String debugHostKey = getDebugHostKey(projectId);
        return getCommonConfigValue(debugHostKey, "");
    }

    public SystemConfig getCommonConfig(String key) {
        return systemConfigMapper.getCommonConfig(key);
    }

    public String getCommonConfigValue(String key, String defaultValue) {
        SystemConfig commonConfig = getCommonConfig(key);
        return Optional.ofNullable(commonConfig)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

}
