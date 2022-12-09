package com.minio.minio_test.pojo;

import lombok.Data;

/**
 * 文件实体类
 *
 * @author zhang
 * @date 2022/11/29
 */
@Data
public class ObjectItem {
    private String objectName;
    private Long size;
}
