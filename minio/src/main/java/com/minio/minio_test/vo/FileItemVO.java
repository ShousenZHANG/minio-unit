package com.minio.minio_test.vo;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the metadata of a file stored in MinIO.
 * Provides details such as name, size, type, and ownership.
 * Implements {@link Serializable} for object serialization.
 *
 * <p>Fields like {@code foreverUrl} can be used for creating permanent access links.</p>
 *
 * @author Zhang
 * @date 2022/11/29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class FileItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The name of the file or directory.
     */
    private String name;

    /**
     * The last modified timestamp of the file, formatted as a string.
     */
    private String lastModifyTime;

    /**
     * The name of the file's owner.
     */
    private String ownerName;

    /**
     * The size of the file in bytes. Default is 0 for directories.
     */
    private long size;

    /**
     * Indicates if the item is a directory.
     */
    private boolean isDir;

    /**
     * The encoding type or MIME type of the file.
     */
    private String encodingType;

    /**
     * The permanent URL for accessing the file.
     */
    private String foreverUrl;
}
