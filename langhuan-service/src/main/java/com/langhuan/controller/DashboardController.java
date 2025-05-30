package com.langhuan.controller;

import com.langhuan.common.Result;
import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TUser;
import com.langhuan.service.TChatFeedbackService;
import com.langhuan.service.TPromptsService;
import com.langhuan.service.TRagFileGroupService;
import com.langhuan.service.TRagFileService;
import com.langhuan.service.TUserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘控制器
 * 提供系统统计信息相关接口
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final TChatFeedbackService tChatFeedbackService;
    private final TPromptsService tPromptsService;
    private final TRagFileGroupService tRagFileGroupService;
    private final TRagFileService tRagFileService;
    private final TUserService tUserService;

    public DashboardController(TChatFeedbackService tChatFeedbackService, TPromptsService tPromptsService,
            TRagFileGroupService tRagFileGroupService, TRagFileService tRagFileService, TUserService tUserService) {
        this.tChatFeedbackService = tChatFeedbackService;
        this.tPromptsService = tPromptsService;
        this.tRagFileGroupService = tRagFileGroupService;
        this.tRagFileService = tRagFileService;
        this.tUserService = tUserService;

    }


    /**
     * 获取反馈统计信息
     * 包括总数、按类型统计、按用户统计
     * 
     * @return 反馈统计结果
     */
    @PostMapping("/feedbackStats")
    public Result<Map<String, Object>> getFeedbackStats() {
        // 获取所有反馈数据
        List<TChatFeedback> allFeedbacks = tChatFeedbackService.list();

        // 总反馈数量
        long totalCount = allFeedbacks.size();

        // 按类型统计
        Map<String, Long> typeStats = allFeedbacks.stream()
                .collect(Collectors.groupingBy(
                        feedback -> feedback.getInteraction() != null ? feedback.getInteraction() : "unknown",
                        Collectors.counting()));

        // 按用户统计
        Map<String, Long> userStats = allFeedbacks.stream()
                .collect(Collectors.groupingBy(
                        feedback -> feedback.getUserId() != null ? feedback.getUserId() : "unknown",
                        Collectors.counting()));

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("typeStats", typeStats);
        result.put("userStats", userStats);

        // 单独统计各类型数量，便于前端使用
        result.put("likeCount", typeStats.getOrDefault("like", 0L));
        result.put("dislikeCount", typeStats.getOrDefault("dislike", 0L));
        result.put("endCount", typeStats.getOrDefault("end", 0L));

        return Result.success(result);
    }

    /**
     * 获取提示词统计信息
     * 
     * @return 提示词统计结果
     */
    @PostMapping("/promptsStats")
    public Result<Map<String, Object>> getPromptsStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", tPromptsService.count());

        return Result.success(result);
    }

    /**
     * 获取文件组统计信息
     * 包括总数和按类型统计
     * 
     * @return 文件组统计结果
     */
    @PostMapping("/fileGroupStats")
    public Result<Map<String, Object>> getFileGroupStats() {
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", tRagFileGroupService.count());
        
        return Result.success(result);
    }

    /**
     * 获取文件统计信息
     * 包括文件数量、文件总大小、文件切割数总和、上传用户数
     * 
     * @return 文件统计结果
     */
    @PostMapping("/fileStats")
    public Result<Map<String, Object>> getFileStats() {
        // 获取所有文件数据
        List<TRagFile> allFiles = tRagFileService.list();

        // 文件总数量
        long totalFileCount = allFiles.size();

        // 计算文件总大小（处理非数字字符）
        long totalFileSize = 0;
        for (TRagFile file : allFiles) {
            if (file.getFileSize() != null && !file.getFileSize().isEmpty()) {
                try {
                    // 提取数字部分，去除非数字字符
                    String sizeStr = file.getFileSize().replaceAll("[^0-9]", "");
                    if (!sizeStr.isEmpty()) {
                        totalFileSize += Long.parseLong(sizeStr);
                    }
                } catch (NumberFormatException e) {
                    // 忽略无法解析的文件大小
                }
            }
        }

        // 计算文件切割数总和（处理非数字字符）
        long totalDocumentNum = 0;
        for (TRagFile file : allFiles) {
            if (file.getDocumentNum() != null && !file.getDocumentNum().isEmpty()) {
                try {
                    // 提取数字部分，去除非数字字符
                    String docNumStr = file.getDocumentNum().replaceAll("[^0-9]", "");
                    if (!docNumStr.isEmpty()) {
                        totalDocumentNum += Long.parseLong(docNumStr);
                    }
                } catch (NumberFormatException e) {
                    // 忽略无法解析的切割数
                }
            }
        }

        // 统计上传用户数（去重）
        long uniqueUploaderCount = allFiles.stream()
                .map(TRagFile::getUploadedBy)
                .filter(uploadedBy -> uploadedBy != null && !uploadedBy.isEmpty())
                .distinct()
                .count();

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalFileCount", totalFileCount);
        result.put("totalFileSize", totalFileSize);
        result.put("totalDocumentNum", totalDocumentNum);
        result.put("uniqueUploaderCount", uniqueUploaderCount);

        return Result.success(result);
    }

    /**
     * 获取用户统计信息
     * 包括用户总数、性别分布、性别比例
     * 
     * @return 用户统计结果
     */
    @PostMapping("/userStats")
    public Result<Map<String, Object>> getUserStats() {
        // 获取所有用户数据
         List<TUser> allUsers = tUserService.list();

        // 用户总数量
        long totalUserCount = allUsers.size();

        // 按性别统计（1-男性，2-女性）
        long maleCount = allUsers.stream()
                .filter(user -> user.getGender() != null && user.getGender() == 1)
                .count();
        
        long femaleCount = allUsers.stream()
                .filter(user -> user.getGender() != null && user.getGender() == 2)
                .count();
        
        // 未知性别数量
        long unknownGenderCount = allUsers.stream()
                .filter(user -> user.getGender() == null || (user.getGender() != 1 && user.getGender() != 2))
                .count();

        // 计算性别比例（保留两位小数）
        double maleRatio = totalUserCount > 0 ? Math.round((double) maleCount / totalUserCount * 10000) / 100.0 : 0.0;
        double femaleRatio = totalUserCount > 0 ? Math.round((double) femaleCount / totalUserCount * 10000) / 100.0 : 0.0;
        double unknownRatio = totalUserCount > 0 ? Math.round((double) unknownGenderCount / totalUserCount * 10000) / 100.0 : 0.0;

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalUserCount", totalUserCount);
        result.put("maleCount", maleCount);
        result.put("femaleCount", femaleCount);
        result.put("unknownGenderCount", unknownGenderCount);
        result.put("maleRatio", maleRatio);
        result.put("femaleRatio", femaleRatio);
        result.put("unknownRatio", unknownRatio);

        return Result.success(result);
    }

}