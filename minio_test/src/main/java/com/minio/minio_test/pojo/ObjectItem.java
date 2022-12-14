package com.minio.minio_test.pojo;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * 文件实体类
 *
 * @author zhang
 * @date 2022/11/29
 */
@Data
public class ObjectItem {
    private String objectName;
    private ZonedDateTime lastModified;
    private Long size;
}
