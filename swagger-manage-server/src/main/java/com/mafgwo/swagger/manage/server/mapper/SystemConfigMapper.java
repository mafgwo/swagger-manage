/*
 * generate by https://gitee.com/durcframework/code-gen
 */
package com.mafgwo.swagger.manage.server.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.mafgwo.swagger.manage.server.entity.SystemConfig;

@Mapper
public interface SystemConfigMapper {

	List<SystemConfig> listGlobalHeaders(int projectId);

	SystemConfig getCommonConfig(String key);

	/**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
	List<SystemConfig> listAll();


	/**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
	SystemConfig getById(Integer id);
	
	/**
     * 新增，插入所有字段
     *
     * @param systemConfig 新增的记录
     * @return 返回影响行数
     */
	int insert(SystemConfig systemConfig);
	
	/**
     * 新增，忽略null字段
     *
     * @param systemConfig 新增的记录
     * @return 返回影响行数
     */
	int insertIgnoreNull(SystemConfig systemConfig);
	
	/**
     * 修改，修改所有字段
     *
     * @param systemConfig 修改的记录
     * @return 返回影响行数
     */
	int update(SystemConfig systemConfig);
	
	/**
     * 修改，忽略null字段
     *
     * @param systemConfig 修改的记录
     * @return 返回影响行数
     */
	int updateIgnoreNull(SystemConfig systemConfig);
	
	/**
     * 删除记录
     *
     * @param systemConfig 待删除的记录
     * @return 返回影响行数
     */
	int delete(SystemConfig systemConfig);
	
}