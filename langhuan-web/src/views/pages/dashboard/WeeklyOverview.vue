<script setup lang="ts">
import { computed } from 'vue'

interface UsageStatsData {
    totalQuestions: number
    weekStartTime: string
    weekQuestions: number
    weekValidAnswers: number
    weekTotalFeedbacks: number
    totalFeedbackCount: number
    totalValidAnswers: number
    userStats: any[]
    feedbackTypeStats: {
        "like": number,
        "dislike": number,
        "end": number
    }
}

interface Props {
    usageData: UsageStatsData
}

const props = defineProps<Props>()

// 活跃用户数
const activeUserCount = computed(() => {
    return props.usageData.userStats.length
})

// 本周文件统计汇总
const weekFileStats = computed(() => {
    const stats = props.usageData.userStats
    return {
        totalFiles: stats.reduce((sum, user) => sum + (user.weekFileCount || 0), 0),
        totalSize: stats.reduce((sum, user) => sum + (user.weekFileSize || 0), 0),
        totalDocuments: stats.reduce((sum, user) => sum + (user.weekDocumentNum || 0), 0)
    }
})

// 格式化文件大小
const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<template>
    <!-- 本周整体有效率和统计概览 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <!-- 统计概览 -->
        <div class="stats-card bg-white rounded-lg shadow-md p-6">
            <h3 class="text-xl font-semibold text-gray-700 mb-4 flex items-center">
                <el-icon class="text-blue-500 mr-2">
                    <DataAnalysis />
                </el-icon>
                统计概览
            </h3>
            <div class="space-y-3">
                <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span class="text-gray-600">平均每用户提问</span>
                    <span class="font-bold text-blue-600">
                        {{ activeUserCount > 0 ? Math.round(usageData.weekQuestions / activeUserCount) : 0 }} 次
                    </span>
                </div>
                <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span class="text-gray-600">平均每用户反馈</span>
                    <span class="font-bold text-green-600">
                        {{ activeUserCount > 0 ? Math.round(usageData.weekTotalFeedbacks / activeUserCount) : 0 }} 次
                    </span>
                </div>
                <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span class="text-gray-600">反馈率</span>
                    <span class="font-bold text-purple-600">
                        {{ usageData.weekQuestions > 0 ? Math.round((usageData.weekTotalFeedbacks / usageData.weekQuestions) * 100) : 0 }}%
                    </span>
                </div>
                <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span class="text-gray-600">本周上传文件</span>
                    <span class="font-bold text-indigo-600">
                        {{ weekFileStats.totalFiles }} 个
                    </span>
                </div>
                <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span class="text-gray-600">本周文件总大小</span>
                    <span class="font-bold text-cyan-600">
                        {{ formatFileSize(weekFileStats.totalSize) }}
                    </span>
                </div>
                <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span class="text-gray-600">本周文档切片</span>
                    <span class="font-bold text-teal-600">
                        {{ weekFileStats.totalDocuments }} 个
                    </span>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.stats-card {
    transition: all 0.3s ease;
    cursor: pointer;
}

.stats-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}
</style>