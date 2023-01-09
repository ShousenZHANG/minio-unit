package com.minio.minio_test.service.serviceImpl;

import com.minio.minio_test.exception.FileTransferException;
import com.minio.minio_test.pojo.ObjectItem;
import com.minio.minio_test.service.MinioService;
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
public class MinioServiceImpl implements MinioService {

    @Resource
    private MinioClient minioClient;

    public Boolean bucketExists(String bucketName) {
        boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Existence of bucket has error!", e);
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
            throw new FileTransferException(ResultResponse.error().getCode(), "Make bucket has error!", e);
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
            throw new FileTransferException(ResultResponse.error().getCode(), "Remove bucket has error!", e);
        }
        return "Remove bucket successfully！";
    }

    public String upload(List<MultipartFile> multipartFile, String bucketName, String directory) {
        if (!bucketExists(bucketName)) {
            return "Bucket is not in existence！";
        }
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
        return "Upload files successfully";
    }

    public Boolean uploadObject(String bucketName, String objectName, String fileName) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder().bucket(bucketName).object(objectName).filename(fileName).build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Upload object has error!", e);
        }
        return true;
    }

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
                //设置强制下载关闭
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

    public String downloadToLocalDisk(String bucketName, String objectName, String diskFileName) {
        try {
            if (!bucketExists(bucketName)) {
                return "Bucket is not in existence！";
            }
            minioClient.downloadObject(DownloadObjectArgs.builder().bucket(bucketName).object(objectName).filename(diskFileName).build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Download to local disk has error!", e);
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
            throw new FileTransferException(ResultResponse.error().getCode(), "List objects has error!", e);
        }
        return objectItems;
    }

    public String getBucketPolicy(String bucketName) {
        String message = null;
        try {
            message = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new FileTransferException(ResultResponse.error().getCode(), "Get bucket policy has error!", e);
        }
        return message;
    }


    @SneakyThrows
    private List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    public List<String> listBucketNames() {
        List<Bucket> bucketList = listBuckets();
        List<String> bucketListName = new ArrayList<>();
        for (Bucket bucket : bucketList) {
            bucketListName.add(bucket.name());
        }
        return bucketListName;
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
            throw new FileTransferException(ResultResponse.error().getCode(), "Remove object has error!", e);
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
            throw new FileTransferException(ResultResponse.error().getCode(), "Create upload url has error!", e);
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

}

