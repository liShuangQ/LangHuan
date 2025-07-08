package com.langhuan.service;

import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TUser;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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

    public DashboardService(TChatFeedbackService tChatFeedbackService, TPromptsService tPromptsService,
            TRagFileGroupService tRagFileGroupService, TRagFileService tRagFileService, TUserService tUserService) {
        this.tChatFeedbackService = tChatFeedbackService;
        this.tPromptsService = tPromptsService;
        this.tRagFileGroupService = tRagFileGroupService;
        this.tRagFileService = tRagFileService;
        this.tUserService = tUserService;
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
        double femaleRatio = totalUserCount > 0 ? Math.round((double) femaleCount / totalUserCount * 10000) / 100.0 : 0.0;
        double unknownRatio = totalUserCount > 0 ? Math.round((double) unknownGenderCount / totalUserCount * 10000) / 100.0 : 0.0;
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
}
