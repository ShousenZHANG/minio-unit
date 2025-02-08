package com.minio.minio_test.exception;

import com.minio.minio_test.Response.ResponseData;
import lombok.Getter;

/**
 * Custom exception class for business logic errors.
 * This exception is thrown when an expected business-related error occurs.
 *
 * Extends {@link RuntimeException} to allow unchecked exceptions.
 * Includes an error code and message for structured error handling.
 *
 * @author Zhang
 * @date 2022/12/12
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer errorCode;
    private final String errorMessage;

    /**
     * Constructs a new BusinessException with a message only.
     * Uses a default error code from {@link ResponseData}.
     *
     * @param errorMessage The error message describing the issue.
     */
    public BusinessException(String errorMessage) {
        this(ResponseData.error().getCode(), errorMessage);
    }

    /**
     * Constructs a new BusinessException with an error code and message.
     *
     * @param errorCode    The specific error code.
     * @param errorMessage The error message describing the issue.
     */
    public BusinessException(Integer errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Constructs a new BusinessException with an error code, message, and a cause.
     *
     * @param errorCode    The specific error code.
     * @param errorMessage The error message describing the issue.
     * @param cause        The underlying throwable cause.
     */
    public BusinessException(Integer errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Constructs a new BusinessException with a message and a cause.
     * Uses a default error code from {@link ResponseData}.
     *
     * @param errorMessage The error message describing the issue.
     * @param cause        The underlying throwable cause.
     */
    public BusinessException(String errorMessage, Throwable cause) {
        this(ResponseData.error().getCode(), errorMessage, cause);
    }
}
