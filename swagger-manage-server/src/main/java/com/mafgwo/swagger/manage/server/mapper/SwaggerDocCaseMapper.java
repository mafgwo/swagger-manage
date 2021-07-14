package com.mafgwo.swagger.manage.server.mapper;

import com.mafgwo.swagger.manage.server.entity.SwaggerDocCase;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 接口用例 mapper
 *
 * @author chenxiaoqi
 * @since 2021/07/14
 */
@Mapper
public interface SwaggerDocCaseMapper {

    /**
     * 新增
     * @param swaggerDocCase
     * @return
     */
    int insert(SwaggerDocCase swaggerDocCase);

    /**
     * 删除
     * @param id
     * @return
     */
    @Delete("delete from swagger_doc_case where id = #{id}")
    int deleteById(Integer id);

    /**
     * 查询接口用例
     * @param swaggerId
     * @param path
     * @param method
     * @return
     */
    @Select("select * from swagger_doc_case where swagger_id = #{swaggerId} and path = #{path} and method = #{method} order by id desc")
    List<SwaggerDocCase> getCases(@Param("swaggerId") Integer swaggerId, @Param("path") String path, @Param("method") String method);
}
