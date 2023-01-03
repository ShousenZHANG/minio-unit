package com.minio.minio_test.controller;

import com.minio.minio_test.pojo.ObjectItem;
import com.minio.minio_test.utils.MinioUtil;
import com.minio.minio_test.vo.ResultResponse;
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
    private MinioUtil minioUtil;


    /**
     * 上传文件信息
     *
     * @param files      多文件
     * @param bucketName 存储桶名称
     * @return {@link ResultResponse}
     */
    @PostMapping("/upload")
    public ResultResponse upload(List<MultipartFile> files, String bucketName) {
        String message = minioUtil.upload(files, bucketName, null);
        return ResultResponse.ok(ResultResponse.ok().getCode(), message);
    }

    /**
     * 删除存储桶中文件信息
     *
     * @param bucketName 文件桶名称
     * @param objectName 文件名称
     * @return {@link ResultResponse}
     */
    @DeleteMapping
    public ResultResponse delete(String bucketName, String objectName) {
        String message = minioUtil.removeObject(bucketName, objectName);
        return ResultResponse.ok(ResultResponse.ok().getCode(), message);
    }

    /**
     * 创建桶
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    @PostMapping("/makeBucket")
    public ResultResponse makeBucket(String bucketName) {
        String message = minioUtil.makeBucket(bucketName);
        return ResultResponse.ok(ResultResponse.ok().getCode(), message);
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
    public ResultResponse download(@RequestParam("bucketName") String bucketName, @RequestParam("object") String objectName, @RequestParam("file") String fileName) {
        String message = minioUtil.downloadToLocalDisk(bucketName, objectName, fileName);
        return ResultResponse.ok(ResultResponse.ok().getCode(), message);

    }


    /**
     * 通过流的方式进行文件下载
     *
     * @param bucketName 文件桶名称
     * @param fileName   下载的文件名称
     * @param res        响应信息
     * @return {@link ResultResponse}
     */
    @PostMapping("/downloadFile")
    public ResultResponse downloadFile(@RequestParam("bucketName") String bucketName, @RequestParam("file") String fileName, HttpServletResponse res) {
        minioUtil.download(bucketName, fileName, res);
        return ResultResponse.ok(ResultResponse.ok().getCode());
    }


    /**
     * 查询所有文件信息
     *
     * @param bucketName 桶名称
     * @return {@link List}<{@link ObjectItem}>
     */
    @GetMapping("/listObjects")
    public ResultResponse listObjects(String bucketName) {
        List<ObjectItem> objectItems = minioUtil.listObjects(bucketName);
        return ResultResponse.ok(objectItems);
    }

    /**
     * 查询所有桶名称
     *
     * @return {@link ResultResponse}
     */
    @GetMapping("/listBuckets")
    public ResultResponse listBuckets() {
        List<String> listBucketNames = minioUtil.listBucketNames();
        return ResultResponse.ok(listBucketNames);
    }


    /**
     * 删除桶
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    @DeleteMapping("/deleteBucket")
    public ResultResponse deleteBucket(String bucketName) {
        String message = minioUtil.removeBucket(bucketName);
        return ResultResponse.ok(ResultResponse.ok().getCode(), message);
    }

    /**
     * 查询桶的策略信息
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    @GetMapping("/getBucketPolicy")
    public String getBucketPolicy(String bucketName) {
        return minioUtil.getBucketPolicy(bucketName);
    }

    /**
     * 定期清除存储桶中的文件内容
     */
    @Scheduled(cron = "0 0/1 14 * * ? ")
    private void clearIntervals() {
        //当前时间前一天的时间
        ZonedDateTime rangeTime = ZonedDateTime.now().minusDays(1);
        //创建定期清除策略的文件存储桶
        minioUtil.makeBucket("miniodemo");
        //删除一天之前的文件内容
        List<ObjectItem> clearIntervalsBucketList = minioUtil.listObjects("miniodemo");
        for (ObjectItem objectItem : clearIntervalsBucketList) {
            ZonedDateTime lastModified = objectItem.getLastModified();
            boolean flag = lastModified.isBefore(rangeTime);
            if (flag) {
                minioUtil.removeObject("miniodemo", objectItem.getObjectName());
            }
        }
    }
}

