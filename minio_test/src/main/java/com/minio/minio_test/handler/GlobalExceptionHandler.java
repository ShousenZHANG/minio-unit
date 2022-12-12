package com.minio.minio_test.handler;

import com.minio.minio_test.exception.FileTransferException;
import com.minio.minio_test.vo.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.SimpleDateFormat;

/**
 * 全局异常处理类
 *
 * @author zhang
 * @date 2022/12/12
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FileTransferException.class)
    public ResultResponse handleFileTransferException(FileTransferException e) {
        LOGGER.error(e.getErrorMessage(), e);
        return ResultResponse.error(ResultResponse.error().getCode(), e.getErrorMessage(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
    }
}
