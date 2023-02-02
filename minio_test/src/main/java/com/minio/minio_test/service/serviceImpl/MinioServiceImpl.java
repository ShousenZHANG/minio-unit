package com.minio.minio_test.service.serviceImpl;

import com.minio.minio_test.exception.BusinessException;
import com.minio.minio_test.vo.BucketVO;
import com.minio.minio_test.vo.FileItemVO;
import com.minio.minio_test.service.MinioService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.*;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new BusinessException(e.getMessage());
        }
        return found;
    }

    public void makeBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                LOGGER.info("创建文件桶成功.bucketName为:{}", bucketName);
            } else {
                throw new BusinessException("bucket already exit");
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error("创建文件桶失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    public void removeBucket(String bucketName) {
        try {
            if (bucketExists(bucketName)) {
                minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            } else {
                throw new BusinessException("bucket does not exit");
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error("删除文件桶失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    public void upload(List<MultipartFile> multipartFile, String bucketName) {
        if (!bucketExists(bucketName)) {
            throw new BusinessException("Bucket does not exit");
        }
        InputStream in = null;
        for (MultipartFile file : multipartFile) {
            try {
                in = file.getInputStream();
                String minFileName = file.getOriginalFilename();
                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(minFileName).stream(in, in.available(), -1).contentType(file.getContentType()).build());
                LOGGER.info("上传文件-成功.文件名:{}, 存储桶名:{}", minFileName, bucketName);
            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                LOGGER.error("上传文件-失败:" + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }

    public Boolean uploadObject(String bucketName, String objectName, String fileName) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder().bucket(bucketName).object(objectName).filename(fileName).build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new BusinessException(e.getMessage());
        }
        return true;
    }

    public void download(String bucketName, String fileName, HttpServletResponse response) {
        InputStream object = null;
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(fileName).build());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/force-download");
            response.setCharacterEncoding("UTF-8");
            GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucketName)
                    .object(fileName).build();
            object = minioClient.getObject(objectArgs);
            IOUtils.copy(object, response.getOutputStream());
            LOGGER.info("下载文件-成功.文件名:{}, 存储桶名:{}", fileName, bucketName);
        } catch (Exception e) {
            LOGGER.error("下载文件失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(object);
        }
    }

    public void downloadToLocalDisk(String bucketName, String objectName, String diskFileName) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            minioClient.downloadObject(DownloadObjectArgs.builder().bucket(bucketName).object(objectName).filename(diskFileName).build());
            LOGGER.info("下载文件到本地磁盘-成功.文件名:{}, 本地磁盘文件名:{},存储桶名:{}", objectName, diskFileName, bucketName);
        } catch (Exception e) {
            LOGGER.error("下载文件到本地磁盘失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    public List<FileItemVO> listObjects(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                throw new BusinessException("bucket does not exit");
            }
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).build();
            Iterable<Result<Item>> it = minioClient.listObjects(args);
            if (it != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.of("Asia/Shanghai"));
                List<FileItemVO> list = new ArrayList<>();
                FileItemVO vo;
                for (Result<Item> result : it) {
                    Item item = result.get();
                    if (item != null) {
                        vo = FileItemVO.builder().name(item.objectName())
                                .ownerName(item.owner() == null ? "" : item.owner().displayName()).size(item.size())
                                .isDir(item.isDir()).encodingType("").build();
                        if (item.lastModified() != null) {
                            vo.setLastModifyTime(item.lastModified().format(formatter));
                        }
                        list.add(vo);
                    }
                }
                return list;
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error("查询目标文件桶中文件失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    public String getBucketPolicy(String bucketName) {
        String message;
        try {
            message = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            LOGGER.error("查询文件桶策略失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
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

    public void removeObject(String bucketName, String objectName) {
        try {
            //判断文件桶或者文件是否存在，如果不存在就抛出异常
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            LOGGER.error("删除文件失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
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

    public String getObjectUrl(String bucketName, String objectName, Integer expiry) {
        String url;
        expiry = expiryHandle(expiry);
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );
        } catch (Exception e) {
            LOGGER.error("获取文件下载地址失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return url;
    }

    public String createUploadUrl(String bucketName, String objectName, Integer expiry) {
        String url;
        expiry = expiryHandle(expiry);
        try {
            if (!bucketExists(bucketName)) {
                throw new BusinessException("bucket does not exit");
            }
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error("获取文件上传地址失败-失败:" + e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return url;
    }

    public void downloadUrl(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("下载文件-开始");
        // 网络链接（文件的http链接）
        String filenamerel = request.getParameter("filenamerel");
        // 文件名
        String fileName = request.getParameter("filename");

        if (StringUtils.isEmpty(fileName)) {
            fileName = this.getFileNameFormUrl(filenamerel);
        }
        InputStream is = null;
        OutputStream os = null;

        try {
            // 从网络读取文件
            URL url = new URL(filenamerel);
            URLConnection conn = url.openConnection();

            is = conn.getInputStream();
            os = response.getOutputStream();

            String userAgent = request.getHeader("user-agent").toLowerCase();

            if (userAgent.contains("msie") || userAgent.contains("like gecko")) {
                // win10 ie edge 浏览器 和其他系统的ie
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                // 文件名转码
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }
            response.setContentType("application/octet-stream; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 文件缓存区
            byte[] bytes = new byte[1024];
            // 判断输入流中的数据是否已经读完的标致
            int len;
            while ((len = is.read(bytes)) > 0) {
                os.write(bytes, 0, len);
            }
            LOGGER.info("下载文件-结束.文件:{}", filenamerel);
        } catch (Exception e) {
            LOGGER.error(String.format("下载文件-失败!文件:%s", filenamerel), e);
            throw new BusinessException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(os, is);
        }
    }

    /**
     * 获取HTTP中的文件名
     *
     * @param filenamerel HTTP链接
     * @return {@link String}
     */
    private String getFileNameFormUrl(String filenamerel) {
        if (StringUtils.isEmpty(filenamerel)) {
            return "";
        }
        int lastIdx = filenamerel.lastIndexOf("/");

        String fileName = "";

        if (lastIdx >= 0) {
            fileName = filenamerel.substring(lastIdx + 1);
        }
        return fileName;
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

