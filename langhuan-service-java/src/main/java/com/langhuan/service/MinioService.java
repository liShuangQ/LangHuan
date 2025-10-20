package com.langhuan.service;

import com.langhuan.utils.minio.MinioUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.minio.MinioClient;

import java.io.InputStream;


/**
 * @author Afish
 * @date 2025/7/28 17:24
 */
@Service
@Slf4j
public class MinioService {

    @Resource
    private MinioClient minioClient;

    @Value("${minio.url}")
    private String minioUrl;

    private final MinioUtils minioUtils;

    // 使用构造函数注入
    public MinioService(MinioClient minioClient, @Value("${minio.url}") String minioUrl) {
        this.minioClient = minioClient;
        this.minioUrl = minioUrl;
        this.minioUtils = new MinioUtils(minioClient, minioUrl);
    }

    public void handleUpload(String objectName, InputStream inputStream, long size, String bucketName) throws Exception {
        // 可以在此添加额外的业务逻辑
        minioUtils.uploadFile(objectName, inputStream, size, bucketName);
    }

    public InputStream handleDownload(String objectName, String bucketName) throws Exception {
        // 可以在此添加额外的业务逻辑
        return minioUtils.downloadFile(objectName, bucketName);
    }

    public void handleDelete(String objectName, String bucketName) throws Exception {
        // 可以在此添加额外的业务逻辑
        minioUtils.deleteFile(objectName, bucketName);
    }

    public void ensureBucketExists(String bucketName) throws Exception {
        minioUtils.createBucket(bucketName);
    }

    public String extractObjectName(String url, String bucketName){
        return minioUtils.extractObjectName(url, bucketName);
    }

    public boolean fileExists(String objectName, String bucketName){
        return minioUtils.fileExists(objectName, bucketName);
    }

    // 生成minio访问链接
    public String generateMinioUrl(String objectName, String bucketName) {
        return minioUtils.generateMinioUrl(objectName, bucketName);
    }
    // 删除文件夹下所有文件和文件夹本身
    public void deleteFolder(String folderName, String bucketName) throws Exception {
        minioUtils.deleteFolder(folderName, bucketName);
    }
}
