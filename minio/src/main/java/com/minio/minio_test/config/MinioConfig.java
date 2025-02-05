package com.minio.minio_test.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio 配置类
 * 负责初始化 MinioClient 并提供全局可用的 Bean
 *
 * @author zhang
 * @date 2023/01/31
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioClientProperties.class)
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioClientProperties minioClientProperties;

    /**
     * 初始化 MinioClient
     *
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        try {
            log.info("正在初始化 MinioClient, 连接到 Minio 服务器: {}", minioClientProperties.getEndpoint());

            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioClientProperties.getEndpoint())
                    .credentials(minioClientProperties.getAccessKey(), minioClientProperties.getSecretKey())
                    .build();

            log.info("MinioClient 初始化成功！");
            return minioClient;
        } catch (Exception e) {
            log.error("MinioClient 初始化失败！请检查 Minio 配置。错误信息：{}", e.getMessage(), e);
            throw new RuntimeException("MinioClient 初始化失败", e);
        }
    }
}
