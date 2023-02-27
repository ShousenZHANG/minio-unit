package com.minio.minio_test.vo;

import lombok.*;

import java.io.Serializable;

/**
 * Minio存储桶
 * 
 * @author wenjianhai
 * @date 2022/1/18
 * @since JDK 1.8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class BucketVO implements Serializable {

	private static final long serialVersionUID = -7078176219040233386L;

	/** 桶名 */
	private String name;
	/** 创建时间 */
	private String createTime;
}
