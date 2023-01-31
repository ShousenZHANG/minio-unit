package com.minio.minio_test.exception;

import com.minio.minio_test.Response.ResponseData;
import lombok.Getter;

/**
 * 文件传输异常类
 *
 * @author zhang
 * @date 2022/12/12
 */

@Getter
public class BusinessException extends RuntimeException {
    private final Integer errorCode;
    private final String errorMessage;


    public BusinessException(String errorMessage) {
        this.errorCode = ResponseData.error().getCode();
        this.errorMessage = errorMessage;
    }

    public BusinessException(Integer errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BusinessException(Integer errorCode, String errorMessage, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BusinessException(String errorMessage,Throwable throwable) {
        super(throwable);
        this.errorCode = ResponseData.error().getCode();
        this.errorMessage = errorMessage;
    }
}