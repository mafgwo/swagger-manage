package com.mafgwo.swagger.manage.server.controller.vo;

import com.mafgwo.swagger.manage.server.entity.SwaggerInfo;

import java.util.List;

/**
 * @author tanghc
 */
public class ProjectInfoVO {
    private Integer id;
    /** 项目名称 */
    private String name;
    /** 访问地址，如：http://localhost:8080 */
    private String host;
    /** 网关规则 */
    private String gatewayRule;
    /** 网关host */
    private String gatewayHost;
    /** 是否导入的项目，1：是，0：否 */
    private Integer isImport;
    /** basic认证用户名 */
    private String basicAuthUsername;
    /** basic认证密码 */
    private String basicAuthPassword;

    private List<SwaggerInfo> groups;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public String getGatewayRule() {
        return gatewayRule;
    }

    public void setGatewayRule(String gatewayRule) {
        this.gatewayRule = gatewayRule;
    }

    public String getGatewayHost() {
        return gatewayHost;
    }

    public void setGatewayHost(String gatewayHost) {
        this.gatewayHost = gatewayHost;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getIsImport() {
        return isImport;
    }

    public void setIsImport(Integer isImport) {
        this.isImport = isImport;
    }

    public List<SwaggerInfo> getGroups() {
        return groups;
    }

    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    public void setBasicAuthUsername(String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthPassword(String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
    }

    public void setGroups(List<SwaggerInfo> groups) {
        this.groups = groups;
    }
}
