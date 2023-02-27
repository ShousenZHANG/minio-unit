package com.minio.minio_test.service;

import com.minio.minio_test.vo.BucketVO;
import com.minio.minio_test.vo.FileItemVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Minio的Service接口层
 *
 * @author zhang
 * @date 2023/01/09
 */
public interface MinioService {

    /**
     * 查看存储bucket是否存在
     *
     * @param bucketName 存储桶名称
     * @return boolean
     */
    Boolean bucketExists(String bucketName);

    /**
     * 创建存储bucket
     *
     * @param bucketName 存储bucket名称
     */
    void makeBucket(String bucketName);

    /**
     * 删除存储桶bucket
     *
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    void removeBucket(String bucketName);

    /**
     * 上传文件
     *
     * @param multipartFile 文件对象集合
     * @param bucketName    文件桶名称
     */
    void upload(List<MultipartFile> multipartFile, String bucketName);

    /**
     * 上传指定文件
     *
     * @param bucketName 文件桶名称
     * @param objectName 桶内文件名称
     * @param fileName   需要上传的文件全路径名称
     * @return {@link Boolean}
     */
    Boolean uploadObject(String bucketName, String objectName, String fileName);

    /**
     * 文件下载
     *
     * @param bucketName 存储桶bucket名称
     * @param fileName   文件名称
     * @param res        response
     */
    void download(String bucketName, String fileName, HttpServletResponse res);

    /**
     * 下载文件到本地磁盘位置
     *
     * @param bucketName   存储桶名称
     * @param objectName   桶中文件名称
     * @param diskFileName 本地磁盘文件名称，全路径
     * @return {@link String}
     */
    void downloadToLocalDisk(String bucketName, String objectName, String diskFileName);

    /**
     * 查看文件对象
     *
     * @param bucketName 存储bucket名称
     * @return 存储bucket内文件对象信息
     */
    List<FileItemVO> listObjects(String bucketName);

    /**
     * 查询桶的策略
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    String getBucketPolicy(String bucketName);

    /**
     * 获取所有桶的名称
     *
     * @return {@link List}<{@link String}>
     */
    List<BucketVO> listBucketNames();

    /**
     * 单个文件的删除
     *
     * @param bucketName 文件桶名称
     * @param objectName 文件名称
     */
    void removeObject(String bucketName, String objectName);

    /**
     * 批量删除多个文件
     *
     * @param bucketName  文件桶名称
     * @param objectNames 文件名称
     * @return {@link Boolean}
     */
    Boolean removeObjects(String bucketName, List<String> objectNames);

    /**
     * 获取访问对象的外链地址
     * 获取文件的下载的url
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry     过期时间（分钟） 最大为7天
     * @return {@link String}
     */
    String getObjectUrl(String bucketName, String objectName, Integer expiry);

    /**
     * 创建上传文件链接
     *
     * @param bucketName 文件桶名称
     * @param objectName 文件名称
     * @param expiry     到期时间
     * @return {@link String}
     */
    String createUploadUrl(String bucketName, String objectName, Integer expiry);

    /**
     * 通过文件链接地址进行文件下载
     *
     * @param request  请求
     * @param response 响应
     */
    void downloadUrl(HttpServletRequest request, HttpServletResponse response);

}
