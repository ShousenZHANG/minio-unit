package com.minio.minio_test.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * minio客户端配置类
 *
 * @author zhang
 * @date 2022/11/29
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinIoClientProperties {
    /**
     * Minio 服务器ip
     */
    private String endpoint;
    /**
     * 用户名
     */
    private String accessKey;
    /**
     * 密码
     */
    private String secretKey;

    /**
     * 注入Minio客户端
     *
     * @return {@link MinioClient}
     */
    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

}
