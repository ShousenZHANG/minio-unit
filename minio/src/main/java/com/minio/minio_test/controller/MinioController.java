package com.minio.minio_test.controller;

import com.minio.minio_test.Response.ResponseData;
import com.minio.minio_test.exception.BusinessException;
import com.minio.minio_test.vo.BucketVO;
import com.minio.minio_test.vo.FileItemVO;
import com.minio.minio_test.service.MinioService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * minio controller.
 *
 * @author zhang
 * @date 2022/11/29
 */
@RestController
public class MinioController {

    @Resource
    private MinioService minioService;

    /**
     * Upload multiple files to MinIO.
     *
     * @param files      List of files to be uploaded.
     * @param bucketName The target bucket name.
     * @return {@link ResponseData}<{@link List}<{@link String}>> List of uploaded file names.
     */
    @ResponseBody
    @PostMapping("/upload")
    public ResponseData<List<String>> upload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("bucketName") String bucketName) {

        // Validate input
        if (files == null || files.isEmpty()) {
            throw new BusinessException("No files provided for upload.");
        }

        // Call service to upload files
        minioService.upload(files, bucketName);

        // Collect uploaded file names
        List<String> uploadedFileNames = files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());

        // Return response with uploaded file names
        return ResponseData.success("Files uploaded successfully", uploadedFileNames);
    }

    /**
     * Deletes a file from the specified bucket.
     *
     * @param bucketName The name of the bucket
     * @param objectName The name of the file to delete
     * @return {@link ResponseData} containing the result message
     */
    @ResponseBody
    @DeleteMapping("/deleteObject")
    public ResponseData<String> deleteObject(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("objectName") String objectName) {
        // Call the service method to remove the object
        minioService.removeObject(bucketName, objectName);
        // Return a success response
        return ResponseData.success("File deleted successfully from bucket: " + bucketName);
    }


    /**
     * Create a new MinIO bucket.
     *
     * @param bucketName The name of the bucket to be created.
     * @return {@link ResponseData}<{@link String}> Success message wrapped in a response object.
     */
    @ResponseBody
    @PostMapping("/makeBucket")
    public ResponseData<String> makeBucket(@RequestParam("bucketName") String bucketName) {
        // Call service to create bucket
        minioService.makeBucket(bucketName);
        // Return response
        return ResponseData.success("Bucket created successfully: " + bucketName);
    }


    /**
     * Downloads a file from MinIO to the local disk.
     *
     * @param bucketName The name of the bucket
     * @param objectName The name of the object to download
     * @param fileName The local file path where the object will be saved
     * @return {@link ResponseData} containing the result message
     */
    @ResponseBody
    @PostMapping("/downloadToLocal")
    public ResponseData<String> downloadToLocal(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("objectName") String objectName,
            @RequestParam("localFile") String fileName) {

        // Call the service method to download the object
        minioService.downloadToLocalDisk(bucketName, objectName, fileName);

        // Return success response
        return ResponseData.success("File downloaded successfully to: " + fileName);
    }



    /**
     * Download a file using a stream.
     *
     * @param bucketName The bucket name where the file is stored.
     * @param fileName   The name of the file to be downloaded.
     * @param response   The HTTP response to write the file data.
     */
    @ResponseBody
    @PostMapping("/downloadFile")
    public void downloadFile(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("fileName") String fileName,
            HttpServletResponse response) {
        // Call the service method to handle the download process
        minioService.download(bucketName, fileName, response);
    }



    /**
     * Query all file information in a bucket.
     *
     * @param bucketName The name of the bucket
     * @return {@link ResponseData} containing a list of {@link FileItemVO}
     */
    @ResponseBody
    @GetMapping("/listObjects")
    public ResponseData<List<FileItemVO>> listObjects(@RequestParam("bucketName") String bucketName) {
        // Fetch the list of file items from the bucket
        List<FileItemVO> fileItems = minioService.listObjects(bucketName);
        return ResponseData.success(fileItems);
    }




    /**
     * Retrieve all bucket names.
     *
     * @return {@link ResponseData}<{@link List}<{@link String}>> A list of bucket names wrapped in a response object.
     */
    @ResponseBody
    @GetMapping("/listBuckets")
    public ResponseData<List<BucketVO>> listBuckets() {
        List<BucketVO> bucketNames = minioService.listBucketNames();
        return ResponseData.success(bucketNames);
    }


    /**
     * Delete a bucket.
     *
     * @param bucketName The name of the bucket to delete.
     * @return {@link ResponseData}<{@link String}> Success message wrapped in a response object.
     */
    @ResponseBody
    @DeleteMapping("/deleteBucket")
    public ResponseData<String> deleteBucket(@RequestParam("bucketName") String bucketName) {
        // Call the service to remove the bucket
        minioService.removeBucket(bucketName);

        // Return a success response
        return ResponseData.success("Bucket deleted successfully: " + bucketName);
    }


    /**
     * Retrieves the access policy of a specified MinIO bucket.
     *
     * @param bucketName The name of the bucket.
     * @return {@link ResponseData} containing the bucket policy in JSON format.
     */
    @ResponseBody
    @GetMapping("/getBucketPolicy")
    public ResponseData<String> getBucketPolicy(@RequestParam("bucketName") String bucketName) {
        String policy = minioService.getBucketPolicy(bucketName);
        return ResponseData.success(policy);
    }


    /**
     * Generate a download URL for a file.
     *
     * @param bucketName The name of the bucket where the file is stored.
     * @param objectName The name of the file for which the URL is being generated.
     * @param expires    The expiration time of the URL in seconds.
     * @return {@link ResponseData}<{@link String}> The generated URL wrapped in a response object.
     */
    @ResponseBody
    @PostMapping("/getObjectUrl")
    public ResponseData<String> getObjectUrl(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expires", required = false, defaultValue = "3600") Integer expires) {

        // Call the service method to generate the URL
        String objectUrl = minioService.getObjectUrl(bucketName, objectName, expires);

        // Return a success response with the generated URL
        return ResponseData.success("File download URL created successfully", objectUrl);
    }


    /**
     * Generate an upload URL for a file.
     *
     * @param bucketName The name of the bucket where the file will be uploaded.
     * @param objectName The name of the file to be uploaded.
     * @param expires    The expiration time of the URL in seconds.
     * @return {@link ResponseData}<{@link String}> The generated URL wrapped in a response object.
     */
    @ResponseBody
    @PostMapping("/getUploadUrl")
    public ResponseData<String> createUploadUrl(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expires", required = false, defaultValue = "3600") Integer expires) {

        // Call the service method to generate the upload URL
        String uploadUrl = minioService.createUploadUrl(bucketName, objectName, expires);

        // Return a success response with the generated upload URL
        return ResponseData.success("File upload URL created successfully", uploadUrl);
    }


    /**
     * Downloads a file using the file URL provided in the HTTP request.
     *
     * @param request  The HTTP request containing the file URL parameters.
     * @param response The HTTP response used to stream the file back to the client.
     */
    @ResponseBody
    @GetMapping("/fileUrl/download")
    public void downloadUrl(HttpServletRequest request, HttpServletResponse response) {
        String fileUrl = request.getParameter("fileUrl");
        if (StringUtils.isBlank(fileUrl)) {
            throw new BusinessException("File URL cannot be empty.");
        }

        // Delegate directly to the service layer without explicit exception handling
        minioService.downloadUrl(request, response);
    }



