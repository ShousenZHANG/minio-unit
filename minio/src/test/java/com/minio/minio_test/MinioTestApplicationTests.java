package com.minio.minio_test;

import com.minio.minio_test.service.MinioService;
import com.minio.minio_test.vo.BucketVO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Test class for MinioService.
 * Validates if Minio bucket existence checks work as expected.
 */
@SpringBootTest
class MinioTestApplicationTests {

    @Resource
    private MinioService minioService;

    /**
     * Test case to verify if a specific bucket exists in Minio storage.
     */
    @Test
    void shouldCheckIfBucketExists() {
        // Given: A predefined bucket name
        String bucketName = "test";

        // When: Checking if the bucket exists
        boolean exists = minioService.bucketExists(bucketName);

        // Log output for reference (Optional)
        System.out.println("Bucket '" + bucketName + "' existence: " + exists);
    }

    @Test
    void makeBucket() {
        // Given: A predefined bucket name
        String bucketName = "test2";

        // When: Creating a new bucket
        minioService.makeBucket(bucketName);

        // Log output for reference (Optional)
        System.out.println("Bucket '" + bucketName + "' created successfully");
    }

    @Test
    void checkBucketList() {
        // When: Listing all buckets
        List<BucketVO> bucketVOList =  minioService.listBucketNames();

        // Log output for reference (Optional)
        bucketVOList.forEach(bucket -> System.out.printf("Bucket: %s, Created At: %s%n", bucket.getName(), bucket.getCreateTime()));
    }


}
