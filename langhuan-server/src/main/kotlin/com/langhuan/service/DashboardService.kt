package com.langhuan.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.langhuan.model.domain.TApiLog
import com.langhuan.model.domain.TRagFile
import com.langhuan.utils.date.DateTimeUtils
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

@Service
class DashboardService(
    private val tChatFeedbackService: TChatFeedbackService,
    private val tPromptsService: TPromptsService,
    private val tRagFileGroupService: TRagFileGroupService,
    private val tRagFileService: TRagFileService,
    private val tUserService: TUserService,
    private val tApiLogService: TApiLogService,
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(DashboardService::class.java)
    }

    fun getPromptsStats(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        result["totalCount"] = tPromptsService.count()
        return result
    }

    fun getFileGroupStats(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        result["totalCount"] = tRagFileGroupService.count()
        return result
    }

    fun getFileStats(): Map<String, Any> {
        val allFiles = tRagFileService.list()
        val totalFileCount = allFiles.size.toLong()
        var totalFileSize = 0L
        for (file in allFiles) {
            if (!file.fileSize.isNullOrEmpty()) {
                try {
                    val sizeStr = file.fileSize!!.replace("[^0-9]".toRegex(), "")
                    if (sizeStr.isNotEmpty()) {
                        totalFileSize += sizeStr.toLong()
                    }
                } catch (e: NumberFormatException) {
                    // ignore
                }
            }
        }
        var totalDocumentNum = 0L
        for (file in allFiles) {
            if (!file.documentNum.isNullOrEmpty()) {
                try {
                    val docNumStr = file.documentNum!!.replace("[^0-9]".toRegex(), "")
                    if (docNumStr.isNotEmpty()) {
                        totalDocumentNum += docNumStr.toLong()
                    }
                } catch (e: NumberFormatException) {
                    // ignore
                }
            }
        }
        val uniqueUploaderCount = allFiles.stream()
            .map { it.uploadedBy }
            .filter { !it.isNullOrEmpty() }
            .distinct()
            .count()

        val result = mutableMapOf<String, Any>()
        result["totalFileCount"] = totalFileCount
        result["totalFileSize"] = totalFileSize
        result["totalDocumentNum"] = totalDocumentNum
        result["uniqueUploaderCount"] = uniqueUploaderCount
        return result
    }

    fun getUserStats(): Map<String, Any> {
        val allUsers = tUserService.list()
        val totalUserCount = allUsers.size.toLong()
        val maleCount = allUsers.stream()
            .filter { user -> user.gender != null && user.gender == 1 }
            .count()
        val femaleCount = allUsers.stream()
            .filter { user -> user.gender != null && user.gender == 2 }
            .count()
        val unknownGenderCount = allUsers.stream()
            .filter { user -> user.gender == null || (user.gender != 1 && user.gender != 2) }
            .count()

        val maleRatio = if (totalUserCount > 0) {
            Math.round(maleCount.toDouble() / totalUserCount * 10000) / 100.0
        } else 0.0
        val femaleRatio = if (totalUserCount > 0) {
            Math.round(femaleCount.toDouble() / totalUserCount * 10000) / 100.0
        } else 0.0
        val unknownRatio = if (totalUserCount > 0) {
            Math.round(unknownGenderCount.toDouble() / totalUserCount * 10000) / 100.0
        } else 0.0

        val result = mutableMapOf<String, Any>()
        result["totalUserCount"] = totalUserCount
        result["maleCount"] = maleCount
        result["femaleCount"] = femaleCount
        result["unknownGenderCount"] = unknownGenderCount
        result["maleRatio"] = maleRatio
        result["femaleRatio"] = femaleRatio
        result["unknownRatio"] = unknownRatio
        return result
    }

    /**
     * 获取使用情况统计
     * 包括总提问次数、本周各用户提问统计等
     *
     * @return 使用情况统计结果
     */
    fun getUsageStats(): Map<String, Any> {
        // 计算本周开始时间（周一00:00:00）
        val now = DateTimeUtils.now()
        val startOfWeek = now.truncatedTo(ChronoUnit.DAYS)
            .minusDays(now.dayOfWeek.value.toLong() - 1)

        // 统计总提问次数
        val totalQuestions = tApiLogService.count(
            QueryWrapper<TApiLog>()
                .eq("api_url", "/chat/chat")
        )

        // 查询本周的聊天记录
        val weekChatLogs = tApiLogService.list(
            QueryWrapper<TApiLog>()
                .eq("api_url", "/chat/chat")
                .ge("createTime", startOfWeek)
        )

        // 查询所有用户
        val allUsers = tUserService.list()
        val userMap = allUsers.stream()
            .collect(Collectors.toMap({ it.username }, { user -> user }))

        // 按用户统计
        val userStats = mutableListOf<Map<String, Any>>()

        // 按用户ID分组统计本周提问次数
        val weekQuestionsByUser = weekChatLogs.stream()
            .filter { log -> !log.userId.isNullOrEmpty() }
            .collect(Collectors.groupingBy({ it.userId }, Collectors.counting()))

        // 按用户ID分组统计点踩次数
        val dislikeCountSql = "SELECT user_id, COUNT(*) as count FROM t_chat_feedback " +
                "WHERE interaction = 'dislike' AND interaction_time >= ? " +
                "GROUP BY user_id"
        val dislikeResults = jdbcTemplate.queryForList(
            dislikeCountSql,
            java.util.Date.from(startOfWeek.atZone(java.time.ZoneId.systemDefault()).toInstant())
        )
        val dislikesByUser = dislikeResults.stream()
            .collect(
                Collectors.toMap(
                    { row -> row["user_id"] as String },
                    { row -> (row["count"] as Number).toLong() }
                ))

        // 按用户ID分组统计反馈总数
        val feedbackCountSql = "SELECT user_id, COUNT(*) as count FROM t_chat_feedback GROUP BY user_id"
        val feedbackResults = jdbcTemplate.queryForList(feedbackCountSql)
        val feedbacksByUser = feedbackResults.stream()
            .collect(
                Collectors.toMap(
                    { row -> row["user_id"] as String },
                    { row -> (row["count"] as Number).toLong() }
                ))

        // 查询本周的文件上传记录
        val weekStartDate = java.util.Date
            .from(startOfWeek.atZone(java.time.ZoneId.systemDefault()).toInstant())
        val weekFiles = tRagFileService.list(
            QueryWrapper<TRagFile>()
                .ge("uploaded_at", weekStartDate)
        )

        // 按用户统计本周文件上传数据
        val filesByUser = weekFiles.stream()
            .filter { file -> !file.uploadedBy.isNullOrEmpty() }
            .collect(Collectors.groupingBy({ it.uploadedBy }))

        // 为每个有提问记录的用户生成统计
        for ((userId, questionCount) in weekQuestionsByUser) {
            val dislikeCount = dislikesByUser.getOrDefault(userId, 0L)
            val validAnswerCount = questionCount - dislikeCount
            val feedbackCount = feedbacksByUser.getOrDefault(userId, 0L)

            val user = userMap[userId] // 使用userId作为username来查找用户
            val username = user?.username ?: userId // 如果找不到用户，直接使用userId
            val name = user?.name ?: userId // 如果找不到用户，直接使用userId
            val realUserId = user?.id // 获取真实的用户ID

            // 计算该用户本周的文件上传统计
            val userFiles = filesByUser.getOrDefault(userId, mutableListOf())
            val fileCount = userFiles.size
            var totalFileSize = 0L
            var totalDocumentNum = 0L

            for (file in userFiles) {
                // 处理文件大小
                if (!file.fileSize.isNullOrEmpty()) {
                    try {
                        val sizeStr = file.fileSize!!.replace("[^0-9]".toRegex(), "")
                        if (sizeStr.isNotEmpty()) {
                            totalFileSize += sizeStr.toLong()
                        }
                    } catch (e: NumberFormatException) {
                        // 忽略无法解析的文件大小
                    }
                }

                // 处理文件切片数
                if (!file.documentNum.isNullOrEmpty()) {
                    try {
                        val docNumStr = file.documentNum!!.replace("[^0-9]".toRegex(), "")
                        if (docNumStr.isNotEmpty()) {
                            totalDocumentNum += docNumStr.toLong()
                        }
                    } catch (e: NumberFormatException) {
                        // 忽略无法解析的切片数
                    }
                }
            }

            val userStat = mutableMapOf<String, Any>()
            userStat["userId"] = realUserId!! // 返回真实的用户ID
            userStat["username"] = username as String
            userStat["name"] = name as String
            userStat["questionCount"] = questionCount
            userStat["validAnswerCount"] = validAnswerCount
            userStat["invalidAnswerCount"] = dislikeCount
            userStat["feedbackCount"] = feedbackCount
            // 新增文件上传统计
            userStat["weekFileCount"] = fileCount
            userStat["weekFileSize"] = totalFileSize
            userStat["weekDocumentNum"] = totalDocumentNum
            userStats.add(userStat)
        }

        // 添加只上传文件但没有提问的用户
        for ((uploadedBy, userFiles) in filesByUser.entries) {
            // 检查该用户是否已经在统计中
            val userExists = userStats.stream()
                .anyMatch { stat -> uploadedBy == stat["username"] }

            if (!userExists) {
                val fileCount = userFiles.size
                var totalFileSize = 0L
                var totalDocumentNum = 0L

                for (file in userFiles) {
                    // 处理文件大小
                    if (!file.fileSize.isNullOrEmpty()) {
                        try {
                            val sizeStr = file.fileSize!!.replace("[^0-9]".toRegex(), "")
                            if (sizeStr.isNotEmpty()) {
                                totalFileSize += sizeStr.toLong()
                            }
                        } catch (e: NumberFormatException) {
                            // 忽略无法解析的文件大小
                        }
                    }

                    // 处理文件切片数
                    if (!file.documentNum.isNullOrEmpty()) {
                        try {
                            val docNumStr = file.documentNum!!.replace("[^0-9]".toRegex(), "")
                            if (docNumStr.isNotEmpty()) {
                                totalDocumentNum += docNumStr.toLong()
                            }
                        } catch (e: NumberFormatException) {
                            // 忽略无法解析的切片数
                        }
                    }
                }

                val user = userMap[uploadedBy]
                val realUserId = user?.id

                val userStat = mutableMapOf<String, Any>()
                userStat["userId"] = realUserId ?: 0
                userStat["username"] = uploadedBy as String
                userStat["questionCount"] = 0L
                userStat["validAnswerCount"] = 0L
                userStat["invalidAnswerCount"] = 0L
                userStat["feedbackCount"] = 0L
                userStat["weekFileCount"] = fileCount
                userStat["weekFileSize"] = totalFileSize
                userStat["weekDocumentNum"] = totalDocumentNum

                userStats.add(userStat)
            }
        }

        // 按提问次数倒序排列，如果提问次数相同则按文件数量倒序排列
        userStats.sortWith { a, b ->
            val questionCompare = java.lang.Long.compare(
                b["questionCount"] as Long,
                a["questionCount"] as Long
            )
            if (questionCompare != 0) {
                return@sortWith questionCompare
            }
            java.lang.Integer.compare(
                b["weekFileCount"] as Int,
                a["weekFileCount"] as Int
            )
        }

        // 计算本周总体统计
        val weekTotalQuestions = weekChatLogs.size

        // 查询本周的反馈记录
        val weekFeedbackCountSql = "SELECT COUNT(*) as count FROM t_chat_feedback " +
                "WHERE interaction_time >= ?"
        val weekTotalFeedbacks = jdbcTemplate.queryForObject(
            weekFeedbackCountSql, Long::class.java,
            java.util.Date.from(startOfWeek.atZone(java.time.ZoneId.systemDefault()).toInstant())
        )

        // 本周总点踩数
        val weekDislikeCountSql = "SELECT COUNT(*) as count FROM t_chat_feedback " +
                "WHERE interaction = 'dislike' AND interaction_time >= ?"
        val weekTotalDislikes = jdbcTemplate.queryForObject(
            weekDislikeCountSql, Long::class.java,
            java.util.Date.from(startOfWeek.atZone(java.time.ZoneId.systemDefault()).toInstant())
        )

        // 本周总有效回答数 = 本周总提问数 - 本周总点踩数
        val weekTotalValidAnswers = weekTotalQuestions - (weekTotalDislikes ?: 0L)

        // 按反馈类型统计反馈信息
        val feedbackTypeCountSql = "SELECT interaction, COUNT(*) as count FROM t_chat_feedback GROUP BY interaction"
        val feedbackTypeResults = jdbcTemplate.queryForList(feedbackTypeCountSql)
        val feedbackTypeStats = feedbackTypeResults.stream()
            .collect(
                Collectors.toMap(
                    { row -> row["interaction"] as String },
                    { row -> (row["count"] as Number).toLong() }
                ))
        // 总反馈信息数字统计
        val totalFeedbackCount = feedbackTypeStats.getOrDefault("like", 0L)
        +feedbackTypeStats.getOrDefault("dislike", 0L)
        +feedbackTypeStats.getOrDefault("end", 0L)

        // 总有效回答数
        val totalValidAnswers = totalQuestions - feedbackTypeStats.getOrDefault("dislike", 0L)
        -feedbackTypeStats.getOrDefault("end", 0L)

        val result = mutableMapOf<String, Any>()
        result["totalQuestions"] = totalQuestions
        result["weekStartTime"] = startOfWeek
        result["weekQuestions"] = weekTotalQuestions
        result["weekValidAnswers"] = weekTotalValidAnswers
        result["weekTotalFeedbacks"] = weekTotalFeedbacks ?: 0L
        result["userStats"] = userStats
        result["totalFeedbackCount"] = totalFeedbackCount
        result["feedbackTypeStats"] = feedbackTypeStats
        result["totalValidAnswers"] = totalValidAnswers
        return result
    }
}
