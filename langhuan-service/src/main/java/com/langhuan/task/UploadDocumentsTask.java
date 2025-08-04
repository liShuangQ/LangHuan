package com.langhuan.task;

import com.langhuan.service.MinioService;
import com.langhuan.service.TRagFileService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author Afish
 * @date 2025/8/1 13:24
 */

@Slf4j
@Component
public class UploadDocumentsTask {

    @Value("${minio.file-bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    private String publicBaseUrl;

    private final String extension = ".txt";

    @Resource
    private TRagFileService tragFileService;

    @Resource
    private MinioService minioService;

    public UploadDocumentsTask(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostConstruct
    public void init() {
        // 确保 bucket 存在
        try {
            minioService.ensureBucketExists(bucketName);
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists", e);
        }

        publicBaseUrl = minioUrl + "/" + bucketName;
        if (!publicBaseUrl.endsWith("/")) {
            publicBaseUrl += "/";
        }
    }

    /**
     * 定时任务：每隔七天凌晨 2:00 执行
     */
    @Scheduled(cron = "${task.schedule.documents}")
    public void exportAndUploadDocuments() {
        log.info("【定时任务】开始执行文档导出并上传至 MinIO...");
        String folderName = java.time.LocalDate.now().toString();
        try {
            List<Integer> fileIds = tragFileService.getAllFileIdsForExport();
            log.info("共找到 {} 个 fileId 需要处理", fileIds.size());

            for (Integer fileId : fileIds) {
                try {
                    log.info("正在处理 fileId: {}", fileId);

                    // 1. 生成文档流
                    InputStreamResource resource = tragFileService.generateDocumentStreamByFileId(fileId);
                    InputStream inputStream = resource.getInputStream();

                    // 2. 转为字节数组（获取长度）
                    byte[] content = inputStream.readAllBytes();
                    if (content.length == 0) {
                        log.warn("fileId={} 的内容为空，跳过上传", fileId);
                        continue;
                    }

                    // 3. 构造 MinIO 对象名
                    String objectName = folderName + "/" + fileId + extension;

                    // 4.检查是否已存在
                    if (minioService.fileExists(objectName, bucketName)) {
                        log.info("文件已存在，跳过: {}", objectName);
                        continue;
                    }

                    // 5. 上传到 MinIO
                    try (InputStream is = new ByteArrayInputStream(content)) {
                        minioService.handleUpload(objectName, is, content.length, bucketName);
                    }

                    log.info("成功上传文档: fileId={}, 路径={}", fileId, objectName);

                } catch (Exception e) {
                    log.error("处理 fileId={} 时发生错误", fileId, e);
                    // 继续处理下一个，不中断整体任务
                }
            }

        } catch (Exception e) {
            log.error("【定时任务】执行失败", e);
        }

        log.info("【定时任务】文档导出与上传完成。");
    }
}
