package com.langhuan.task;

import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.service.MinioService;
import com.langhuan.service.TRagFileGroupService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UploadDocumentsTask {

    @Value("${minio.file-bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    private String publicBaseUrl;

    @Resource
    private TRagFileService ragFileService;

    @Resource
    private TRagFileGroupService ragFileGroupService;

    @Resource
    private MinioService minioService;

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

        // 1. 获取当前日期作为文件夹名
        String folderName = LocalDate.now().toString();

        try {
            // 2. 查询所有文件组，获取文件组ID和名称映射
            List<TRagFileGroup> fileGroups = ragFileGroupService.list();
            Map<Integer, String> groupMap = fileGroups.stream()
                    .collect(Collectors.toMap(TRagFileGroup::getId, TRagFileGroup::getGroupName));

            if (groupMap.isEmpty()) {
                log.warn("未找到任何文件组，跳过上传任务。");
                return;
            }

            log.info("共找到 {} 个文件组", groupMap.size());

            // 3. 查询所有有效文件
            List<TRagFile> allFiles = ragFileService.list();
            log.info("共找到 {} 个文件", allFiles.size());

            for (TRagFile file : allFiles) {
                int fileGroupId = Integer.parseInt(file.getFileGroupId());
                String fileName = file.getFileName();
                Integer fileId = file.getId();

                if (fileName == null || fileName.isBlank()) {
                    fileName = "未知文件"; // 默认名
                }

                // 去除扩展名（如果包含）
                String baseName = fileName.contains(".")
                        ? fileName.substring(0, fileName.lastIndexOf('.'))
                        : fileName;

                String groupName = groupMap.getOrDefault(fileGroupId, "未知组");

                try {
                    log.info("正在处理文件: {} - {}", groupName, baseName);

                    // 4. 生成文档流
                    InputStreamResource resource = ragFileService.generateDocumentStreamByFileId(fileId);
                    InputStream inputStream = resource.getInputStream();
                    byte[] content = inputStream.readAllBytes();

                    if (content.length == 0) {
                        log.warn("fileId={} 的内容为空，跳过上传", fileId);
                        continue;
                    }

                    // 5. 构造对象名：时间/文件组名-文件名.txt
                    String objectName = folderName + "/" +
                            groupName.trim() + "-" +
                            baseName.trim() + ".txt";

                    // 6. 检查是否已存在
                    if (minioService.fileExists(objectName, bucketName)) {
                        log.info("文件已存在，跳过: {}", objectName);
                        continue;
                    }

                    // 7. 上传到 MinIO
                    try (InputStream is = new ByteArrayInputStream(content)) {
                        minioService.handleUpload(objectName, is, content.length, bucketName);
                    }

                    log.info("成功上传文档: {} -> {}", fileId, objectName);

                } catch (Exception e) {
                    log.error("处理文件失败: fileId={}, name={}", fileId, fileName, e);
                }
            }

        } catch (Exception e) {
            log.error("【定时任务】执行失败", e);
        }

        log.info("【定时任务】文档导出与上传完成。");
    }
}