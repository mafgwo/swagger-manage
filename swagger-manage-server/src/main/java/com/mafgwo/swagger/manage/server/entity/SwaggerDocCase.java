package com.mafgwo.swagger.manage.server.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author chenxiaoqi
 * @since 2021/07/14
 */
@Data
public class SwaggerDocCase {
    private Integer id;

    private String name;

    private Integer swaggerId;

    private String path;

    private String method;

    private String content;

    private Date createdAt;
}
