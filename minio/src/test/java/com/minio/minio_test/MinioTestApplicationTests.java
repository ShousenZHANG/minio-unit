package com.minio.minio_test;

import com.minio.minio_test.service.MinioService;
import com.minio.minio_test.vo.BucketVO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
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
        String bucketName = "test3";

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

    @Test
    void removeBucket() {
        // Given: A predefined bucket name
        String bucketName = "test3";

        // When: Removing the bucket
        minioService.removeBucket(bucketName);

        // Log output for reference (Optional)
        System.out.println("Bucket '" + bucketName + "' removed successfully");
    }

    @Test
    void upload() {
        // Given: A predefined bucket name
        String bucketName = "test";

        MultipartFile mockFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello, Minio!".getBytes()
        );

        List<MultipartFile> files = Collections.singletonList(mockFile);
        // When: Uploading a file to the bucket
        minioService.upload(files, bucketName);

        // Log output for reference (Optional)
        System.out.println("File uploaded successfully");
    }

    @Test
    void uploadObject() {
        // Given: A predefined bucket name
        String bucketName = "test";
        String objectName = "zzzz.txt";
        String fileName = "E:\\minio_unit\\minio\\logs\\application.log";

        // When: Uploading a file to the bucket
        minioService.uploadObject(bucketName, objectName, fileName);

        // Log output for reference (Optional)
        System.out.println("File uploaded successfully");
    }

}
