package com.minio.minio_test.controller;

import com.minio.minio_test.Response.ResponseData;
import com.minio.minio_test.vo.ObjectItem;
import com.minio.minio_test.service.MinioService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * minio 控制层
 *
 * @author zhang
 * @date 2022/11/29
 */
@RestController
public class MinioController {

    @Resource
    private MinioService minioService;

    /**
     * 上传文件信息
     *
     * @param files      多文件
     * @param bucketName 存储桶名称
     * @return {@link ResponseData}
     */
    @ResponseBody
    @PostMapping("/upload")
    public ResponseData upload(List<MultipartFile> files, String bucketName) {
        String message = minioService.upload(files, bucketName);
        return ResponseData.success(message);
    }

    /**
     * 删除存储桶中文件信息
     *
     * @param bucketName 文件桶名称
     * @param objectName 文件名称
     * @return {@link ResponseData}
     */
    @DeleteMapping
    public ResponseData delete(String bucketName, String objectName) {
        String message = minioService.removeObject(bucketName, objectName);
        return ResponseData.success(message);
    }

    /**
     * 创建桶
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    @PostMapping("/makeBucket")
    public ResponseData makeBucket(String bucketName) {
        String message = minioService.makeBucket(bucketName);
        return ResponseData.success(message);
    }

    /**
     * 通过minio的api进行下载文件
     * TODO 本地单机测试通过，服务器下载到本地磁盘出现错误
     *
     * @param objectName 目标文件
     * @param fileName   下载
     * @return {@link String}
     */
    @PostMapping("/download")
    public ResponseData download(@RequestParam("bucketName") String bucketName, @RequestParam("object") String objectName, @RequestParam("file") String fileName) {
        String message = minioService.downloadToLocalDisk(bucketName, objectName, fileName);
        return ResponseData.success(message);

    }


    /**
     * 通过流的方式进行文件下载
     *
     * @param bucketName 文件桶名称
     * @param fileName   下载的文件名称
     * @param res        响应信息
     * @return {@link ResponseData}
     */
    @ResponseBody
    @PostMapping("/downloadFile")
    public void downloadFile(@RequestParam("bucketName") String bucketName, @RequestParam("file") String fileName, HttpServletResponse res) {
        minioService.download(bucketName, fileName, res);
    }


    /**
     * 查询所有文件信息
     *
     * @param bucketName 桶名称
     * @return {@link List}<{@link ObjectItem}>
     */
    @GetMapping("/listObjects")
    public ResponseData listObjects(String bucketName) {
        List<ObjectItem> objectItems = minioService.listObjects(bucketName);
        return ResponseData.success(objectItems);
    }

    /**
     * 查询所有桶名称
     *
     * @return {@link ResponseData}
     */
    @ResponseBody
    @GetMapping("/listBuckets")
    public ResponseData listBuckets() {
        return ResponseData.success(minioService.listBucketNames());
    }


    /**
     * 删除桶
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    @DeleteMapping("/deleteBucket")
    public ResponseData deleteBucket(String bucketName) {
        String message = minioService.removeBucket(bucketName);
        return ResponseData.success(message);
    }

    /**
     * 查询桶的策略信息
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    @GetMapping("/getBucketPolicy")
    public String getBucketPolicy(String bucketName) {
        return minioService.getBucketPolicy(bucketName);
    }

    /**
     * 创建文件外链链接
     *
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @param expires    过期时间
     * @return {@link ResponseData}
     */
    @PostMapping("/uploadUrl")
    public ResponseData createUploadUrl(String bucketName, String objectName, Integer expires) {
        String uploadUrl = minioService.createUploadUrl(bucketName, objectName, expires);
        return ResponseData.success("上传文件外链链接", uploadUrl);
    }


    /**
     * 创建下载文件外链链接
     *
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @param expires    过期时间
     * @return {@link ResponseData}
     */
    @PostMapping("/getObjectUrl")
    public ResponseData getObjectUrl(String bucketName, String objectName, Integer expires) {
        String objectUrl = minioService.getObjectUrl(bucketName, objectName, expires);
        return ResponseData.success("下载文件外链链接", objectUrl);
    }

    /**
     * 定期清除存储桶中的文件内容
     */
    @Scheduled(cron = "0 0/1 14 * * ? ")
    private void clearIntervals() {
        //当前时间前一天的时间
        ZonedDateTime rangeTime = ZonedDateTime.now().minusDays(1);
        //创建定期清除策略的文件存储桶
        minioService.makeBucket("miniodemo");
        //删除一天之前的文件内容
        List<ObjectItem> clearIntervalsBucketList = minioService.listObjects("miniodemo");
        for (ObjectItem objectItem : clearIntervalsBucketList) {
            ZonedDateTime lastModified = objectItem.getLastModified();
            boolean flag = lastModified.isBefore(rangeTime);
            if (flag) {
                minioService.removeObject("miniodemo", objectItem.getObjectName());
            }
        }
    }
}

