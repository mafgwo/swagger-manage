package com.mafgwo.swagger.manage.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.mafgwo.swagger.manage.server.common.HttpTool;
import com.mafgwo.swagger.manage.server.controller.vo.ProjectInfoVO;
import com.mafgwo.swagger.manage.server.doc.DocInfo;
import com.mafgwo.swagger.manage.server.doc.DocItem;
import com.mafgwo.swagger.manage.server.doc.DocModule;
import com.mafgwo.swagger.manage.server.doc.DocParser;
import com.mafgwo.swagger.manage.server.entity.ProjectInfo;
import com.mafgwo.swagger.manage.server.entity.SwaggerInfo;
import com.mafgwo.swagger.manage.server.mapper.ProjectInfoMapper;
import com.mafgwo.swagger.manage.server.mapper.SwaggerInfoMapper;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author tanghc
 */
@Service
public class DocService {

    private static final Logger log = LoggerFactory.getLogger(DocService.class);

    // key: DocItem.id
    private static final Map<String, DocItem> ITEM_MAP = new ConcurrentHashMap<>(128);

    @Autowired
    private SwaggerInfoMapper swaggerInfoMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    @Qualifier("swaggerDocParserV2")
    private DocParser docParserV2;

    @Autowired
    @Qualifier("swaggerDocParserV3")
    private DocParser docParserV3;

    @Value("${swagger.resources-path:/swagger-resources}")
    private String swaggerResourcesPath;

    @Value("${swagger.use-v2:true}")
    private String useV2;

    /**
     * 添加项目
     *
     * @param projectInfo 参数
     */
    @Transactional
    public void addProject(ProjectInfo projectInfo) {
        projectInfo.setIsImport(0);
        this.doAddProject(projectInfo);
        this.refreshSwaggerDoc(projectInfo);
    }

    public List<ProjectInfoVO> listAllProject() {
        List<ProjectInfo> projectInfos = projectInfoMapper.listAll();
        return projectInfos.stream()
                .map(projectInfo -> {
                    ProjectInfoVO projectInfoVO = new ProjectInfoVO();
                    BeanUtils.copyProperties(projectInfo, projectInfoVO);
                    List<SwaggerInfo> swaggerInfos = swaggerInfoMapper.listGroupByProjectId(projectInfo.getId());
                    projectInfoVO.setGroups(swaggerInfos);
                    return projectInfoVO;
                })
                .collect(Collectors.toList());
    }

    private ProjectInfo doAddProject(ProjectInfo projectInfo) {
        String host = projectInfo.getHost();
        if (projectInfoMapper.getByHost(host) != null) {
            throw new RuntimeException(host + "已存在");
        }
        projectInfoMapper.insertIgnoreNull(projectInfo);
        return projectInfo;
    }

    /**
     * 导入json
     *
     * @param projectInfo projectInfo
     */
    public List<SwaggerInfo> importJson(ProjectInfo projectInfo) {
        String json;
        String url = projectInfo.getHost();
        try {
            Response response = getHttpTool(projectInfo).requestForResponse(url, null, null, HttpTool.HTTPMethod.GET);
            String body = response.body().string();
            if (response.code() != HttpStatus.OK.value()) {
                throw new RuntimeException(body);
            }
            json = body;
        } catch (IOException e) {
            log.error("导入json异常, url:{}", url, e);
            throw new RuntimeException("导入json异常, msg:" + e.getMessage());
        }
        JSONObject docRoot = JSON.parseObject(json, Feature.OrderedField, Feature.DisableCircularReferenceDetect);
        DocParser docParser = docRoot.containsKey("openapi") ? docParserV3 : docParserV2;
        DocInfo docInfo = docParser.parseJson(json);

        projectInfo.setIsImport(1);
        projectInfo.setName(docInfo.getTitle());
        if (projectInfo.getId() != null && projectInfo.getId() > 0) {
            projectInfoMapper.updateIgnoreNull(projectInfo);
            List<SwaggerInfo> swaggerInfoList = swaggerInfoMapper.listByProjectId(projectInfo.getId());
            for (SwaggerInfo swaggerInfo : swaggerInfoList) {
                swaggerInfo.setName(docInfo.getTitle());
                swaggerInfo.setUrl(url);
                swaggerInfo.setDocContent(json);
                swaggerInfoMapper.updateIgnoreNull(swaggerInfo);
            }
            return swaggerInfoList;
        } else {
            this.doAddProject(projectInfo);
            SwaggerInfo swaggerInfo = new SwaggerInfo();
            swaggerInfo.setProjectId(projectInfo.getId());
            swaggerInfo.setName(docInfo.getTitle());
            swaggerInfo.setUrl(url);
            swaggerInfo.setDocContent(json);
            swaggerInfoMapper.insertIgnoreNull(swaggerInfo);
            return Collections.singletonList(swaggerInfo);
        }
    }

