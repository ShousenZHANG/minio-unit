package com.minio.minio_test.service;

import com.minio.minio_test.vo.BucketVO;
import com.minio.minio_test.vo.FileItemVO;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * Minio Service Interface for managing file storage operations.
 * Provides methods for bucket management, file upload/download,
 * policy retrieval, and URL generation.
 *
 * @author Zhang
 * @date 2023/01/09
 */
public interface MinioService {

    /**
     * Checks if the specified bucket exists.
     *
     * @param bucketName The name of the bucket.
     * @return true if the bucket exists, false otherwise.
     */
    Boolean bucketExists(String bucketName);

    /**
     * Creates a new bucket.
     *
     * @param bucketName The name of the bucket to create.
     */
    void makeBucket(String bucketName);

    /**
     * Deletes a bucket.
     *
     * @param bucketName The name of the bucket to delete.
     */
    void removeBucket(String bucketName);

    /**
     * Uploads multiple files to the specified bucket.
     *
     * @param files      List of files to be uploaded.
     * @param bucketName The target bucket name.
     */
    void upload(List<MultipartFile> files, String bucketName);

    /**
     * Uploads a specific file to the bucket.
     *
     * @param bucketName The target bucket name.
     * @param objectName The object name inside the bucket.
     * @param filePath   The full path of the file to upload.
     */
    void uploadObject(String bucketName, String objectName, String filePath);

    /**
     * Downloads a file from a bucket and streams it to the response.
     *
     * @param bucketName The bucket containing the file.
     * @param fileName   The name of the file to download.
     * @param response   The HTTP response to write the file data.
     */
    void download(String bucketName, String fileName, HttpServletResponse response);

    /**
     * Downloads a file from the bucket and saves it to the local disk.
     *
     * @param bucketName   The bucket name.
     * @param objectName   The object name inside the bucket.
     * @param localFilePath The full path where the file should be saved locally.
     */
    void downloadToLocalDisk(String bucketName, String objectName, String localFilePath);

    /**
     * Lists all objects (files) in a specified bucket.
     *
     * @param bucketName The bucket name.
     * @return A list of file objects inside the bucket.
     */
    List<FileItemVO> listObjects(String bucketName);

    /**
     * Retrieves the bucket policy for the specified bucket.
     *
     * @param bucketName The bucket name.
     * @return The bucket policy as a JSON string.
     */
    String getBucketPolicy(String bucketName);

    /**
     * Lists all available bucket names.
     *
     * @return A list of bucket names.
     */
    List<BucketVO> listBucketNames();

    /**
     * Deletes a specific object (file) from a bucket.
     *
     * @param bucketName The bucket name.
     * @param objectName The object name to be deleted.
     */
    void removeObject(String bucketName, String objectName);

    /**
     * Deletes multiple objects (files) from a bucket.
     *
     * @param bucketName  The bucket name.
     * @param objectNames List of object names to be deleted.
     * @return true if deletion was successful, false otherwise.
     */
    Boolean removeObjects(String bucketName, List<String> objectNames);

    /**
     * Generates a pre-signed URL for downloading an object.
     *
     * @param bucketName The bucket name.
     * @param objectName The object name inside the bucket.
     * @param expiry     Expiration time in minutes (maximum 7 days).
     * @return A pre-signed URL for downloading the file.
     */
    String getObjectUrl(String bucketName, String objectName, Integer expiry);

    /**
     * Generates a pre-signed URL for uploading an object.
     *
     * @param bucketName The bucket name.
     * @param objectName The object name inside the bucket.
     * @param expiry     Expiration time in minutes.
     * @return A pre-signed URL for uploading the file.
     */
    String createUploadUrl(String bucketName, String objectName, Integer expiry);

    /**
     * Downloads a file via a direct URL and streams it to the response.
     *
     * @param request  The HTTP request containing file URL parameters.
     * @param response The HTTP response to send the file data.
     */
    void downloadUrl(HttpServletRequest request, HttpServletResponse response);
}
