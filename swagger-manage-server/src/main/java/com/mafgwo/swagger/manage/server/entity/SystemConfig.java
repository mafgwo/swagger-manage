/*
 * generate by https://gitee.com/durcframework/code-gen
 */
package com.mafgwo.swagger.manage.server.entity;


/**
 * 系统配置
 */
public class SystemConfig {
	private Integer id;
	private Integer projectId;
	/** 配置类型，1：全局header */
	private Integer type;
	/** 配置key */
	private String configKey;
	/** 配置值 */
	private String configValue;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public Integer getProjectId() {
		return this.projectId;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getType() {
		return this.type;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	public String getConfigKey() {
		return this.configKey;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

	public String getConfigValue() {
		return this.configValue;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) {return false;}
		SystemConfig that = (SystemConfig) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SystemConfig{" +
				"id=" + id +
				",projectId='" + projectId + "'" +
				",type='" + type + "'" +
				",configKey='" + configKey + "'" +
				",configValue='" + configValue + "'" +
				'}';
	}

}