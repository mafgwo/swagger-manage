package com.mafgwo.swagger.manage.server.doc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.parser.Feature;
import com.mafgwo.swagger.manage.server.service.SystemConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 解析swagger3的json内容
 *
 * @author tanghc
 */
@Service("swaggerDocParserV3")
public class SwaggerDocParserV3 implements DocParser {

    private final Set<String> cycleCache = new HashSet<>(8);

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public DocInfo parseJson(String swaggerJson) {
        cycleCache.clear();
        JSONObject docRoot = JSON.parseObject(swaggerJson, Feature.OrderedField, Feature.DisableCircularReferenceDetect);
        JSONObject info = docRoot.getJSONObject("info");
        String requestUrl = this.getRequestUrl(docRoot);
        List<DocItem> docItems = new ArrayList<>();
        String allowMethod = systemConfigService.getAllowMethods();

        JSONObject paths = docRoot.getJSONObject("paths");
        if (paths == null) {
            paths = new JSONObject();
        }
        Set<String> pathNameSet = paths.keySet();
        for (String path : pathNameSet) {
            JSONObject pathInfo = paths.getJSONObject(path);
            Map<String, List<DocItem>> sameMap = new HashMap<>(16);
            // key: get,post,head...
            Set<String> httpMethodList = pathInfo.keySet();
            for (String method : httpMethodList) {
                ApiInfo apiInfo = new ApiInfo();
                apiInfo.setRequestUrl(requestUrl);
                apiInfo.setPath(path);
                apiInfo.setMethod(method);

                JSONObject docInfo = pathInfo.getJSONObject(method);
                DocItem docItem = buildDocItem(apiInfo, docInfo, docRoot);
                String key = docItem.getSummary() + docItem.getDescription();
                List<DocItem> docItemList = sameMap.computeIfAbsent(key, (k) -> new ArrayList<>());
                docItemList.add(docItem);
            }

            if (sameMap.size() == 1 && httpMethodList.size() != 1) {
                String key = sameMap.keySet().iterator().next();
                List<DocItem> docItemList = sameMap.get(key);
                // 使用了@RequestMapping，没有指定方法
                if (docItemList.size() == httpMethodList.size()) {
                    String allowMethodTemp = allowMethod;
                    // 取其中一个，判断是否是文件上传接口
                    boolean uploadRequest = docItemList.get(0).getUploadRequest();
                    // 如果是上传文件接口强制使用POST
                    if (uploadRequest) {
                        allowMethodTemp = HttpMethod.POST.name();
                    }
                    for (DocItem docItem : docItemList) {
                        if (allowMethodTemp.contains(docItem.getMethod())) {
                            docItems.add(docItem);
                        }
                    }
                }
            } else {
                for (List<DocItem> docItemList : sameMap.values()) {
                    docItems.addAll(docItemList);
                }
            }
        }
        // 先根据路径排序，再根据描述排序
        docItems.sort(Comparator.comparing(DocItem::getPath).thenComparing(DocItem::getSummary));

        List<DocModule> docModuleList = docItems.stream()
                .collect(Collectors.groupingBy(DocItem::getModule))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<DocItem> docItemList = entry.getValue();
                    DocModule docModule = new DocModule();
                    docModule.setModule(entry.getKey());
                    docModule.setItems(docItemList);
                    return docModule;
                })
                // 模块排序
                .sorted(Comparator.comparing(DocModule::getOrder).thenComparing(DocModule::getModule))
                .collect(Collectors.toList());

        DocInfo docInfo = new DocInfo();

        docInfo.setTitle(info.getString("title"));
        docInfo.setVersion(info.getString("version"));
        docInfo.setDescription(info.getString("description"));
        docInfo.setRequestUrl(requestUrl);
        docInfo.setDocModules(docModuleList);
        return docInfo;
    }

    private DocItem buildDocItem(ApiInfo apiInfo, JSONObject docInfo, JSONObject docRoot) {
        DocItem docItem = new DocItem();
//        docItem.setId(DigestUtils.md5DigestAsHex((apiInfo.toString()).getBytes(StandardCharsets.UTF_8)));
        docItem.setRequestUrl(apiInfo.getRequestUrl());
        docItem.setPath(apiInfo.getPath());
        docItem.setMethod(apiInfo.getMethod().toUpperCase());
        String summary = docInfo.getString("summary");
        docItem.setSummary(StringUtils.isEmpty(summary) ? apiInfo.getPath() : summary);
        docItem.setDescription(docInfo.getString("description"));
        docItem.setMultiple(docInfo.getString("multiple") != null);
        docItem.setProduces(getProduces(docInfo));
        docItem.setConsumes(getConsumes(docInfo));
        String moduleName = this.buildModuleName(docInfo, docRoot);
        docItem.setModule(moduleName);
        List<DocParameter> requestParameterList = this.buildRequestParameterList(apiInfo, docInfo, docRoot);
        boolean hasUploadParam = hasUploadParam(requestParameterList);
        docItem.setUploadRequest(hasUploadParam);
        docItem.setRequestParameters(requestParameterList);

        // 清除缓存，可以再次使用
        cycleCache.clear();

        List<DocParameter> responseParameterList = this.buildResponseParameterList(apiInfo, docInfo, docRoot);
        docItem.setResponseParameters(responseParameterList);

        return docItem;
    }

    private boolean hasUploadParam(List<DocParameter> docParameterList) {
        for (DocParameter requestParameter : docParameterList) {
            String type = requestParameter.getType();
            if (DataType.FILE.equalsIgnoreCase(type) || DataType.FILE.equals(requestParameter.getElementType())) {
                return true;
            }
        }
        return false;
    }

    private String buildModuleName(JSONObject docInfo, JSONObject docRoot) {
        String title = docRoot.getJSONObject("info").getString("title");
        JSONArray tags = docInfo.getJSONArray("tags");
        if (tags != null && tags.size() > 0) {
            return tags.getString(0);
        }
        return title;
    }

    private List<DocParameter> buildRequestParameterList(ApiInfo apiInfo, JSONObject docInfo, JSONObject docRoot) {
        JSONArray parameters = this.getParameterObject(docInfo);
        List<DocParameter> docParameterList = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject fieldJson = parameters.getJSONObject(i);
            JSONObject schemaJson = fieldJson.getJSONObject("schema");
            RefInfo refInfo = getRefInfo(schemaJson);
            if (refInfo == null) {
                if (schemaJson != null && "array".equals(schemaJson.getString("type")) && schemaJson.getJSONObject("items") != null) {
                    DocParameter docParameter = fieldJson.toJavaObject(DocParameter.class);
                    docParameter.setIn("body");
                    docParameter.setType("array" + "(" + schemaJson.getJSONObject("items").getString("type") + ")");
                    docParameterList.add(docParameter);
                    return formatDocParameters(docParameterList);
                }
                refInfo = getRefInfo(fieldJson.getJSONObject("items"));
            }
            if (refInfo != null) {
                if (refInfo.getRef().contains("MultipartFile")) {
                    DocParameter docParameter = fieldJson.toJavaObject(DocParameter.class);
                    docParameter.setType("file");
                    docParameterList.add(docParameter);
                } else {
                    String fieldType = getFieldType(fieldJson);
                    List<DocParameter> parameterList = this.buildDocParameters(apiInfo, refInfo, docRoot, true);
                    parameterList.forEach(docParameter -> docParameter.setParentType(fieldType));
                    docParameterList.addAll(parameterList);
                }
            } else {
                fieldJson = formatFieldJson(docRoot, fieldJson);
                DocParameter docParameter = fieldJson.toJavaObject(DocParameter.class);
                // @RequestBody String reqEntity
                // 这种情况下name为null
                if (docParameter.getName() == null) {
                    continue;
                }
                this.setType(fieldJson, docParameter);
                docParameterList.add(docParameter);
            }
        }
        return formatDocParameters(docParameterList);
    }

    private List<DocParameter> formatDocParameters(List<DocParameter> docParameterList) {
        Map<String, List<DocParameter>> collect = docParameterList.stream()
                .filter(docParameter -> docParameter.getName() != null && docParameter.getName().contains("."))
                .map(docParameter -> {
                    String name = docParameter.getName();
                    int index = name.indexOf('.');
                    String module = name.substring(0, index);
                    String newName = name.substring(index + 1);
                    DocParameter ret = new DocParameter();
                    BeanUtils.copyProperties(docParameter, ret);
                    ret.setName(newName);
                    ret.setModule(module);
                    return ret;
                })
                .collect(Collectors.groupingBy(DocParameter::getModule));

        collect.forEach((key, value) -> {
            DocParameter moduleDoc = new DocParameter();
            moduleDoc.setName(key);
            moduleDoc.setType("object");
            moduleDoc.setRefs(value);
            docParameterList.add(moduleDoc);
        });

        return docParameterList.stream()
                .filter(docParameter -> docParameter.getName() == null || !docParameter.getName().contains("."))
                .collect(Collectors.toList());
    }

    private void setType(JSONObject fieldJson, DocParameter docParameter) {
        String type = getFieldType(fieldJson);
        docParameter.setType(type);
        if (DataType.ARRAY.equals(type)) {
            JSONObject items = fieldJson.getJSONObject("items");
            Optional<String> optionalType = Optional.ofNullable(items)
                    .map(jsonObject -> jsonObject.getString("type"));
            if (!optionalType.isPresent()) {
                optionalType = Optional.ofNullable(items)
                        .flatMap(jsonObject -> {
                            String ref = jsonObject.getString("$ref");
                            String refName = getRefName(ref);
                            return Optional.ofNullable(refName);
                        });
            }

            String elementType = optionalType.orElseGet(() -> Optional.ofNullable(fieldJson.getJSONObject("schema"))
                    .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("items")))
                    .map(jsonObject -> {
                        String actType = jsonObject.getString("type");
                        if (actType == null) {
                            String $ref = jsonObject.getString("$ref");
                            return getRefName($ref);
                        }
                        return actType;
                    })
                    .orElse("string"));
            docParameter.setElementType(elementType);
        } else if (type == null) {
            String refStr = fieldJson.getString("$ref");
            if (refStr != null) {
                docParameter.setElementType(refStr.substring(refStr.lastIndexOf("/") + 1));
            }
        }
        this.setEnum(fieldJson, docParameter);
    }



    private void setEnum(JSONObject fieldJson, DocParameter docParameter) {
        JSONArray enumsArr = getEnum(fieldJson);
        // 如果是枚举字段
        if (enumsArr != null) {
            docParameter.setEnums(enumsArr.toJavaList(String.class));
            docParameter.setType(DataType.ENUM);
        }
    }

    private JSONArray getEnum(JSONObject fieldJson) {
        JSONArray array = fieldJson.getJSONArray("enum");
        if (array == null) {
            array = Optional.ofNullable(fieldJson.getJSONObject("schema"))
                    .flatMap(schema -> Optional.ofNullable(schema.getJSONArray("enum")))
                    .orElse(null);
        }
        return array;
    }

    private List<DocParameter> buildResponseParameterList(ApiInfo apiInfo, JSONObject docInfo, JSONObject docRoot) {
        if (isJsonArray(docInfo)) {
            DocParameter docParameter = new DocParameter();
            docParameter.setType(DataType.ARRAY);
            return Collections.singletonList(docParameter);
        } else {
            RefInfo refInfo = getResponseRefInfo(docInfo);
            List<DocParameter> respParameterList = Collections.emptyList();
            if (refInfo != null) {
                respParameterList = this.buildDocParameters(apiInfo, refInfo, docRoot, true);
                // 如果返回数组
                if (refInfo.getIsArray()) {
                    DocParameter docParameter = new DocParameter();
                    docParameter.setName(refInfo.getRef());
                    docParameter.setType(DataType.ARRAY);
                    docParameter.setRefs(respParameterList);
                    respParameterList = Collections.singletonList(docParameter);
                }
            }
            return respParameterList;
        }
    }

    private boolean isJsonArray(JSONObject docInfo) {
        return Optional.ofNullable(docInfo.getJSONObject("responses"))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("200")))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("content")))
                .map(jsonObject -> {
                    String mediaType = jsonObject.keySet().iterator().next();
                    JSONObject schema = jsonObject
                            .getJSONObject(mediaType)
                            .getJSONObject("schema");
                    if (mediaType.contains("json")) {
                        boolean isArray = DataType.ARRAY.equals(schema.getString("type"));
                        if (isArray) {
                            JSONObject items = schema.getJSONObject("items");
                            String elementType = items.getString("type");
                            return elementType != null;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    private List<DocParameter> buildDocParameters(ApiInfo apiInfo, RefInfo currentRef, JSONObject docRoot, boolean doSubRef) {
        String ref = currentRef.getRef();
        JSONObject responseObject = getDefinitions(docRoot, currentRef).getJSONObject(ref);
        if (responseObject == null) {
            return Collections.emptyList();
        }
        JSONArray required = responseObject.getJSONArray("required");
        String className = responseObject.getString("title");
        JSONObject properties = responseObject.getJSONObject("properties");
        List<DocParameter> docParameterList = new ArrayList<>();
        if (properties == null) {
            return docParameterList;
        }
        Set<String> fieldNames = properties.keySet();
        for (String fieldName : fieldNames) {
            /*
               {
                  "description": "分类故事",
                  "$ref": "#/definitions/StoryVO"
                }
             */
            JSONObject fieldInfo = properties.getJSONObject(fieldName);
            DocParameter docParameter = fieldInfo.toJavaObject(DocParameter.class);
            docParameter.setIn("body");
            docParameter.setName(fieldName);
            if (required != null && required.contains(fieldName)) {
                docParameter.setRequired(true);
            }
            this.setType(fieldInfo, docParameter);
            RefInfo refInfo = this.getRefInfo(fieldInfo);
            if (refInfo != null) {
                docParameter.setType(DataType.OBJECT);
            }
            if (refInfo != null && doSubRef) {
                String subRef = refInfo.getRef();
                String key = apiInfo.toString() + className + fieldName + ref + subRef;
                // 避免对象相互依赖导致无限循环
                boolean nextDoRef = !isSameRef(ref, subRef) && !cycleCache.contains(key);
                if (nextDoRef) {
                    cycleCache.add(key);
                }
                List<DocParameter> refs = buildDocParameters(apiInfo, refInfo, docRoot, nextDoRef);
                docParameter.setType(refInfo.getIsArray() ? DataType.ARRAY : DataType.OBJECT);
                docParameter.setRefs(refs);
            }
            this.setEnum(fieldInfo, docParameter);
            docParameterList.add(docParameter);
        }
        return docParameterList;
    }


    private boolean isSameRef(String ref, String subRef) {
        return Objects.equals(ref, subRef);
    }

    private RefInfo getRefInfo(JSONObject schema) {
        if (schema == null) {
            return null;
        }
        String ref;
        boolean isArray = DataType.ARRAY.equals(schema.getString("type"));
        if (isArray) {
            JSONObject items = schema.getJSONObject("items");
            ref = items.getString("$ref");
        } else {
            // #/definitions/Category
            ref = schema.getString("$ref");
        }
        if (schema.containsKey("additionalProperties")) {
            String additionalProperties = schema.getString("additionalProperties");
            if (JSONValidator.from(additionalProperties).validate() && additionalProperties.contains("$ref")) {
                ref = JSON.parseObject(additionalProperties).getString("$ref");
            }
        }
        if (ref == null) {
            return null;
        }
        RefInfo refInfo = new RefInfo();
        refInfo.setIsArray(isArray);
        refInfo.setRawRef(ref);
        refInfo.setRef(getRefName(ref));
        return refInfo;
    }

    private static String getRefName(String ref) {
        if (ref == null) {
            return null;
        }
        int index = ref.lastIndexOf("/");
        if (index > -1) {
            ref = ref.substring(index + 1);
        }
        return ref;
    }

    
    private String getRequestUrl(JSONObject docRoot) {
        /*
        "servers": [
            {
                "url": "http://localhost:8080",
                "description": "Inferred Url"
            }
        ]
         */
        return Optional.ofNullable(docRoot)
                .flatMap(root -> Optional.ofNullable(root.getJSONArray("servers")))
                .filter(servers -> servers.size() > 0)
                .map(servers -> servers.getJSONObject(0))
                .map(server -> server.getString("url"))
                .orElse("");
    }

    /**
     * <pre>
     * "requestBody": {
     *                     "content": {
     *                         "application/json": {
     *                             "schema": {
     *                                 "$ref": "#/components/schemas/用户基本信息"
     *                             }
     *                         }
     *                     }
     *                 }
     * </pre>
     * @param docInfo 文档内容
     * @return 返回consume
     */
    
    private List<String> getConsumes(JSONObject docInfo) {
        Set<String> consumes = Optional.ofNullable(docInfo.getJSONObject("requestBody"))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("content")))
                .map(JSONObject::keySet)
                .orElse(Collections.emptySet());
        return new ArrayList<>(consumes);
    }

    
    private List<String> getProduces(JSONObject docInfo) {
        Set<String> consumes = Optional.ofNullable(docInfo.getJSONObject("responses"))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("200")))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("content")))
                .map(JSONObject::keySet)
                .orElse(Collections.emptySet());
        return new ArrayList<>(consumes);
    }

    
    private JSONObject formatFieldJson(JSONObject docRoot, JSONObject fieldJson) {
        if (fieldJson.containsKey("$ref")) {
            RefInfo refInfo = getRefInfo(fieldJson);
            return getDefinitions(docRoot, refInfo).getJSONObject(refInfo.getRef());
        }
        return fieldJson;
    }

    // components.schemas.xx
    // components.parameters.xx
    
    private JSONObject getDefinitions(JSONObject docRoot, RefInfo refInfo) {
        String rawRef = refInfo.getRawRef();
        if (rawRef.contains("schemas")) {
            return docRoot.getJSONObject("components").getJSONObject("schemas");
        } else if (rawRef.contains("parameters")) {
            return docRoot.getJSONObject("components").getJSONObject("parameters");
        }
        return new JSONObject();
    }

    
    private RefInfo getResponseRefInfo(JSONObject docInfo) {
        return Optional.ofNullable(docInfo.getJSONObject("responses"))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("200")))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("content")))
                .flatMap(jsonObject -> {
                    String mediaType = jsonObject.keySet().iterator().next();
                    JSONObject schema = jsonObject
                            .getJSONObject(mediaType)
                            .getJSONObject("schema");
                    return Optional.ofNullable(schema);
                })
                .map(this::getRefInfo)
                .orElse(null);
    }

    /*
    "requestBody": {
                    "content": {
                        "application/json": {
                            "schema": {
                                "$ref": "#/components/schemas/用户基本信息"
                            }
                        }
                    }
                }
     */
    
    private JSONArray getParameterObject(JSONObject docInfo) {
        Optional<JSONArray> parametersOptional = Optional.ofNullable(docInfo.getJSONArray("parameters"));
        JSONArray jsonArray = parametersOptional.orElseGet(JSONArray::new);
        JSONObject requestBody = docInfo.getJSONObject("requestBody");
        Optional.ofNullable(requestBody)
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("content")))
                .flatMap(jsonObject -> {
                    String mediaType = jsonObject.keySet().iterator().next();
                    JSONObject schema = jsonObject
                            .getJSONObject(mediaType);
                    return Optional.ofNullable(schema);
                })
                .ifPresent(jsonArray::add);
        return jsonArray;
    }

    /*
        {
                            "name": "pageIndex",
                            "in": "query",
                            "description": "查看第几页",
                            "required": true,
                            "style": "form",
                            "schema": {
                                "type": "integer",
                                "format": "int32"
                            }
                        }
         */
    
    private String getFieldType(JSONObject fieldJson) {
        JSONObject schema = fieldJson.getJSONObject("schema");
        if (schema == null) {
            return fieldJson.getString("type");
        }
        return schema.getString("type");
    }
}
