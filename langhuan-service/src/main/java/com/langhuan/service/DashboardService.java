package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.langhuan.model.domain.TApiLog;
import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TUser;
import com.langhuan.utils.date.DateTimeUtils;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardService {

    private final TChatFeedbackService tChatFeedbackService;
    private final TPromptsService tPromptsService;
    private final TRagFileGroupService tRagFileGroupService;
    private final TRagFileService tRagFileService;
    private final TUserService tUserService;
    private final TApiLogService tApiLogService;

    public DashboardService(TChatFeedbackService tChatFeedbackService, TPromptsService tPromptsService,
            TRagFileGroupService tRagFileGroupService, TRagFileService tRagFileService, TUserService tUserService,
            TApiLogService tApiLogService) {
        this.tChatFeedbackService = tChatFeedbackService;
        this.tPromptsService = tPromptsService;
        this.tRagFileGroupService = tRagFileGroupService;
        this.tRagFileService = tRagFileService;
        this.tUserService = tUserService;
        this.tApiLogService = tApiLogService;
    }

    public Map<String, Object> getFeedbackStats() {
        List<TChatFeedback> allFeedbacks = tChatFeedbackService.list();
        long totalCount = allFeedbacks.size();
        Map<String, Long> typeStats = allFeedbacks.stream()
                .collect(Collectors.groupingBy(
                        feedback -> feedback.getInteraction() != null ? feedback.getInteraction() : "unknown",
                        Collectors.counting()));
        Map<String, Long> userStats = allFeedbacks.stream()
                .collect(Collectors.groupingBy(
                        feedback -> feedback.getUserId() != null ? feedback.getUserId() : "unknown",
                        Collectors.counting()));
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("typeStats", typeStats);
        result.put("userStats", userStats);
        result.put("likeCount", typeStats.getOrDefault("like", 0L));
        result.put("dislikeCount", typeStats.getOrDefault("dislike", 0L));
        result.put("endCount", typeStats.getOrDefault("end", 0L));
        return result;
    }

    public Map<String, Object> getPromptsStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", tPromptsService.count());
        return result;
    }

    public Map<String, Object> getFileGroupStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", tRagFileGroupService.count());
        return result;
    }

    public Map<String, Object> getFileStats() {
        List<TRagFile> allFiles = tRagFileService.list();
        long totalFileCount = allFiles.size();
        long totalFileSize = 0;
        for (TRagFile file : allFiles) {
            if (file.getFileSize() != null && !file.getFileSize().isEmpty()) {
                try {
                    String sizeStr = file.getFileSize().replaceAll("[^0-9]", "");
                    if (!sizeStr.isEmpty()) {
                        totalFileSize += Long.parseLong(sizeStr);
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        long totalDocumentNum = 0;
        for (TRagFile file : allFiles) {
            if (file.getDocumentNum() != null && !file.getDocumentNum().isEmpty()) {
                try {
                    String docNumStr = file.getDocumentNum().replaceAll("[^0-9]", "");
                    if (!docNumStr.isEmpty()) {
                        totalDocumentNum += Long.parseLong(docNumStr);
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        long uniqueUploaderCount = allFiles.stream()
                .map(TRagFile::getUploadedBy)
                .filter(uploadedBy -> uploadedBy != null && !uploadedBy.isEmpty())
                .distinct()
                .count();
        Map<String, Object> result = new HashMap<>();
        result.put("totalFileCount", totalFileCount);
        result.put("totalFileSize", totalFileSize);
        result.put("totalDocumentNum", totalDocumentNum);
        result.put("uniqueUploaderCount", uniqueUploaderCount);
        return result;
    }

    public Map<String, Object> getUserStats() {
        List<TUser> allUsers = tUserService.list();
        long totalUserCount = allUsers.size();
        long maleCount = allUsers.stream()
                .filter(user -> user.getGender() != null && user.getGender() == 1)
                .count();
        long femaleCount = allUsers.stream()
                .filter(user -> user.getGender() != null && user.getGender() == 2)
                .count();
        long unknownGenderCount = allUsers.stream()
                .filter(user -> user.getGender() == null || (user.getGender() != 1 && user.getGender() != 2))
                .count();
        double maleRatio = totalUserCount > 0 ? Math.round((double) maleCount / totalUserCount * 10000) / 100.0 : 0.0;
        double femaleRatio = totalUserCount > 0 ? Math.round((double) femaleCount / totalUserCount * 10000) / 100.0
                : 0.0;
        double unknownRatio = totalUserCount > 0
                ? Math.round((double) unknownGenderCount / totalUserCount * 10000) / 100.0
                : 0.0;
        Map<String, Object> result = new HashMap<>();
        result.put("totalUserCount", totalUserCount);
        result.put("maleCount", maleCount);
        result.put("femaleCount", femaleCount);
        result.put("unknownGenderCount", unknownGenderCount);
        result.put("maleRatio", maleRatio);
        result.put("femaleRatio", femaleRatio);
        result.put("unknownRatio", unknownRatio);
        return result;
    }

    /**
     * 获取使用情况统计
     * 包括总提问次数、本周各用户提问统计等
     * 
     * @return 使用情况统计结果
     */
    public Map<String, Object> getUsageStats() {
        // 计算本周开始时间（周一00:00:00）
        LocalDateTime now = DateTimeUtils.now();
        LocalDateTime startOfWeek = now.truncatedTo(ChronoUnit.DAYS)
                .minusDays(now.getDayOfWeek().getValue() - 1);

        // 查询所有聊天接口调用记录
        List<TApiLog> chatLogs = tApiLogService.list(
                new LambdaQueryWrapper<TApiLog>()
                        .eq(TApiLog::getApiUrl, "/chat/chat"));

        // 统计总提问次数
        long totalQuestions = chatLogs.size();

        // 查询本周的聊天记录
        List<TApiLog> weekChatLogs = tApiLogService.list(
                new LambdaQueryWrapper<TApiLog>()
                        .eq(TApiLog::getApiUrl, "/chat/chat")
                        .ge(TApiLog::getCreateTime, startOfWeek));

        // 查询所有用户
        List<TUser> allUsers = tUserService.list();
        Map<String, TUser> userMap = allUsers.stream()
                .collect(Collectors.toMap(TUser::getUsername, user -> user));

        // 查询所有反馈记录
        List<TChatFeedback> allFeedbacks = tChatFeedbackService.list();

        // 按用户统计
        List<Map<String, Object>> userStats = new ArrayList<>();

        // 按用户ID分组统计本周提问次数
        Map<String, Long> weekQuestionsByUser = weekChatLogs.stream()
                .filter(log -> log.getUserId() != null && !log.getUserId().isEmpty())
                .collect(Collectors.groupingBy(TApiLog::getUserId, Collectors.counting()));

        // 按用户ID分组统计点踩次数
        Map<String, Long> dislikesByUser = allFeedbacks.stream()
                .filter(feedback -> "dislike".equals(feedback.getInteraction()))
                .collect(Collectors.groupingBy(TChatFeedback::getUserId, Collectors.counting()));

        // 按用户ID分组统计反馈总数
        Map<String, Long> feedbacksByUser = allFeedbacks.stream()
                .collect(Collectors.groupingBy(TChatFeedback::getUserId, Collectors.counting()));

        // 查询本周的文件上传记录
        java.util.Date weekStartDate = java.util.Date
                .from(startOfWeek.atZone(java.time.ZoneId.systemDefault()).toInstant());
        List<TRagFile> weekFiles = tRagFileService.list(
                new LambdaQueryWrapper<TRagFile>()
                        .ge(TRagFile::getUploadedAt, weekStartDate));

        // 按用户统计本周文件上传数据
        Map<String, List<TRagFile>> filesByUser = weekFiles.stream()
                .filter(file -> file.getUploadedBy() != null && !file.getUploadedBy().isEmpty())
                .collect(Collectors.groupingBy(TRagFile::getUploadedBy));

        // 为每个有提问记录的用户生成统计
        for (Map.Entry<String, Long> entry : weekQuestionsByUser.entrySet()) {
            String userId = entry.getKey(); // 这里的userId实际上对应用户表的username
            long questionCount = entry.getValue();
            long dislikeCount = dislikesByUser.getOrDefault(userId, 0L);
            long validAnswerCount = questionCount - dislikeCount;
            long feedbackCount = feedbacksByUser.getOrDefault(userId, 0L);

            TUser user = userMap.get(userId); // 使用userId作为username来查找用户
            String username = user != null ? user.getUsername() : userId; // 如果找不到用户，直接使用userId
            String name = user != null ? user.getName() : userId; // 如果找不到用户，直接使用userId
            Integer realUserId = user != null ? user.getId() : null; // 获取真实的用户ID

            // 计算该用户本周的文件上传统计
            List<TRagFile> userFiles = filesByUser.getOrDefault(userId, new ArrayList<>());
            int fileCount = userFiles.size();
            long totalFileSize = 0;
            long totalDocumentNum = 0;

            for (TRagFile file : userFiles) {
                // 处理文件大小
                if (file.getFileSize() != null && !file.getFileSize().isEmpty()) {
                    try {
                        String sizeStr = file.getFileSize().replaceAll("[^0-9]", "");
                        if (!sizeStr.isEmpty()) {
                            totalFileSize += Long.parseLong(sizeStr);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无法解析的文件大小
                    }
                }

                // 处理文件切片数
                if (file.getDocumentNum() != null && !file.getDocumentNum().isEmpty()) {
                    try {
                        String docNumStr = file.getDocumentNum().replaceAll("[^0-9]", "");
                        if (!docNumStr.isEmpty()) {
                            totalDocumentNum += Long.parseLong(docNumStr);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无法解析的切片数
                    }
                }
            }

            Map<String, Object> userStat = new HashMap<>();
            userStat.put("userId", realUserId); // 返回真实的用户ID
            userStat.put("username", username);
            userStat.put("name", name);
            userStat.put("questionCount", questionCount);
            userStat.put("validAnswerCount", validAnswerCount);
            userStat.put("invalidAnswerCount", dislikeCount);
            userStat.put("feedbackCount", feedbackCount);
            // 新增文件上传统计
            userStat.put("weekFileCount", fileCount);
            userStat.put("weekFileSize", totalFileSize);
            userStat.put("weekDocumentNum", totalDocumentNum);
            userStats.add(userStat);
        }

        // 添加只上传文件但没有提问的用户
        for (Map.Entry<String, List<TRagFile>> entry : filesByUser.entrySet()) {
            String uploadedBy = entry.getKey();

            // 检查该用户是否已经在统计中
            boolean userExists = userStats.stream()
                    .anyMatch(stat -> uploadedBy.equals(stat.get("username")));

            if (!userExists) {
                List<TRagFile> userFiles = entry.getValue();
                int fileCount = userFiles.size();
                long totalFileSize = 0;
                long totalDocumentNum = 0;

                for (TRagFile file : userFiles) {
                    // 处理文件大小
                    if (file.getFileSize() != null && !file.getFileSize().isEmpty()) {
                        try {
                            String sizeStr = file.getFileSize().replaceAll("[^0-9]", "");
                            if (!sizeStr.isEmpty()) {
                                totalFileSize += Long.parseLong(sizeStr);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略无法解析的文件大小
                        }
                    }

                    // 处理文件切片数
                    if (file.getDocumentNum() != null && !file.getDocumentNum().isEmpty()) {
                        try {
                            String docNumStr = file.getDocumentNum().replaceAll("[^0-9]", "");
                            if (!docNumStr.isEmpty()) {
                                totalDocumentNum += Long.parseLong(docNumStr);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略无法解析的切片数
                        }
                    }
                }

                TUser user = userMap.get(uploadedBy);
                Integer realUserId = user != null ? user.getId() : null;

                Map<String, Object> userStat = new HashMap<>();
                userStat.put("userId", realUserId);
                userStat.put("username", uploadedBy);
                userStat.put("questionCount", 0L);
                userStat.put("validAnswerCount", 0L);
                userStat.put("invalidAnswerCount", 0L);
                userStat.put("feedbackCount", 0L);
                userStat.put("weekFileCount", fileCount);
                userStat.put("weekFileSize", totalFileSize);
                userStat.put("weekDocumentNum", totalDocumentNum);

                userStats.add(userStat);
            }
        }

        // 按提问次数倒序排列，如果提问次数相同则按文件数量倒序排列
        userStats.sort((a, b) -> {
            int questionCompare = Long.compare(
                    (Long) b.get("questionCount"),
                    (Long) a.get("questionCount"));
            if (questionCompare != 0) {
                return questionCompare;
            }
            return Integer.compare(
                    (Integer) b.get("weekFileCount"),
                    (Integer) a.get("weekFileCount"));
        });

        // 计算本周总体统计
        long weekTotalQuestions = weekChatLogs.size();

        // 查询本周的反馈记录
        List<TChatFeedback> weekFeedbacks = allFeedbacks.stream()
                .filter(feedback -> feedback.getInteractionTime() != null)
                .filter(feedback -> {
                    // 将Date转换为LocalDateTime进行比较
                    java.time.LocalDateTime feedbackTime = feedback.getInteractionTime()
                            .toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();
                    return feedbackTime.isAfter(startOfWeek) || feedbackTime.isEqual(startOfWeek);
                })
                .collect(Collectors.toList());

        // 本周总点踩数
        long weekTotalDislikes = weekFeedbacks.stream()
                .filter(feedback -> "dislike".equals(feedback.getInteraction()))
                .count();

        // 本周总有效回答数 = 本周总提问数 - 本周总点踩数
        long weekTotalValidAnswers = weekTotalQuestions - weekTotalDislikes;

        // 本周总问题反馈数
        long weekTotalFeedbacks = weekFeedbacks.size();

        Map<String, Object> result = new HashMap<>();
        result.put("totalQuestions", totalQuestions);
        result.put("weekStartTime", startOfWeek);
        result.put("weekQuestions", weekTotalQuestions);
        result.put("weekValidAnswers", weekTotalValidAnswers);
        result.put("weekTotalFeedbacks", weekTotalFeedbacks);
        result.put("userStats", userStats);

        return result;
    }
}
