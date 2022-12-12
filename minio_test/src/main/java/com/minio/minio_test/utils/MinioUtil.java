package com.minio.minio_test.utils;

import com.minio.minio_test.exception.FileTransferException;
import com.minio.minio_test.pojo.ObjectItem;
import com.minio.minio_test.vo.ResultResponse;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.*;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

/**
 * minio工具类
 *
 * @author zhang
 * @date 2022/11/29
 */
@Component
public class MinioUtil {
    /**
     * minio客户端
     */
    @Resource
    private MinioClient minioClient;

    /**
     * 查看存储bucket是否存在
     *
     * @param bucketName 存储桶名称
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Existence of bucket has error!", e);
        }
        return found;
    }

    /**
     * 创建存储bucket
     *
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            // 如存储桶不存在，创建之。
            Boolean found = bucketExists(bucketName);
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Make bucket has error!", e);
        }
        return true;
    }

    /**
     * 删除存储桶bucket
     *
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            Boolean found = bucketExists(bucketName);
            if (found) {
                minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Remove bucket has error!", e);
        }
        return true;
    }

    /**
     * 上传文件
     *
     * @param multipartFile 文件对象集合
     * @param bucketName    文件桶名称
     * @param directory     上传的路径地址
     * @return {@link Boolean}
     */
    public Boolean upload(List<MultipartFile> multipartFile, String bucketName, String directory) {
        for (MultipartFile file : multipartFile) {
            try {
                InputStream in = file.getInputStream();
                directory = Optional.ofNullable(directory).orElse("");
                String minFileName = directory + file.getOriginalFilename();
                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(minFileName).stream(in, in.available(), -1).contentType(file.getContentType()).build());
                in.close();
            } catch (Exception e) {
                throw new FileTransferException(ResultResponse.error().getCode(), "Upload file has error!", e);
            }
        }
        return true;
    }

    /**
     * 上传指定文件
     *
     * @param bucketName 文件桶名称
     * @param objectName 桶内文件名称
     * @param fileName   需要上传的文件全路径名称
     * @return {@link Boolean}
     */
    public Boolean uploadObject(String bucketName, String objectName, String fileName) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder().bucket(bucketName).object(objectName).filename(fileName).build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Upload object has error!", e);
        }
        return true;
    }

    /**
     * 文件下载
     *
     * @param bucketName 存储桶bucket名称
     * @param fileName   文件名称
     * @param res        response
     */
    public void download(String bucketName, String fileName, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucketName)
                .object(fileName).build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs)) {
            byte[] buf = new byte[1024];
            int len;
            try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()) {
                while ((len = response.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
                os.flush();
                byte[] bytes = os.toByteArray();
                res.setCharacterEncoding("utf-8");
                res.setContentType("application/force-download");
                res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                try (ServletOutputStream stream = res.getOutputStream()) {
                    stream.write(bytes);
                    stream.flush();
                }
            }
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Download object has error!", e);

        }
    }

    /**
     * 下载文件到本地磁盘位置
     *
     * @param bucketName   存储桶名称
     * @param objectName   桶中文件名称
     * @param diskFileName 本地磁盘文件名称，全路径
     */
    public void downloadToLocalDisk(String bucketName, String objectName, String diskFileName) {
        try {
            minioClient.downloadObject(DownloadObjectArgs.builder().bucket(bucketName).object(objectName).filename(diskFileName).build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Download to local disk has error!", e);
        }
    }

    /**
     * 查看文件对象
     *
     * @param bucketName 存储bucket名称
     * @return 存储bucket内文件对象信息
     */
    public List<ObjectItem> listObjects(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        List<ObjectItem> objectItems = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                ObjectItem objectItem = new ObjectItem();
                objectItem.setObjectName(item.objectName());
                objectItem.setSize(item.size());
                objectItems.add(objectItem);
            }
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "List objects has error!", e);
        }
        return objectItems;
    }

    /**
     * 查询桶的策略
     *
     * @param bucketName 桶名称
     * @return {@link String}
     */
    public String getBucketPolicy(String bucketName) {
        String message = null;
        try {
            message = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Get bucket policy has error!", e);
        }
        return message;
    }


    /**
     * 查询所有桶
     *
     * @return {@link List}<{@link Bucket}>
     */
    @SneakyThrows
    private List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 获取所有桶的名称
     *
     * @return {@link List}<{@link String}>
     */
    public List<String> listBucketNames() {
        List<Bucket> bucketList = listBuckets();
        List<String> bucketListName = new ArrayList<>();
        for (Bucket bucket : bucketList) {
            bucketListName.add(bucket.name());
        }
        return bucketListName;
    }

    /**
     * 单个文件的删除
     *
     * @param bucketName 文件桶名称
     * @param objectName 文件名称
     * @return boolean
     */
    public boolean removeObject(String bucketName, String objectName) {
        if (!bucketExists(bucketName)) {
            return false;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Remove object has error!", e);
        }
        return true;
    }

    /**
     * 批量删除多个文件
     *
     * @param bucketName  文件桶名称
     * @param objectNames 文件名称
     * @return {@link Boolean}
     */
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

    /**
     * 创建上传文件对象的外链
     *
     * @param bucketName 存储桶名称
     * @param objectName 要上传文件对象的名称
     * @param expiry     过期时间(分钟) 最大为7天，超过7天的为默认最大值
     * @return {@link String}
     */
    public String createUploadUrl(String bucketName, String objectName, Integer expiry) {
        String url;
        expiry = expiryHandle(expiry);
        try {
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Create upload url has error!", e);
        }
        return url;
    }

    /**
     * 创建上传文件对象的外链(默认一个小时到期)
     *
     * @param bucketName 存储桶名称
     * @param objectName 要上传文件对象的名称
     * @return {@link String}
     */
    public String createUploadUrl(String bucketName, String objectName) {
        return createUploadUrl(bucketName, objectName, 60);
    }

    /**
     * 获取访问对象的外链地址
     * 获取文件的下载的url
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry     过期时间（分钟） 最大为7天
     * @return {@link String}
     */
    public String getObjectUrl(String bucketName, String objectName, Integer expiry) {
        String url = null;
        expiry = expiryHandle(expiry);
        try {
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Get object url has error!", e);
        }
        return url;
    }

    /**
     * 将分钟数转换为秒数
     *
     * @param expiry 过期时间(分钟数)
     * @return int
     */
    private static int expiryHandle(Integer expiry) {
        expiry = expiry * 60;
        if (expiry > 604800) {
            return 604800;
        }
        return expiry;
    }

//    @Scheduled(cron = "0 0 10,14,17 * * ?")
//    public void timeRemoveObjects(String bucketName) {
//        try {
//            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object("平台流水表分区脚本说明.pdf").build());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}

