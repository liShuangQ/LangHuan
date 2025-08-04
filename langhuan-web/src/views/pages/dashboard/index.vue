<script lang="ts">
export default {
    auth: true,
}
</script>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { http } from '@/plugins/axios'
import { ElMessage } from 'element-plus'
import DashboardHeader from './DashboardHeader.vue'
import HistoryStatsCards from './HistoryStatsCards.vue'
import FileStatsCard from './FileStatsCard.vue'
import WeeklyStatsCards from './WeeklyStatsCards.vue'
import WeeklyOverview from './WeeklyOverview.vue'
import UserStatsTable from './UserStatsTable.vue'

// 定义接口数据类型
interface UserStat {
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

interface UsageStatsData {
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

// 定义响应式数据
const loading = ref(false)
const promptsStats = ref<any>({})
const fileGroupStats = ref<any>({})
const fileStats = ref<any>({})
const userStats = ref<any>({})

// 使用情况统计数据
const usageData = ref<UsageStatsData>({
    totalQuestions: 0,
    weekStartTime: '',
    weekQuestions: 0,
    weekValidAnswers: 0,
    weekTotalFeedbacks: 0,
    totalValidAnswers: 0,
    totalFeedbackCount: 0,
    feedbackTypeStats: {
        "like": 0,
        "dislike": 0,
        "end": 0
    },
    userStats: []
})



// 获取提示词统计数据
const getPromptsStats = async () => {
    try {
        const res = await http.request<any>({
            url: '/dashboard/promptsStats',
            method: 'post',
            q_spinning: false,
            q_contentType: 'json'
        })
        if (res.code === 200) {
            promptsStats.value = res.data
        }
    } catch (error) {
        console.error('获取提示词统计失败:', error)
    }
}


// 获取文件组统计数据
const getFileGroupStats = async () => {
    try {
        const res = await http.request<any>({
            url: '/dashboard/fileGroupStats',
            method: 'post',
            q_spinning: false,
            q_contentType: 'json'
        })
        if (res.code === 200) {
            fileGroupStats.value = res.data
        }
    } catch (error) {
        console.error('获取文件组统计失败:', error)
    }
}

// 获取文件统计数据
const getFileStats = async () => {
    try {
        const res = await http.request<any>({
            url: '/dashboard/fileStats',
            method: 'post',
            q_spinning: false,
            q_contentType: 'json'
        })
        if (res.code === 200) {
            fileStats.value = res.data
        }
    } catch (error) {
        console.error('获取文件统计失败:', error)
    }
}

// 获取用户统计数据
const getUserStats = async () => {
    try {
        const res = await http.request<any>({
            url: '/dashboard/userStats',
            method: 'post',
            q_spinning: false,
            q_contentType: 'json'
        })
        if (res.code === 200) {
            userStats.value = res.data
        }
    } catch (error) {
        console.error('获取用户统计失败:', error)
    }
}

// 获取使用情况统计数据
const getUsageStats = async () => {
    try {
        const res = await http.request<any>({
            url: '/dashboard/usageStats',
            method: 'post',
            q_spinning: false,
            q_contentType: 'json'
        })
        if (res.code === 200) {
            usageData.value = res.data
        }
    } catch (error) {
        console.error('获取使用统计数据失败:', error)
    }
}

// 加载所有统计数据
const loadAllStats = async () => {
    loading.value = true
    try {
        await Promise.all([
            getPromptsStats(),
            getFileGroupStats(),
            getFileStats(),
            // getUserStats(),
            getUsageStats()
        ])
    } catch (error) {
        ElMessage.error('加载统计数据失败')
    } finally {
        loading.value = false
    }
}

// 格式化周开始时间
const weekStartTimeFormatted = computed(() => {
    if (!usageData.value.weekStartTime) return ''
    return new Date(usageData.value.weekStartTime).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
})

// 刷新数据
const refreshData = () => {
    loadAllStats()
    ElMessage.success('数据已刷新')
}

// 组件挂载时加载数据
onMounted(() => {
    loadAllStats()
})
</script>

<template>
    <div class="dashboard-container p-6 bg-gray-50 min-h-screen">
        <!-- 页面标题 -->
        <DashboardHeader :loading="loading" @refresh="refreshData" />


        <!-- 使用情况统计-总体情况 -->
        <div>
            <div class="mb-6">
                <h2 class="text-2xl font-bold text-gray-800 mb-2">使用情况统计</h2>
            </div>
            <!-- 历史统计卡片 -->
            <HistoryStatsCards :usage-data="usageData" :file-group-stats="fileGroupStats" />

            <!-- 文件详细统计卡片 -->
            <FileStatsCard :file-stats="fileStats" />
        </div>

        <!-- 使用情况统计-本周情况 -->
        <div class="mt-8 mb-8">
            <!-- 使用情况统计标题 -->
            <div class="mb-6">
                <h2 class="text-2xl font-bold text-gray-800 mb-2">本周使用情况统计</h2>
                            <p v-if="weekStartTimeFormatted" class="text-sm text-gray-500 mt-1">
                统计周期：{{ weekStartTimeFormatted }} 开始的本周
            </p>
            </div>

            <!-- 本周统计卡片 -->
            <WeeklyStatsCards :usage-data="usageData" />

            <!-- 本周统计概览 -->
            <WeeklyOverview :usage-data="usageData" />

            <!-- 用户统计表格 -->
            <UserStatsTable :user-stats="usageData.userStats" :loading="loading" />
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg p-6 flex items-center space-x-3">
                <el-icon class="is-loading text-blue-500">
                    <Loading />
                </el-icon>
                <span class="text-gray-700">正在加载数据...</span>
            </div>
        </div>
    </div>
</template>

<style scoped>
.dashboard-container {
    min-height: calc(100vh - 60px);
}

.stats-card {
    transition: all 0.3s ease;
    cursor: pointer;
}

.stats-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.refresh-btn {
    transition: all 0.3s ease;
}

.refresh-btn:hover {
    transform: scale(1.05);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .dashboard-container {
        padding: 1rem;
    }

    .grid {
        grid-template-columns: 1fr;
    }
}

/* 表格样式 */
table {
    border-collapse: collapse;
}

th,
td {
    border-bottom: 1px solid #e5e7eb;
}

tr:last-child td {
    border-bottom: none;
}

/* 表格滚动区域样式 */
.max-h-96 {
    max-height: 24rem;
    /* 384px */
}

/* 固定表头样式 */
thead.sticky {
    position: sticky;
    top: 0;
    z-index: 10;
}

thead.sticky th {
    background-color: #f9fafb;
    box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

/* 自定义滚动条 */
::-webkit-scrollbar {
    width: 6px;
    height: 6px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* 响应式表格 */
@media (max-width: 768px) {
    .overflow-x-auto {
        -webkit-overflow-scrolling: touch;
    }
}
</style>