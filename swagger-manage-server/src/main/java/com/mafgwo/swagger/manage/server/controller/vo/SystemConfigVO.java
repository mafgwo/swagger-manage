package com.mafgwo.swagger.manage.server.controller.vo;

import com.mafgwo.swagger.manage.server.entity.SystemConfig;

import java.util.List;

/**
 * @author tanghc
 */
public class SystemConfigVO {
    private List<SystemConfig> globalHeaders;
    private List<String> allowMethods;
    private String debugHost;

    public List<SystemConfig> getGlobalHeaders() {
        return globalHeaders;
    }

    public void setGlobalHeaders(List<SystemConfig> globalHeaders) {
        this.globalHeaders = globalHeaders;
    }

    public List<String> getAllowMethods() {
        return allowMethods;
    }

    public void setAllowMethods(List<String> allowMethods) {
        this.allowMethods = allowMethods;
    }

    public String getDebugHost() {
        return debugHost;
    }

    public void setDebugHost(String debugHost) {
        this.debugHost = debugHost;
    }
}
