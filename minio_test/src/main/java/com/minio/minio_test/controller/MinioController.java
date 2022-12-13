package com.minio.minio_test.controller;

import com.minio.minio_test.pojo.ObjectItem;
import com.minio.minio_test.utils.MinioUtil;
import com.minio.minio_test.vo.ResultResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
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
     * 下载文件
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

}

