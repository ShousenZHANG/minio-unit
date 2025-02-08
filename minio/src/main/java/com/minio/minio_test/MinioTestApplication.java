package com.minio.minio_test;

import com.minio.minio_test.config.MinioClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * minio
 *
 * @author zhang
 * @date 2022/11/29
 */
@EnableConfigurationProperties({MinioClientProperties.class})
@EnableScheduling
@SpringBootApplication
public class MinioTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinioTestApplication.class, args);
    }

}

