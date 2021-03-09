package com.mafgwo.swagger.manage.server.common;

public class Result {

    private static final String CODE_SUCCESS = "0";
    private static final String CODE_ERROR = "100";

    private static Result ok = new Result();
    static {
        ok.setCode(CODE_SUCCESS);
    }

    private String code;
    private Object data;
    private String msg;

    public static Result ok() {
        return ok;
    }

    public static Result ok(Object data) {
        Result result = new Result();
        result.setCode(CODE_SUCCESS);
        result.setData(data);
        return result;
    }

    public static Result err(String msg) {
        Result result = new Result();
        result.setCode(CODE_ERROR);
        result.setMsg(msg);
        return result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}