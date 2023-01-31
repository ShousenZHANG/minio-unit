package com.minio.minio_test.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio控制类
 *
 * @author zhang
 * @date 2023/01/31
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioConfig.class);

    private final MinioClientProperties minioClientProperties;

    @Bean
    public MinioClient minioClient() {
        LOGGER.info(
                "开始初始化MinioClient, url为{}, accessKey为:{}",
                minioClientProperties.getEndpoint(),
                minioClientProperties.getAccessKey());
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(minioClientProperties.getEndpoint())
                        .credentials(minioClientProperties.getAccessKey(), minioClientProperties.getSecretKey())
                        .build();
        LOGGER.info("MinioClient初始化成功!");
        return minioClient;
    }
}
