package com.langhuan.utils.minio;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;

/**
 * @author Afish
 * @date 2025/7/29 09:25
 */
@Slf4j
public class MinioUtils {

    private MinioClient minioClient;
    private String bucketName;
    private String minioUrl;


    public MinioUtils(MinioClient minioClient, String bucketName, String minioUrl) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.minioUrl = minioUrl;
    }

    public void createBucket() throws Exception {
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public void uploadFile(String objectName, InputStream inputStream, long size) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, size, 10 * 1024 * 1024)
                        .build()
        );
    }

    public InputStream downloadFile(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 从 URL 中提取 MinIO 的 objectName
     */
    public String extractObjectName(String url) {
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

}
