package com.mafgwo.swagger.manage.server.controller.param;


/**
 * 系统配置
 */
public class SystemConfigParam {
	private Integer swaggerId;
	/** 配置key */
	private String configKey;
	/** 配置值 */
	private String configValue;

	public Integer getSwaggerId() {
		return swaggerId;
	}

	public void setSwaggerId(Integer swaggerId) {
		this.swaggerId = swaggerId;
	}

	public String getConfigKey() {
		return configKey;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

	@Override
	public String toString() {
		return "SystemConfigParam{" +
				"swaggerId=" + swaggerId +
				", configKey='" + configKey + '\'' +
				", configValue='" + configValue + '\'' +
				'}';
	}
}