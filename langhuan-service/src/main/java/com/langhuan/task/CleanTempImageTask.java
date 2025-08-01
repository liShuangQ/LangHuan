package com.langhuan.task;

import com.langhuan.model.domain.TFileUrl;
import com.langhuan.service.MinioService;
import com.langhuan.service.TFileUrlService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Afish
 * @date 2025/7/31 14:44
 */
@Component
@Slf4j
public class CleanTempImageTask {
    @Resource
    private TFileUrlService tFileUrlService;

    @Resource
    private MinioService minioService;

    /**
     * 定时任务：每天凌晨 0 点执行，清理超过 1 小时的临时图片
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天 00:00:00 执行
    @Transactional(rollbackFor = Exception.class)
    public void cleanupTemporaryImages() {
        log.info("Starting cleanup of temporary images at midnight...");


        // 查询 fStatus='临时'的记录
        List<TFileUrl> expiredUrls = tFileUrlService.lambdaQuery()
                .eq(TFileUrl::getFStatus, "临时")
                .list();

        if (expiredUrls.isEmpty()) {
            log.info("No expired temporary images found.");
            return;
        }

        // 提取需要删除的 MinIO objectNames
        List<String> objectNames = expiredUrls.stream()
                .map(TFileUrl::getFUrl)
                .map(minioService::extractObjectName)
                .filter(objName -> objName != null && !objName.isEmpty())
                .toList();

        // 删除 MinIO 中的对象
        for (String objectName : objectNames) {
            try {
                minioService.handleDelete(objectName);
                log.info("Successfully removed MinIO object: {}", objectName);
            } catch (Exception e) {
                log.error("Failed to remove MinIO object: {}", objectName, e);
            }
        }

        // 获取要删除的记录ID列表
        List<Integer> idsToDelete = expiredUrls.stream()
                .map(TFileUrl::getId)
                .collect(Collectors.toList());

        // 批量删除数据库记录
        if (!idsToDelete.isEmpty()) {
            boolean deleteSuccess = tFileUrlService.removeByIds(idsToDelete);
            if (deleteSuccess) {
                log.info("Successfully deleted {} expired records from database.", idsToDelete.size());
            } else {
                log.error("Failed to delete some or all expired records from database.");
            }
        }

        log.info("Finished daily cleanup. Total cleaned: {} files", expiredUrls.size());
    }

}
