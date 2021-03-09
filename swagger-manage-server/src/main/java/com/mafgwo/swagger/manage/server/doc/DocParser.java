package com.mafgwo.swagger.manage.server.doc;

/**
 * @author tanghc
 */
public interface DocParser {
    /**
     * 解析swagger文档，将json转换成对象模式
     * @param swaggerJson swagger文档内容
     * @return 返回对象
     */
    DocInfo parseJson(String swaggerJson);
}
