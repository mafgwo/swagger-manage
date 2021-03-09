/*
 * generate by https://gitee.com/durcframework/code-gen
 */
package com.mafgwo.swagger.manage.server.entity;


/**
 * 项目信息
 */
public class ProjectInfo {
	private Integer id;
	/** 项目名称 */
	private String name;
	/** 网关规则 */
	private String gatewayRule;
	/** 网关host */
	private String gatewayHost;
	/** 访问地址，如：http://localhost:8080 */
	private String host;
	/** 是否导入的项目，1：是，0：否 */
	private Integer isImport;
	/** basic认证用户名 */
	private String basicAuthUsername;
	/** basic认证密码 */
	private String basicAuthPassword;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
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

	public String getHost() {
		return this.host;
	}

	public void setIsImport(Integer isImport) {
		this.isImport = isImport;
	}

	public Integer getIsImport() {
		return this.isImport;
	}

	public void setBasicAuthUsername(String basicAuthUsername) {
		this.basicAuthUsername = basicAuthUsername;
	}

	public String getBasicAuthUsername() {
		return this.basicAuthUsername;
	}

	public void setBasicAuthPassword(String basicAuthPassword) {
		this.basicAuthPassword = basicAuthPassword;
	}

	public String getBasicAuthPassword() {
		return this.basicAuthPassword;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) {return false;}
		ProjectInfo that = (ProjectInfo) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectInfo{" +
				"id=" + id +
				",name='" + name + "'" +
				",host='" + host + "'" +
				",isImport='" + isImport + "'" +
				",basicAuthUsername='" + basicAuthUsername + "'" +
				",basicAuthPassword='" + basicAuthPassword + "'" +
				'}';
	}

}