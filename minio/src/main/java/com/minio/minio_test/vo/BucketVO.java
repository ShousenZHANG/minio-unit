package com.minio.minio_test.vo;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a Minio storage bucket.
 * This class holds metadata related to a bucket such as its name and creation time.
 * <p>
 * Implements {@link Serializable} for object serialization.
 * </p>
 *
 * @author wenjianhai
 * @date 2022/01/18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class BucketVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/** The name of the bucket */
	private String name;

	/** The creation timestamp of the bucket */
	private String createTime;
}
