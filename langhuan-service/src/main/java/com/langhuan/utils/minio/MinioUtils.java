package com.langhuan.utils.minio;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;


@Slf4j
public class MinioUtils {

    private MinioClient minioClient;
    private String minioUrl;


    public MinioUtils(MinioClient minioClient, String minioUrl) {
        this.minioClient = minioClient;
        this.minioUrl = minioUrl;
    }

    public void createBucket(String bucketName) throws Exception {
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public void uploadFile(String objectName, InputStream inputStream, long size, String bucketName) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, size, 10 * 1024 * 1024)
                        .build()
        );
    }

    /**
     * 上传MultipartFile格式的文件到MinIO
     *
     * @param objectName 文件对象名称（即路径+文件名）
     * @param file       MultipartFile格式的文件
     * @param bucketName 存储桶名称
     * @throws Exception 上传过程中可能抛出的异常
     */
    public void uploadFile(String objectName, MultipartFile file, String bucketName) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            uploadFile(objectName, inputStream, file.getSize(), bucketName);
        }
    }

    public InputStream downloadFile(String objectName, String bucketName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    public void deleteFile(String objectName, String bucketName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    // 生成minio访问链接
    public String generateMinioUrl(String objectName, String bucketName) {
        return minioUrl + "/" + bucketName + "/" + objectName;
    }

    /**
     * 从 URL 中提取 MinIO 的 objectName
     */
    public String extractObjectName(String url, String bucketName) {
        if (url == null || url.isEmpty()) return null;
        try {
            String baseUrl = minioUrl + "/" + bucketName + "/";
            if (url.startsWith(baseUrl)) {
                return url.substring(baseUrl.length());
            }
            log.warn("Unrecognized MinIO URL format: {}", url);
            return null;
        } catch (Exception e) {
            log.error("Error extracting object name from URL: {}", url, e);
            return null;
        }
    }

    /**
     * 判断指定 objectName 的文件是否存在于 MinIO 的 bucket 中
     *
     * @param objectName 文件对象名称（即路径+文件名）
     * @return true 如果文件存在，false 不存在或发生异常
     */
    public boolean fileExists(String objectName, String bucketName) {
        if (objectName == null || objectName.isEmpty()) {
            log.warn("Object name is null or empty.");
            return false;
        }

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true; // 能成功获取元信息，说明文件存在
        } catch (Exception e) {
            // 文件不存在会抛出异常（如 404），其他可能是网络等问题
            if (e.getMessage().contains("404") || e instanceof io.minio.errors.ErrorResponseException) {
                log.debug("File not found in MinIO: {}", objectName);
                return false;
            }
            log.error("Error occurred while checking file existence: {}", objectName, e);
            return false;
        }
    }

    /**
     * 删除文件夹及其所有内容
     */
    public void deleteFolder(String folderName, String bucketName) throws Exception {
        if (folderName == null || folderName.isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be null or empty");
        }

        // 确保文件夹名以 / 结尾，表示一个目录
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }

        // 列出该文件夹下的所有对象
        List<DeleteObject> objectsToDelete = new LinkedList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(folderName)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            objectsToDelete.add(new DeleteObject(item.objectName()));
        }

        // 批量删除所有对象
        if (!objectsToDelete.isEmpty()) {
            Iterable<Result<DeleteError>> deleteErrors = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objectsToDelete)
                            .build()
            );

            // 检查是否有删除错误
            for (Result<DeleteError> deleteError : deleteErrors) {
                DeleteError error = deleteError.get();
                log.error("Error deleting object: {} - {}", error.objectName(), error.message());
            }
        }
    }

}