    public List<SwaggerInfo> updateProject(ProjectInfo projectInfo) {
        projectInfoMapper.updateIgnoreNull(projectInfo);
        return syncProject(projectInfo);
    }

    public void deleteProject(ProjectInfo projectInfo) {
        swaggerInfoMapper.deleteByProjectId(projectInfo.getId());
        projectInfoMapper.delete(projectInfo);
    }

    private List<SwaggerInfo> refreshSwaggerDoc(ProjectInfo projectInfo) {
        Integer projectId = projectInfo.getId();
        String swaggerResourceUrl = projectInfo.getHost() + swaggerResourcesPath;
        String swaggerGroupInfo;
        try {
            // [{"name":"default","url":"/v2/api-docs","swaggerVersion":"2.0","location":"/v2/api-docs"}]
            Response response = getHttpTool(projectInfo).requestForResponse(swaggerResourceUrl, null, null, HttpTool.HTTPMethod.GET);
            String body = response.body().string();
            if (response.code() != HttpStatus.OK.value()) {
                throw new RuntimeException(body);
            }
            swaggerGroupInfo = body;
        } catch (IOException e) {
            throw new RuntimeException("同步文档出错：" + e.getMessage(), e);
        }
        if (StringUtils.isEmpty(swaggerGroupInfo)) {
            throw new RuntimeException("同步文档出错：文档内容不存在");
        }
        List<SwaggerResource> swaggerResources = JSON.parseArray(swaggerGroupInfo, SwaggerResource.class);
        if (CollectionUtils.isEmpty(swaggerResources)) {
            throw new RuntimeException("swagger还未初始化完成，等会再试");
        }
        SwaggerResource swaggerResource = swaggerResources.get(0);
        List<SwaggerInfo> swaggerInfoList = swaggerInfoMapper.listByProjectId(projectId);
        SwaggerInfo swaggerInfo = swaggerInfoList.size() > 0 ? swaggerInfoList.get(0) : new SwaggerInfo();
        String name = swaggerResource.name;
        String projectName = projectInfo.getName();
        if ("default".equals(name)) {
            name = StringUtils.isEmpty(projectName) ? projectInfo.getHost() : projectName;
        }
        String groupUrl = this.getGroupUrl(swaggerResource);
        String swaggerUrl = projectInfo.getHost() + groupUrl;
        String docContent = this.fetchSwaggerDoc(swaggerUrl, projectInfo);
        // 新增
        if (swaggerInfo.getId() == null) {
            swaggerInfo.setProjectId(projectId);
            swaggerInfo.setName(name);
            swaggerInfo.setUrl(groupUrl);
            swaggerInfo.setDocContent(docContent);
            swaggerInfoMapper.insertIgnoreNull(swaggerInfo);
        } else {
            // 修改
            swaggerInfo.setUrl(groupUrl);
            swaggerInfo.setDocContent(docContent);
            swaggerInfoMapper.update(swaggerInfo);
        }
        return swaggerInfoList;

    }

    private HttpTool getHttpTool(ProjectInfo projectInfo) {
        String basicAuthUsername = projectInfo.getBasicAuthUsername();
        String basicAuthPassword = projectInfo.getBasicAuthPassword();
        return new HttpTool(basicAuthUsername, basicAuthPassword);
    }

    private String getGroupUrl(SwaggerResource swaggerResource) {
        String url = swaggerResource.url;
        if (StringUtils.isEmpty(url)) {
            url = swaggerResource.location;
        }
        // 强制使用v2的json
        if ("true".equals(useV2)) {
            url = url.replace("v3/api-docs", "v2/api-docs");
        }
        return url;
    }

    /**
     * 获取文档内容
     *
     * @param swaggerId swaggerId
     * @return 返回文档对象
     */
    public DocInfo getDoc(int swaggerId) {
        SwaggerInfo docInfo = swaggerInfoMapper.getById(swaggerId);
        Objects.requireNonNull(docInfo, "文档配置不存在");
        return getDoc(docInfo);
    }

    public DocItem getByItemId(Integer swaggerId, String path, String method) {
        String itemKey = this.getItemKey(swaggerId, path, method);
        DocItem docItem = ITEM_MAP.get(itemKey);
        if (docItem == null) {
            this.getDoc(swaggerId);
        }
        return ITEM_MAP.get(itemKey);
    }

    /**
     * 同步项目文档
     *
     * @param projectId 项目id
     * @return 返回同步后的文档
     */
    private List<SwaggerInfo> syncProject(int projectId) {
        ProjectInfo projectInfo = projectInfoMapper.getById(projectId);
        return syncProject(projectInfo);
    }

