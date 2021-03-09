package com.mafgwo.swagger.manage.server.controller;

import com.mafgwo.swagger.manage.server.common.Result;
import com.mafgwo.swagger.manage.server.entity.SwaggerInfo;
import com.mafgwo.swagger.manage.server.mapper.SwaggerInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author tanghc
 */
@RestController
@RequestMapping("swagger")
public class SwaggerInfoController {

    @Autowired
    private SwaggerInfoMapper swaggerInfoMapper;

    @GetMapping("/list/{projectId}")
    public Result list(@PathVariable("projectId") int projectId) {
        List<SwaggerInfo> serverConfigs = swaggerInfoMapper.listByProjectId(projectId);
        return Result.ok(serverConfigs);
    }

    @GetMapping("/group/{projectId}")
    public Result group(@PathVariable("projectId") int projectId) {
        List<SwaggerInfo> serverConfigs = swaggerInfoMapper.listGroupByProjectId(projectId);
        return Result.ok(serverConfigs);
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(id);
        return Result.ok(swaggerInfo);
    }

    @GetMapping("/get/first")
    public Result first() {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getFirst();
        return Result.ok(swaggerInfo);
    }

    @PostMapping("/add")
    public Result add(@RequestBody SwaggerInfo docInfo) {
        swaggerInfoMapper.insertIgnoreNull(docInfo);
        return Result.ok(docInfo);
    }

    @PostMapping("/update")
    public Result update(@RequestBody SwaggerInfo docInfo) {
        swaggerInfoMapper.update(docInfo);
        return Result.ok();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody SwaggerInfo docInfo) {
        swaggerInfoMapper.delete(docInfo);
        return Result.ok();
    }

}
