package com.minio.minio_test.service.serviceImpl;

import com.minio.minio_test.exception.BusinessException;
import com.minio.minio_test.vo.BucketVO;
import com.minio.minio_test.vo.FileItemVO;
import com.minio.minio_test.service.MinioService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * minio service implementation.
 *
 * @author zhang
 * @date 2022/11/29
 */
@Component
public class MinioServiceImpl implements MinioService {

    // Static pattern for formatter
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN)
            .withZone(ZoneId.systemDefault());

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioServiceImpl.class);

    @Resource
    private MinioClient minioClient;

    @Override
    public Boolean bucketExists(String bucketName) {
        try {
            // Check if the bucket exists
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            // Log the error for debugging purposes
            LOGGER.error("Error while checking if bucket '{}' exists: {}", bucketName, e.getMessage(), e);
            // Throw a custom exception with detailed context
            throw new BusinessException("Failed to check if bucket exists: " + bucketName, e);
        }
    }

    @Override
    public void makeBucket(String bucketName) {
        try {
            // Check if the bucket already exists
            if (!bucketExists(bucketName)) {
                // Create the bucket if it does not exist
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                LOGGER.info("Bucket created successfully. Bucket name: {}", bucketName);

                // Define public read policy for the bucket
                String policy = "{\n" +
                        "  \"Version\": \"2012-10-17\",\n" +
                        "  \"Statement\": [\n" +
                        "    {\n" +
                        "      \"Sid\": \"PublicRead\",\n" +
                        "      \"Effect\": \"Allow\",\n" +
                        "      \"Principal\": \"*\",\n" +
                        "      \"Action\": [\n" +
                        "        \"s3:GetObject\"\n" +
                        "      ],\n" +
                        "      \"Resource\": [\n" +
                        "        \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

                // Apply bucket policy
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build());

                LOGGER.info("Public read policy applied successfully for bucket: {}", bucketName);
            } else {
                // Throw a custom exception if the bucket already exists
                throw new BusinessException("Bucket already exists: " + bucketName);
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            // Log the error with bucket name and full stack trace
            LOGGER.error("Failed to create bucket or apply policy. Bucket name: {}, Error: {}", bucketName, e.getMessage(), e);
            // Throw a custom exception with context
            throw new BusinessException("Failed to create bucket: " + bucketName, e);
        }
    }



    @Override
    public void removeBucket(String bucketName) {
        // Validate input
        if (StringUtils.isBlank(bucketName)) {
            throw new BusinessException("Bucket name cannot be empty.");
        }

        // Check if the bucket exists before attempting deletion
        if (!bucketExists(bucketName)) {
            LOGGER.warn("Attempted to remove a non-existing bucket: {}", bucketName);
            return; // Exit gracefully instead of throwing an exception
        }

        try {
            // Proceed with bucket deletion
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            LOGGER.info("Bucket removed successfully: {}", bucketName);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error("Failed to remove bucket '{}': {}", bucketName, e.getMessage(), e);
            throw new BusinessException("Failed to remove bucket: " + bucketName, e);
        }
    }



    @Override
    public void upload(List<MultipartFile> multipartFiles, String bucketName) {
        // Check if the bucket exists
        if (!bucketExists(bucketName)) {
            throw new BusinessException("Bucket does not exist: " + bucketName);
        }

        for (MultipartFile file : multipartFiles) {
            // Get the original file name
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                LOGGER.warn("Skipping file with empty name in bucket: {}", bucketName);
                continue; // Skip files with no names
            }

            try (InputStream in = file.getInputStream()) {
                // Upload the file to MinIO
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(in, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );

                // Log success message
                LOGGER.info("File uploaded successfully. File: {}, Size: {} bytes, Bucket: {}",
                        fileName, file.getSize(), bucketName);

            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                LOGGER.error("Failed to upload file: {} to bucket: {}. Error: {}", fileName, bucketName, e.getMessage(), e);
                throw new BusinessException("Failed to upload file: " + fileName + " to bucket: " + bucketName, e);
            }
        }
    }

    @Override
    public void uploadObject(String bucketName, String objectName, String fileName) {
        try {
            LOGGER.info("Starting upload. Bucket: {}, Object: {}, File: {}", bucketName, objectName, fileName);

            // Upload the object to MinIO
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(fileName)
                            .build()
            );

            LOGGER.info("Upload successful. Bucket: {}, Object: {}", bucketName, objectName);

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error("Upload failed. Bucket: {}, Object: {}, Error: {}", bucketName, objectName, e.getMessage(), e);
            throw new BusinessException("Failed to upload object: " + objectName + " to bucket: " + bucketName, e);
        }
    }


    /**
     * Download a file from MinIO.
     *
     * @param bucketName The bucket name.
     * @param fileName   The name of the file to be downloaded.
     * @param response   The HTTP response to write the file data.
     */
    @Override
    public void download(String bucketName, String fileName, HttpServletResponse response) {
        try {
            // Check if the file exists in the bucket
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(fileName).build());

            // Set response headers for file download
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setBufferSize(8192);

            // Fetch object from MinIO
            try (InputStream object = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build())) {

                // Write file data to the HTTP response
                IOUtils.copy(object, response.getOutputStream());
                response.flushBuffer(); // Ensure all data is sent
            }

            LOGGER.info("File download successful. File: {}, Bucket: {}", fileName, bucketName);

        } catch (Exception e) {
            LOGGER.error("File download failed. File: {}, Bucket: {}, Error: {}", fileName, bucketName, e.getMessage(), e);
            throw new BusinessException("Failed to download file: " + fileName, e);
        }
    }

    @Override
    public void downloadToLocalDisk(String bucketName, String objectName, String diskFileName) {
        try {
            // Check if the object exists in the bucket
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            // Download the object to local disk
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(diskFileName)
                    .build());

            // Log successful download
            LOGGER.info("Successfully downloaded object '{}' from bucket '{}' to local disk '{}'.",
                    objectName, bucketName, diskFileName);
        } catch (ErrorResponseException e) {
            // Handle MinIO specific errors (e.g., file not found, bucket not found)
            LOGGER.error("Failed to download object '{}' from bucket '{}'. Error: {}", objectName, bucketName, e.getMessage(), e);
            throw new BusinessException("Bucket or object not found: " + objectName, e);
        } catch (MinioException | IOException e) {
            // Handle MinIO and IO errors
            LOGGER.error("Error occurred while downloading object '{}' from bucket '{}'. Error: {}", objectName, bucketName, e.getMessage(), e);
            throw new BusinessException("Error downloading object: " + objectName, e);
        } catch (Exception e) {
            // Handle unexpected errors
            LOGGER.error("Unexpected error while downloading object '{}' from bucket '{}'. Error: {}", objectName, bucketName, e.getMessage(), e);
            throw new BusinessException("Unexpected error occurred while downloading object: " + objectName, e);
        }
    }


    /**
     * List all objects in the given MinIO bucket.
     *
     * @param bucketName The name of the bucket.
     * @return A list of {@link FileItemVO} objects containing file metadata.
     */
    @Override
    public List<FileItemVO> listObjects(String bucketName) {
        // Check if bucket exists before listing objects
        if (!bucketExists(bucketName)) {
            throw new BusinessException("Bucket does not exist: " + bucketName);
        }

        // List all objects in the bucket
        Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .build());

        // Convert Iterable to List using Java Streams
        return IterableUtils.toList(objects).stream()
                .map(result -> {
                    try {
                        Item item = result.get();
                        return FileItemVO.builder()
                                .name(item.objectName())
                                .ownerName(item.owner() == null ? "" : item.owner().displayName())
                                .size(item.size())
                                .isDir(item.isDir())
                                .encodingType("")
                                .lastModifyTime(item.lastModified() != null ? item.lastModified().format(FORMATTER) : null)
                                .build();
                    } catch (Exception e) {
                        LOGGER.error("Error processing object in bucket: {}. Error: {}", bucketName, e.getMessage(), e);
                        return null; // Skip invalid items
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the policy of a specified MinIO bucket.
     *
     * @param bucketName The name of the bucket whose policy is to be retrieved.
     * @return The bucket policy in JSON format as a string.
     */
    @Override
    public String getBucketPolicy(String bucketName) {
        String policy;
        try {
            // Retrieve the bucket policy from MinIO
            policy = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            // Log the error and throw a custom business exception
            LOGGER.error("Failed to retrieve bucket policy for '{}'. Error: {}", bucketName, e.getMessage(), e);
            throw new BusinessException("Error retrieving policy for bucket: " + bucketName, e);
        }
        return policy;
    }



    /**
     * Fetches the list of buckets from Minio.
     *
     * @return List of Minio buckets, or an empty list if an error occurs.
     */
    private List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            LOGGER.error("An error occurred while fetching the bucket list: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Converts a list of Minio buckets to a list of BucketVOs.
     * Uses the user's system time zone for date formatting.
     *
     * @return List of BucketVOs
     */
    @Override
    public List<BucketVO> listBucketNames() {
        List<Bucket> bucketList = listBuckets();

        if (bucketList == null || bucketList.isEmpty()) {
            return Collections.emptyList();
        }

        // Dynamic formatter with the system's default time zone
        ZoneId systemZone = ZoneId.systemDefault();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(systemZone);

        return bucketList.stream()
                .map(bucket -> BucketVO.builder()
                        .name(bucket.name())
                        .createTime(bucket.creationDate().format(formatter))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        try {
            // Check if the object exists in the bucket
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            // Remove the object from the bucket
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            // Log successful removal
            LOGGER.info("Successfully removed object '{}' from bucket '{}'.", objectName, bucketName);
        } catch (ErrorResponseException e) {
            // Handle specific MinIO error (e.g., object or bucket not found)
            LOGGER.error("Failed to remove object '{}'. Bucket '{}' or object might not exist. Error: {}", objectName, bucketName, e.getMessage(), e);
            throw new BusinessException("Bucket or object not found: " + objectName, e);
        } catch (MinioException | IOException e) {
            // Handle other MinIO-related exceptions
            LOGGER.error("Failed to remove object '{}' from bucket '{}'. Error: {}", objectName, bucketName, e.getMessage(), e);
            throw new BusinessException("Error occurred while removing object: " + objectName, e);
        } catch (Exception e) {
            // Handle unexpected exceptions
            LOGGER.error("Unexpected error while removing object '{}' from bucket '{}'. Error: {}", objectName, bucketName, e.getMessage(), e);
            throw new BusinessException("Unexpected error occurred while removing object: " + objectName, e);
        }
    }


    public Boolean removeObjects(String bucketName, List<String> objectNames) {
        if (!bucketExists(bucketName)) {
            return false;
        }
        List<DeleteObject> deleteObjects = new ArrayList<>(objectNames.size());
        for (String objectName : objectNames) {
            deleteObjects.add(new DeleteObject(objectName));
        }
        minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(deleteObjects)
                        .build());
        return true;
    }

    @Override
    public String getObjectUrl(String bucketName, String objectName, Integer expiry) {
        // Handle expiry with default or validated value
        expiry = expiryHandle(expiry);

        try {
            // Check if the object exists in the bucket
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            // Generate a pre-signed URL for the object
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry)
                    .build());

            LOGGER.info("Successfully generated file download URL. Bucket: {}, Object: {}, Expiry: {} seconds",
                    bucketName, objectName, expiry);
            return url;

        } catch (Exception e) {
            // Log the error with full details
            LOGGER.error("Failed to generate file download URL. Bucket: {}, Object: {}, Error: {}",
                    bucketName, objectName, e.getMessage(), e);

            // Wrap and rethrow the exception with a custom error message
            throw new BusinessException("Failed to generate download URL for object: " + objectName, e);
        }
    }


    @Override
    public String createUploadUrl(String bucketName, String objectName, Integer expiry) {
        expiry = expiryHandle(expiry);

        try {
            // Log the request details
            LOGGER.info("Generating upload URL. Bucket: {}, Object: {}, Expiry: {} seconds", bucketName, objectName, expiry);

            // Check if the bucket exists
            if (!bucketExists(bucketName)) {
                throw new BusinessException("Bucket does not exist: " + bucketName);
            }

            // Generate a pre-signed URL for object upload
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );

            LOGGER.info("Upload URL generated successfully. Bucket: {}, Object: {}", bucketName, objectName);
            return url;

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error("Failed to generate upload URL. Bucket: {}, Object: {}, Error: {}", bucketName, objectName, e.getMessage(), e);
            throw new BusinessException("Failed to generate upload URL for object: " + objectName + " in bucket: " + bucketName, e);
        }
    }


    /**
     * Downloads a file from a given URL and streams it to the HTTP response.
     *
     * @param request  The HTTP request containing file parameters.
     * @param response The HTTP response to send the file to the client.
     */
    @Override
    public void downloadUrl(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("Starting file download...");

        // Get file URL and name from request parameters
        String fileUrl = request.getParameter("filenamerel");
        String fileName = request.getParameter("filename");

        // Validate file URL
        if (StringUtils.isBlank(fileUrl)) {
            throw new BusinessException("File URL cannot be empty.");
        }

        // Derive file name if not provided
        if (StringUtils.isBlank(fileName)) {
            fileName = getFileNameFromUrl(fileUrl);
        }

        // Stream the file from the URL to the HTTP response
        try (InputStream is = new URL(fileUrl).openStream();
             OutputStream os = response.getOutputStream()) {

            // Handle file name encoding for different browsers
            String userAgent = request.getHeader("user-agent").toLowerCase();
            if (userAgent.contains("msie") || userAgent.contains("like gecko")) {
                fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            } else {
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }

            // Set response headers for file download
            response.setContentType("application/octet-stream; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // Buffer for reading and writing data
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            LOGGER.info("File download completed successfully: {}", fileUrl);

        } catch (MalformedURLException e) {
            LOGGER.error("Invalid file URL: {}. Error: {}", fileUrl, e.getMessage(), e);
            throw new BusinessException("Invalid file URL: " + fileUrl, e);
        } catch (IOException e) {
            LOGGER.error("Failed to download file: {}. Error: {}", fileUrl, e.getMessage(), e);
            throw new BusinessException("Error occurred while downloading file: " + fileName, e);
        }
    }



    /**
     * Extracts the file name from an HTTP URL.
     *
     * @param fileUrl The HTTP URL from which to extract the file name.
     * @return The extracted file name, or an empty string if the URL is invalid or empty.
     */
    private String getFileNameFromUrl(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return "";
        }

        // Find the last slash (/) in the URL
        int lastIdx = fileUrl.lastIndexOf('/');

        // Extract the substring after the last slash, if it exists
        return (lastIdx >= 0 && lastIdx < fileUrl.length() - 1)
                ? fileUrl.substring(lastIdx + 1)
                : "";
    }


    /**
     * Handle expiry time for pre-signed URLs.
     *
     * @param expiry The expiry time in minutes.
     * @return int
     */
    private static int expiryHandle(Integer expiry) {
        expiry = expiry * 60;
        if (expiry > 604800) {
            return 604800;
        }
        return expiry;
    }

}

