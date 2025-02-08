package com.minio.minio_test.config;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Minio Client Configuration Properties.
 * This class maps Minio-related properties from the application configuration.
 *
 * @author Zhang
 * @date 2022/11/29
 */
@Data
@Validated
@ConfigurationProperties(prefix = "minio")
public class MinioClientProperties {

    /**
     * Minio server endpoint URL.
     */
    @NotBlank(message = "Minio server endpoint cannot be empty.")
    @URL(message = "Invalid Minio server URL format.")
    private String endpoint;

    /**
     * Minio authentication access key.
     */
    @NotBlank(message = "Minio authentication access key cannot be empty.")
    private String accessKey;

    /**
     * Minio authentication secret key.
     */
    @NotBlank(message = "Minio authentication secret key cannot be empty.")
    private String secretKey;
}
