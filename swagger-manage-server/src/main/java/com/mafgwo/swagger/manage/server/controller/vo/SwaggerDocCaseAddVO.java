package com.mafgwo.swagger.manage.server.controller.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SwaggerDocCaseAddVO {

    @NotBlank(message = "用例名不能为空")
    private String name;

    @NotNull(message = "swaggerId不能为空")
    private Integer swaggerId;

    @NotBlank(message = "path不能为空")
    private String path;

    @NotBlank(message = "method不能为空")
    private String method;

    @NotBlank(message = "content不能为空")
    private String content;

}