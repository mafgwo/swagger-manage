/*
 * generate by https://gitee.com/durcframework/code-gen
 */
package com.mafgwo.swagger.manage.server.entity;


/**
 * swagger信息
 */
public class SwaggerInfo {
	private Integer id;
	/** project_info.id */
	private Integer projectId;
	/** 分组名称 */
	private String name;
	/** 文档地址 */
	private String url;
	/** 文档内容 */
	private String docContent;

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

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return this.url;
	}

	public void setDocContent(String docContent) {
		this.docContent = docContent;
	}

	public String getDocContent() {
		return this.docContent;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) {return false;}
		SwaggerInfo that = (SwaggerInfo) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SwaggerInfo{" +
				"id=" + id +
				",projectId='" + projectId + "'" +
				",name='" + name + "'" +
				",url='" + url + "'" +
				",docContent='" + docContent + "'" +
				'}';
	}

}