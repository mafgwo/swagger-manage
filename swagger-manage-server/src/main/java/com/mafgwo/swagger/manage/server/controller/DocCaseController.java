package com.mafgwo.swagger.manage.server.controller;

import com.mafgwo.swagger.manage.server.common.Result;
import com.mafgwo.swagger.manage.server.controller.vo.SwaggerDocCaseAddVO;
import com.mafgwo.swagger.manage.server.entity.SwaggerDocCase;
import com.mafgwo.swagger.manage.server.service.SwaggerDocCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户保存获取controller
 *
 * @author chenxiaoqi
 * @since 2021/07/14
 */
@RestController
@RequestMapping("doc-cases")
public class DocCaseController {

    @Autowired
    private SwaggerDocCaseService swaggerDocCaseService;

    /**
     * 新增接口用例
     * @param swaggerDocCaseAddVO
     * @return
     */
    @PostMapping
    public Result addCase(@RequestBody @Validated SwaggerDocCaseAddVO swaggerDocCaseAddVO) {
        SwaggerDocCase docCase = swaggerDocCaseService.add(swaggerDocCaseAddVO);
        return Result.ok(docCase);
    }

    /**
     * 获取接口的所有用例
     * @param swaggerId
     * @param swaggerId
     * @param swaggerId
     * @return
     */
    @GetMapping
    public Result getCases(@RequestParam Integer swaggerId, @RequestParam String path, @RequestParam String method) {
        List<SwaggerDocCase> docCases = swaggerDocCaseService.getCases(swaggerId, path, method);
        return Result.ok(docCases);
    }

    /**
     * 删除接口用例
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    public Result delete(@PathVariable Integer id) {
        swaggerDocCaseService.delete(id);
        return Result.ok();
    }


}
