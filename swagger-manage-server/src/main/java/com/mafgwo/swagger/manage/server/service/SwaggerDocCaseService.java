package com.mafgwo.swagger.manage.server.service;

import com.mafgwo.swagger.manage.server.controller.vo.SwaggerDocCaseAddVO;
import com.mafgwo.swagger.manage.server.entity.SwaggerDocCase;
import com.mafgwo.swagger.manage.server.mapper.SwaggerDocCaseMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * swagegr 接口用例 service
 * @author chenxiaoqi
 * @since 2021/07/14
 */
@Service
public class SwaggerDocCaseService {

    @Autowired
    private SwaggerDocCaseMapper swaggerDocCaseMapper;

    /**
     * 新增
     * @param swaggerDocCaseAddVO
     * @return
     */
    public SwaggerDocCase add(SwaggerDocCaseAddVO swaggerDocCaseAddVO) {
        SwaggerDocCase swaggerDocCase = new SwaggerDocCase();
        BeanUtils.copyProperties(swaggerDocCaseAddVO, swaggerDocCase);
        swaggerDocCase.setCreatedAt(new Date());
        swaggerDocCaseMapper.insert(swaggerDocCase);
        return swaggerDocCase;
    }

    /**
     * 过去接口用例
     * @param swaggerId
     * @param path
     * @param method
     * @return
     */
    public List<SwaggerDocCase> getCases(Integer swaggerId, String path, String method) {
        return swaggerDocCaseMapper.getCases(swaggerId, path, method);
    }

    /**
     * 根据ID删除
     * @param id
     */
    public void delete(Integer id) {
        swaggerDocCaseMapper.deleteById(id);
    }
}
