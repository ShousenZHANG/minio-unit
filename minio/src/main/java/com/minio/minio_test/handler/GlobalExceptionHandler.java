package com.minio.minio_test.handler;

import com.minio.minio_test.Response.ResponseData;
import com.minio.minio_test.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global Exception Handler for the application.
 * Handles exceptions and returns unified error responses.
 *
 * @author zhang
 * @date 2022/12/12
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Handles custom BusinessException.
     *
     * @param e the BusinessException thrown by the application.
     * @return a standardized error response.
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseData<Object> handleBusinessException(BusinessException e) {
        // Log the exception with detailed message and stack trace
        LOGGER.error("BusinessException occurred: {}", e.getErrorMessage(), e);

        // Create an error response with the current timestamp
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        return ResponseData.error(
                ResponseData.error().getCode(),
                e.getErrorMessage(),
                timestamp
        );
    }
}
