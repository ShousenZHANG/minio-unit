package com.minio.minio_test.Response;

import lombok.*;
import org.springframework.http.HttpStatus;

/**
 * Unified API response structure.
 *
 * @author Zhang
 * @param <T> The type of response data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ResponseData<T> {

    /** HTTP status code */
    private int code;

    /** Response message */
    private String message;

    /** Response data */
    private T data;

    /**
     * Success response without data.
     */
    public static <T> ResponseData<T> success() {
        return new ResponseData<>(HttpStatus.OK.value(), "Operation successful", null);
    }

    /**
     * Success response with a custom message.
     */
    public static <T> ResponseData<T> success(String message) {
        return new ResponseData<>(HttpStatus.OK.value(), message, null);
    }

    /**
     * Success response with data.
     */
    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<>(HttpStatus.OK.value(), "Operation successful", data);
    }

    /**
     * Success response with custom message and data.
     */
    public static <T> ResponseData<T> success(String message, T data) {
        return new ResponseData<>(HttpStatus.OK.value(), message, data);
    }

    /**
     * Error response with default internal server error.
     */
    public static <T> ResponseData<T> error() {
        return new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Operation failed", null);
    }

    /**
     * Error response with custom message.
     */
    public static <T> ResponseData<T> error(String message) {
        return new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null);
    }

    /**
     * Error response with custom message and data.
     */
    public static <T> ResponseData<T> error(String message, T data) {
        return new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, data);
    }

    /**
     * Error response with custom status code and message.
     */
    public static <T> ResponseData<T> error(int code, String message) {
        return new ResponseData<>(code, message, null);
    }

    /**
     * Error response with custom status code, message, and data.
     */
    public static <T> ResponseData<T> error(int code, String message, T data) {
        return new ResponseData<>(code, message, data);
    }
}
