package com.minio.minio_test.service.serviceImpl;

import com.minio.minio_test.Response.ResponseData;
import com.minio.minio_test.exception.BusinessException;
import com.minio.minio_test.vo.BucketVO;
import com.minio.minio_test.vo.ObjectItem;
import com.minio.minio_test.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.*;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * minio工具类
 *
 * @author zhang
 * @date 2022/11/29
 */
@Component
public class MinioServiceImpl implements MinioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioServiceImpl.class);

    @Resource
    private MinioClient minioClient;

    public Boolean bucketExists(String bucketName) {
        boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new BusinessException(ResponseData.error().getCode(), "Existence of bucket has error!", e);
        }
        return found;
    }

    public String makeBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                return "Bucket is already in existence";
            }
        } catch (Exception e) {
            throw new BusinessException(ResponseData.error().getCode(), "Make bucket has error!", e);
        }
        return "Make bucket successfully";
    }

    public String removeBucket(String bucketName) {
        try {
            if (bucketExists(bucketName)) {
                minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            } else {
                return "Bucket is not in existence！";
            }
        } catch (Exception e) {
            throw new BusinessException(ResponseData.error().getCode(), "Remove bucket has error!", e);
        }
        return "Remove bucket successfully！";
    }

    public String upload(List<MultipartFile> multipartFile, String bucketName) {
        if (!bucketExists(bucketName)) {
            return "Bucket is not in existence！";
        }
        for (MultipartFile file : multipartFile) {
            try {
                InputStream in = file.getInputStream();
                String minFileName = file.getOriginalFilename();
                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(minFileName).stream(in, in.available(), -1).contentType(file.getContentType()).build());
                LOGGER.info("上传文件-成功.文件名:{}, 存储桶名:{}", minFileName, bucketName);
                in.close();
            } catch (Exception e) {
                LOGGER.error("上传文件-失败:" + e.getMessage(), e);
                throw new BusinessException("Upload files unsuccessfully");
            }
        }
        return "Upload files successfully";
    }

    public Boolean uploadObject(String bucketName, String objectName, String fileName) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder().bucket(bucketName).object(objectName).filename(fileName).build());
        } catch (Exception e) {
            throw new BusinessException(ResponseData.error().getCode(), "Upload object has error!", e);
        }
        return true;
    }

    public void download(String bucketName, String fileName, HttpServletResponse response) {
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/force-download");
            response.setCharacterEncoding("UTF-8");
            GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucketName)
                    .object(fileName).build();
            GetObjectResponse object = minioClient.getObject(objectArgs);
            IOUtils.copy(object, response.getOutputStream());
            LOGGER.info("下载文件-成功.文件名:{}, 存储桶名:{}", fileName, bucketName);
            IOUtils.closeQuietly(object);
        } catch (Exception e) {
            LOGGER.error("下载文件失败-失败:" + e.getMessage(), e);
            throw new BusinessException("Download object has error!");
        }
    }

    public String downloadToLocalDisk(String bucketName, String objectName, String diskFileName) {
        try {
            if (!bucketExists(bucketName)) {
                return "Bucket is not in existence！";
            }
            minioClient.downloadObject(DownloadObjectArgs.builder().bucket(bucketName).object(objectName).filename(diskFileName).build());
        } catch (Exception e) {
            throw new BusinessException(ResponseData.error().getCode(), "Download to local disk has error!", e);
        }
        return "Download to local disk successfully";
    }

    public List<ObjectItem> listObjects(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        List<ObjectItem> objectItems = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                ObjectItem objectItem = new ObjectItem();
                objectItem.setLastModified(item.lastModified());
                objectItem.setObjectName(item.objectName());
                objectItem.setSize(item.size());
                objectItems.add(objectItem);
            }
        } catch (Exception e) {
            throw new BusinessException(ResponseData.error().getCode(), "List objects has error!", e);
        }
        return objectItems;
    }

    public String getBucketPolicy(String bucketName) {
        String message = null;
        try {
            message = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new BusinessException(ResponseData.error().getCode(), "Get bucket policy has error!", e);
        }
        return message;
    }


    @SneakyThrows
    private List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    public List<BucketVO> listBucketNames() {
        List<Bucket> list = listBuckets();
        if (!(CollectionUtils.isEmpty(list))) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.of("Asia/Shanghai"));

            return list.stream().map(o -> {
                ZonedDateTime creationDate = o.creationDate();
                BucketVO vo = new BucketVO();
                vo.setName(o.name());
                vo.setCreateTime(creationDate.format(formatter));
                return vo;
            }).collect(Collectors.toList());
        }
        return null;
    }

    public String removeObject(String bucketName, String objectName) {
        if (!bucketExists(bucketName)) {
            return "Bucket is not in existence！";
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new BusinessException(ResponseData.error().getCode(), "Remove object has error!", e);
        }
        return "Successfully deleted！";
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
            throw new BusinessException(ResponseData.error().getCode(), "Create upload url has error!", e);
        }
        return url;
    }

    public String createUploadUrl(String bucketName, String objectName) {
        return createUploadUrl(bucketName, objectName, 60);
    }

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
            throw new BusinessException(ResponseData.error().getCode(), "Get object url has error!", e);
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

}

