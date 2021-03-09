package com.mafgwo.swagger.manage.server.controller;

import com.mafgwo.swagger.manage.server.common.Result;
import com.mafgwo.swagger.manage.server.controller.param.DebugHostParam;
import com.mafgwo.swagger.manage.server.controller.param.ListParam;
import com.mafgwo.swagger.manage.server.controller.param.SystemConfigParam;
import com.mafgwo.swagger.manage.server.controller.vo.SystemConfigVO;
import com.mafgwo.swagger.manage.server.entity.SwaggerInfo;
import com.mafgwo.swagger.manage.server.entity.SystemConfig;
import com.mafgwo.swagger.manage.server.mapper.SwaggerInfoMapper;
import com.mafgwo.swagger.manage.server.mapper.SystemConfigMapper;
import com.mafgwo.swagger.manage.server.service.SystemConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tanghc
 */
@RestController
@RequestMapping("systemconfig")
public class SystemConfigController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private SwaggerInfoMapper swaggerInfoMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/get")
    public Result get(@RequestParam("swaggerId") int swaggerId) {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(swaggerId);
        List<SystemConfig> globalHeaders = systemConfigMapper.listGlobalHeaders(swaggerInfo.getProjectId());
        String allowMethods = systemConfigService.getAllowMethods();
        List<String> methodList = Stream.of(allowMethods.split(",")).collect(Collectors.toList());
        String debugHost = systemConfigService.getDebugHost(swaggerInfo.getProjectId());
        SystemConfigVO systemConfigVO = new SystemConfigVO();
        systemConfigVO.setGlobalHeaders(globalHeaders);
        systemConfigVO.setAllowMethods(methodList);
        systemConfigVO.setDebugHost(debugHost);
        return Result.ok(systemConfigVO);
    }

    @PostMapping("/allowMethod/set")
    public Result allowMethod(@RequestBody ListParam param) {
        SystemConfig commonConfig = systemConfigMapper.getCommonConfig(SystemConfigService.KEY_ALLOW_METHODS);
        List<String> list = param.getList();
        if (CollectionUtils.isEmpty(list)) {
            list = Collections.singletonList("GET");
        }
        String value = String.join(",", list);
        if (commonConfig == null) {
            commonConfig = new SystemConfig();
            commonConfig.setProjectId(0);
            commonConfig.setType(SystemConfigService.TYPE_COMMON);
            commonConfig.setConfigKey(SystemConfigService.KEY_ALLOW_METHODS);
            commonConfig.setConfigValue(value);
            systemConfigMapper.insertIgnoreNull(commonConfig);
        } else {
            commonConfig.setConfigValue(value);
            systemConfigMapper.update(commonConfig);
        }
        return Result.ok();
    }

    @PostMapping("/debughost/set")
    public Result debughost(@RequestBody DebugHostParam param) {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(param.getSwaggerId());
        String key = SystemConfigService.getDebugHostKey(swaggerInfo.getProjectId());
        SystemConfig commonConfig = systemConfigService.getCommonConfig(key);
        String value = param.getDebugHost();
        if (commonConfig == null) {
            commonConfig = new SystemConfig();
            commonConfig.setProjectId(swaggerInfo.getProjectId());
            commonConfig.setType(SystemConfigService.TYPE_COMMON);
            commonConfig.setConfigKey(key);
            commonConfig.setConfigValue(value);
            systemConfigMapper.insertIgnoreNull(commonConfig);
        } else {
            commonConfig.setConfigValue(value);
            systemConfigMapper.update(commonConfig);
        }
        return Result.ok();
    }

    @PostMapping("/globalHeader/add")
    public Result addHadder(@RequestBody SystemConfigParam systemConfigParam) {
        SystemConfig systemConfig = new SystemConfig();
        BeanUtils.copyProperties(systemConfigParam, systemConfig);
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(systemConfigParam.getSwaggerId());
        systemConfig.setProjectId(swaggerInfo.getProjectId());
        systemConfig.setType(SystemConfigService.TYPE_HEADERS);
        systemConfigMapper.insertIgnoreNull(systemConfig);
        return Result.ok();
    }

    @GetMapping("/globalHeader/list")
    public Result listHeader(@RequestParam("swaggerId") int swaggerId) {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(swaggerId);
        List<SystemConfig> systemConfigs = systemConfigMapper.listGlobalHeaders(swaggerInfo.getProjectId());
        return Result.ok(systemConfigs);
    }

    @PostMapping("/globalHeader/update")
    public Result updateHeader(@RequestBody SystemConfig systemConfig) {
        systemConfigMapper.update(systemConfig);
        return Result.ok();
    }

    @PostMapping("/globalHeader/delete")
    public Result deleteHeader(@RequestBody SystemConfig systemConfig) {
        systemConfigMapper.delete(systemConfig);
        return Result.ok();
    }

}
