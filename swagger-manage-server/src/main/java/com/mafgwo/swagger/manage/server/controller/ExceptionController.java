package com.mafgwo.swagger.manage.server.controller;

import com.mafgwo.swagger.manage.server.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局错误处理
 *
 * @author tanghc
 */
@RestControllerAdvice
public class ExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(Exception.class)
    public Object exceptionHandler(Exception e) {
        logger.error("报错：", e);
        return Result.err(e.getMessage());
    }

}
