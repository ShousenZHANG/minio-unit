package com.minio.minio_test.config;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * minio客户端配置类
 *
 * @author zhang
 * @date 2022/11/29
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioClientProperties {
    /**
     * Minio 服务器ip
     */
    @NotEmpty(message = "minio服务地址不可为空")
    @URL(message = "minio服务地址格式错误")
    private String endpoint;
    /**
     * 用户名
     */
    @NotEmpty(message = "minio认证账户不可为空")
    private String accessKey;
    /**
     * 密码
     */
    @NotEmpty(message = "minio认证密码不可为空")
    private String secretKey;

}
