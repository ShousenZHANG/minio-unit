package com.minio.minio_test;

import com.minio.minio_test.config.MinIoClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * minio启动类
 *
 * @author zhang
 * @date 2022/11/29
 */
@EnableConfigurationProperties({MinIoClientProperties.class})
@ComponentScan(basePackages = "com.minio.minio_test")
@EnableAutoConfiguration
@EnableScheduling
public class MinioTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinioTestApplication.class, args);
    }

}

