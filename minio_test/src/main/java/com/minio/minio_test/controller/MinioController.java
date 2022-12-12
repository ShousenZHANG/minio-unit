package com.minio.minio_test.controller;

import com.minio.minio_test.pojo.ObjectItem;
import com.minio.minio_test.utils.MinioUtil;
import com.minio.minio_test.vo.ResultResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
        Boolean upload = minioUtil.upload(files, bucketName, null);
        return ResultResponse.ok(upload);
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
        Boolean delete = minioUtil.removeObject(bucketName, objectName);
        return ResultResponse.ok(delete);
    }

    /**
     * 创建桶
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    @PostMapping("/makeBucket")
    public String makeBucket(String bucketName) {
        Boolean flag = minioUtil.makeBucket(bucketName);
        if (!flag) {
            return "创建失败！";
        }
        return "创建成功！";
    }

    /**
     * 下载文件
     *
     * @param objectName 目标文件
     * @param fileName   下载
     * @return {@link String}
     */
//    @PostMapping("/download")
//    public String download(@RequestParam("object") String objectName, @RequestParam("file") String fileName) {
//        Boolean flag = minioUtil.downloadFile(objectName, fileName);
//        if (!flag) {
//            return "下载文件失败！";
//        }
//        return "下载文件成功！";
//    }


    /**
     * 查询所有文件信息
     *
     * @param bucketName 桶名称
     * @return {@link List}<{@link ObjectItem}>
     */
    @GetMapping("/listObjects")
    public List<ObjectItem> listObjects(String bucketName) {
        return minioUtil.listObjects(bucketName);
    }


    /**
     * 删除桶
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    @DeleteMapping("/deleteBuckets")
    public String deleteBuckets(String bucketName) {
        Boolean flag = minioUtil.removeBucket(bucketName);
        if (!flag) {
            return "删除失败！";
        }
        return "删除成功！";
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

