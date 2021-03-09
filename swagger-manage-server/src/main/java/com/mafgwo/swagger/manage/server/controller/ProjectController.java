package com.mafgwo.swagger.manage.server.controller;

import com.mafgwo.swagger.manage.server.common.Result;
import com.mafgwo.swagger.manage.server.controller.vo.ProjectInfoVO;
import com.mafgwo.swagger.manage.server.entity.ProjectInfo;
import com.mafgwo.swagger.manage.server.entity.SwaggerInfo;
import com.mafgwo.swagger.manage.server.service.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author tanghc
 */
@RestController
@RequestMapping("project")
public class ProjectController {

    @Autowired
    private DocService docService;

    @GetMapping("/list/all")
    public Result list() {
        List<ProjectInfoVO> projectInfoVOS = docService.listAllProject();
        return Result.ok(projectInfoVOS);
    }

    @PostMapping("/importJson")
    public Result importJson(@RequestBody ProjectInfo param) {
        docService.importJson(param);
        return Result.ok();
    }

    @PostMapping("/add")
    public Result add(@RequestBody ProjectInfo projectInfo) {
        docService.addProject(projectInfo);
        return Result.ok();
    }

    @PostMapping("/update")
    public Result update(@RequestBody ProjectInfo projectInfo) {
        List<SwaggerInfo> swaggerInfoList = docService.updateProject(projectInfo);
        return Result.ok(swaggerInfoList);
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody ProjectInfo projectInfo) {
        docService.deleteProject(projectInfo);
        return Result.ok();
    }

}
