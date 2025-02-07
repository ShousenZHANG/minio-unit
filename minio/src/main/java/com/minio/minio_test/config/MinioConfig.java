package com.minio.minio_test.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio Configuration Class
 * Responsible for initializing the MinioClient and providing it as a Spring Bean.
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
     * Initializes the MinioClient with the configured endpoint, access key, and secret key.
     *
     * @return MinioClient instance
     */
    @Bean
    public MinioClient minioClient() {
        try {
            log.info("Initializing MinioClient, connecting to Minio server at: {}", minioClientProperties.getEndpoint());

            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioClientProperties.getEndpoint())
                    .credentials(minioClientProperties.getAccessKey(), minioClientProperties.getSecretKey())
                    .build();

            log.info("MinioClient successfully initialized.");
            return minioClient;
        } catch (Exception e) {
            log.error("Failed to initialize MinioClient. Please check the Minio configuration. Error: {}", e.getMessage(), e);
            throw new RuntimeException("MinioClient initialization failed. Ensure the Minio endpoint, access key, and secret key are correctly configured.", e);
        }
    }
}
