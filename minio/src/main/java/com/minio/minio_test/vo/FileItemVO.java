package com.minio.minio_test.vo;

import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * 文件信息
 *
 * @author zhang
 * @date 2022/11/29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class FileItemVO implements Serializable {
    /** 文件名 */
    private String name;
    /** 最后更新时间 */
    private String lastModifyTime;
    /** 作者 */
    private String ownerName;
    /** 文件大小（字节） */
    private long size;
    /** 是否目录 */
    private boolean isDir;
    /** 文件类型 */
    private String encodingType;
    /** 文件永久链接 */
    private String foreverUrl;
}
