// Dashboard 相关类型定义
export interface UserStat {
    userId: number
    name: string
    username: string
    questionCount: number
    validAnswerCount: number
    invalidAnswerCount: number
    feedbackCount: number
    weekFileCount: number
    weekFileSize: number
    weekDocumentNum: number
}

export interface UsageStatsData {
    totalQuestions: number
    weekStartTime: string
    weekQuestions: number
    weekValidAnswers: number
    weekTotalFeedbacks: number
    totalFeedbackCount: number
    totalValidAnswers: number
    userStats: UserStat[]
    feedbackTypeStats: {
        "like": number,
        "dislike": number,
        "end": number
    }
}

export interface FileStats {
    totalFileCount: number
    totalFileSize: number
    totalDocumentNum: number
    uniqueUploaderCount: number
}

export interface FileGroupStats {
    totalCount: number
}

export interface WeekFileStats {
    totalFiles: number
    totalSize: number
    totalDocuments: number
}