//    /**
//     * Scheduled task to periodically clear files in the specified bucket.
//     * Removes files older than one day from the "miniodemo" bucket.
//     */
//    @Scheduled(cron = "0 0/1 14 * * ?")
//    private void clearIntervals() {
//        // Calculate the time range for files to be deleted (files older than one day)
//        ZonedDateTime cutoffTime = ZonedDateTime.now().minusDays(1);
//
//        // Ensure the bucket exists; create it if necessary
//        String bucketName = "miniodemo";
//        minioService.makeBucket(bucketName);
//
//        // Retrieve all objects in the bucket
//        List<FileItemVO> fileList = minioService.listObjects(bucketName);
//
//        // Filter and delete files older than the cutoff time
//        fileList.stream()
//                .filter(file -> isOlderThanCutoff(file.getLastModifyTime(), cutoffTime))
//                .forEach(file -> {
//                    minioService.removeObject(bucketName, file.getName());
//                });
//    }
//
//    /**
//     * Checks if a file's last modified time is before the given cutoff time.
//     *
//     * @param lastModifiedTime The last modified time of the file as a string.
//     * @param cutoffTime       The cutoff time for deletion.
//     * @return true if the file is older than the cutoff time, false otherwise.
//     */
//    private boolean isOlderThanCutoff(String lastModifiedTime, ZonedDateTime cutoffTime) {
//        if (StringUtils.isBlank(lastModifiedTime)) {
//            return false;
//        }
//
//        try {
//            ZonedDateTime fileTime = ZonedDateTime.parse(lastModifiedTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai")));
//            return fileTime.isBefore(cutoffTime);
//        } catch (Exception e) {
//            return false;
//        }
//    }

}

