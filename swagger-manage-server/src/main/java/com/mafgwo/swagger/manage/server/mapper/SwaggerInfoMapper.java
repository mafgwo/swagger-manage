/*
 * generate by https://gitee.com/durcframework/code-gen
 */
package com.mafgwo.swagger.manage.server.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.mafgwo.swagger.manage.server.entity.SwaggerInfo;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SwaggerInfoMapper {

	List<SwaggerInfo> listByProjectId(int projectId);

	List<SwaggerInfo> listGroupByProjectId(int projectId);


	SwaggerInfo getFirst();

	int deleteByProjectId(@Param("projectId") int projectId);

	int deleteBatch(@Param("items") List<SwaggerInfo> items);

	/**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
	List<SwaggerInfo> listAll();


	/**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
	SwaggerInfo getById(Integer id);
	
	/**
     * 新增，插入所有字段
     *
     * @param docInfo 新增的记录
     * @return 返回影响行数
     */
	int insert(SwaggerInfo docInfo);
	
	/**
     * 新增，忽略null字段
     *
     * @param docInfo 新增的记录
     * @return 返回影响行数
     */
	int insertIgnoreNull(SwaggerInfo docInfo);
	
	/**
     * 修改，修改所有字段
     *
     * @param docInfo 修改的记录
     * @return 返回影响行数
     */
	int update(SwaggerInfo docInfo);
	
	/**
     * 修改，忽略null字段
     *
     * @param docInfo 修改的记录
     * @return 返回影响行数
     */
	int updateIgnoreNull(SwaggerInfo docInfo);
	
	/**
     * 删除记录
     *
     * @param docInfo 待删除的记录
     * @return 返回影响行数
     */
	int delete(SwaggerInfo docInfo);
	
}