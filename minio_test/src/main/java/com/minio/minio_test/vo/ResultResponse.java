package com.minio.minio_test.vo;


import java.io.Serializable;

/**
 * 响应结果封装类
 *
 * @author zhang
 * @date 2022/12/09
 */
public class ResultResponse<T> implements Serializable {

    private static final long serialVersionUID = 6273326371984994386L;
    private Integer code;
    private String msg;
    private T data;

    private ResultResponse() {
        this.code = 200;
        this.msg = "OK";
    }

    private ResultResponse(T data) {
        this.code = 200;
        this.msg = "OK";
        this.setData(data);
    }

    private ResultResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private ResultResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultResponse<T> setError(Integer code, String msg) {
        this.setCode(code);
        this.setMsg(msg);
        return this;
    }

    public boolean isSuccess() {
        return this.getCode().equals(200);
    }

    public static ResultResponse ok() {
        return new ResultResponse();
    }

    public static <T> ResultResponse ok(T data) {
        return new ResultResponse(data);
    }

    public static <T> ResultResponse ok(Integer code, String msg) {
        return new ResultResponse(code, msg);
    }

    public static <T> ResultResponse ok(Integer code, String msg, T data) {
        return new ResultResponse(code, msg, data);
    }

    public static <T> ResultResponse error() {
        return new ResultResponse(500, "failed");
    }

    public static <T> ResultResponse error(String msg) {
        return new ResultResponse(500, msg);
    }

    public static <T> ResultResponse error(Integer code, String msg) {
        return new ResultResponse(code, msg);
    }

    public static <T> ResultResponse error(Integer code, String msg, T data) {
        return new ResultResponse(code, msg, data);
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
