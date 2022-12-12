package com.minio.minio_test.exception;

/**
 * 文件传输异常类
 *
 * @author zhang
 * @date 2022/12/12
 */

public class FileTransferException extends RuntimeException {
    private final Integer errorCode;
    private final String errorMessage;

    public FileTransferException(Integer errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public FileTransferException(Integer errorCode, String errorMessage, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}