/*
 * generate by https://gitee.com/durcframework/code-gen
 */
package com.mafgwo.swagger.manage.server.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.mafgwo.swagger.manage.server.entity.ProjectInfo;

@Mapper
public interface ProjectInfoMapper {

	/**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
	List<ProjectInfo> listAll();


	/**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
	ProjectInfo getById(Integer id);
	
	/**
     * 新增，插入所有字段
     *
     * @param projectInfo 新增的记录
     * @return 返回影响行数
     */
	int insert(ProjectInfo projectInfo);
	
	/**
     * 新增，忽略null字段
     *
     * @param projectInfo 新增的记录
     * @return 返回影响行数
     */
	int insertIgnoreNull(ProjectInfo projectInfo);
	
	/**
     * 修改，修改所有字段
     *
     * @param projectInfo 修改的记录
     * @return 返回影响行数
     */
	int update(ProjectInfo projectInfo);
	
	/**
     * 修改，忽略null字段
     *
     * @param projectInfo 修改的记录
     * @return 返回影响行数
     */
	int updateIgnoreNull(ProjectInfo projectInfo);
	
	/**
     * 删除记录
     *
     * @param projectInfo 待删除的记录
     * @return 返回影响行数
     */
	int delete(ProjectInfo projectInfo);

	ProjectInfo getByHost(String host);
}