    /**
     * 同步项目文档
     *
     * @param projectInfo 项目
     * @return 返回同步后的文档
     */
    public List<SwaggerInfo> syncProject(ProjectInfo projectInfo) {
        // 如果是导入的项目
        if (projectInfo.getIsImport() == 1) {
            return this.importJson(projectInfo);
        } else {
            return this.refreshSwaggerDoc(projectInfo);
        }
    }

    /**
     * 同步文档内容
     *
     * @param swaggerId 文档id
     * @return 返回最新的文档信息
     */
    public List<SwaggerInfo> syncDoc(int swaggerId) {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(swaggerId);
        return this.syncProject(swaggerInfo.getProjectId());
    }

    /**
     * 同步文档内容
     *
     * @param swaggerInfo 文档配置
     * @return 返回最新的文档内容
     */
    private String syncDoc(SwaggerInfo swaggerInfo) {
        Integer projectId = swaggerInfo.getProjectId();
        ProjectInfo projectInfo = projectInfoMapper.getById(projectId);
        String swaggerDocUrl = projectInfo.getHost() + swaggerInfo.getUrl();
        String body = this.fetchSwaggerDoc(swaggerDocUrl, projectInfo);
        // 设置doc内容
        swaggerInfo.setDocContent(body);
        swaggerInfoMapper.update(swaggerInfo);
        return body;
    }

    /**
     * 获取swagger文档内容
     *
     * @param url         请求路径
     * @param projectInfo projectInfo
     * @return 返回文档内容
     */
    private String fetchSwaggerDoc(String url, ProjectInfo projectInfo) {
        String body = null;
        Response response = null;
        try {
            response = getHttpTool(projectInfo).requestForResponse(url, Collections.emptyMap(), Collections.emptyMap(), HttpTool.HTTPMethod.GET);
            if (response.code() != HttpStatus.OK.value()) {
                throw new RuntimeException("请求服务器错误，httpStatus:" + response.code() + ", body:" + HttpTool.parseBody(response));
            }
            body = response.body().string();
        } catch (IOException e) {
            log.error("同步文档失败, url:{}", url, e);
            throw new RuntimeException("同步文档失败:" + e.getMessage());
        } finally {
            HttpTool.close(response);
        }
        return body;
    }

    /**
     * 获取文档内容
     *
     * @param swaggerInfo 文档配置
     * @return 返回文档对象
     */
    private DocInfo getDoc(SwaggerInfo swaggerInfo) {
        String docContent = swaggerInfo.getDocContent();
        if (StringUtils.isEmpty(docContent)) {
            docContent = syncDoc(swaggerInfo);
        }
        return this.parseDoc(docContent, swaggerInfo);
    }

    private DocInfo parseDoc(String docContent, SwaggerInfo swaggerInfo) {
        String url = swaggerInfo.getUrl();
        DocParser docParser = this.getSwaggerDocParser(url, docContent);
        DocInfo docInfo = docParser.parseJson(docContent);
        docInfo.setProjectId(swaggerInfo.getProjectId());
        docInfo.setSwaggerId(swaggerInfo.getId());
        ProjectInfo projectInfo = projectInfoMapper.getById(swaggerInfo.getProjectId());
        this.setProperties(docInfo, docItem -> {
            docItem.setProjectId(docInfo.getProjectId());
            docItem.setSwaggerId(swaggerInfo.getId());
            // 网关地址：
            if (projectInfo != null && !StringUtils.isEmpty(projectInfo.getGatewayRule()) && !StringUtils.isEmpty(projectInfo.getGatewayHost())) {
                String[] split = projectInfo.getGatewayRule().split(",");
                if (split.length == 2) {
                    String regexp = split[0].trim();
                    String replacement = split[1].trim().replace("$\\", "$");
                    if (docItem.getPath().matches(regexp)) {
                        String newPath = docItem.getPath().replaceAll(regexp, replacement);
                        docItem.setGatewayUrl(projectInfo.getGatewayHost() + newPath);
                    }
                }
            }
//            ITEM_MAP.put(docItem.getId(), docItem);
            ITEM_MAP.put(this.getItemKey(swaggerInfo.getId(), docItem.getPath(), docItem.getMethod()), docItem);
        });
        return docInfo;
    }

    private void setProperties(DocInfo docInfo, Consumer<DocItem> consumer) {
        List<DocModule> docModules = docInfo.getDocModules();
        docModules.forEach(docModule -> {
            docModule.getItems().forEach(consumer);
        });
    }

    private DocParser getSwaggerDocParser(String url, String json) {
        JSONObject docRoot = JSON.parseObject(json, Feature.OrderedField, Feature.DisableCircularReferenceDetect);
        return docRoot.containsKey("openapi") ? docParserV3 : docParserV2;
    }

    private static class SwaggerResource {
        private String name;
        private String url;
        private String location;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    private String getItemKey(Integer swaggerId, String path, String method) {
        return swaggerId + "_" + path + "_" + method;
    }

}
