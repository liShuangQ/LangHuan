<script lang="ts">
export default {
    auth: true,
}
</script>
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { http } from '@/plugins/axios'
import { ElMessage } from 'element-plus'

// 定义接口数据类型
interface UserStat {
    userId: number
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
    userStats: UserStat[]
}

// 定义响应式数据
const loading = ref(false)
const feedbackStats = ref<any>({})
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
    userStats: []
})

// 基础项目名称
const BASE_PROJECT_NAME = computed(() => {
    return process.env.BASE_PROJECT_NAME as string
})

// 使用情况统计计算属性
const weekEffectiveRate = computed(() => {
    if (usageData.value.weekQuestions === 0) return 0
    return Math.round((usageData.value.weekValidAnswers / usageData.value.weekQuestions) * 100)
})

const activeUserCount = computed(() => {
    return usageData.value.userStats.length
})



// 本周文件统计汇总
const weekFileStats = computed(() => {
    const stats = usageData.value.userStats
    return {
        totalFiles: stats.reduce((sum, user) => sum + (user.weekFileCount || 0), 0),
        totalSize: stats.reduce((sum, user) => sum + (user.weekFileSize || 0), 0),
        totalDocuments: stats.reduce((sum, user) => sum + (user.weekDocumentNum || 0), 0)
    }
})

const weekStartTimeFormatted = computed(() => {
    if (!usageData.value.weekStartTime) return ''
    return new Date(usageData.value.weekStartTime).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
})
// 获取反馈统计数据
const getFeedbackStats = async () => {
    try {
        const res = await http.request<any>({
            url: '/dashboard/feedbackStats',
            method: 'post',
            q_spinning: false,
            q_contentType: 'json'
        })
        if (res.code === 200) {
            feedbackStats.value = res.data
        }
    } catch (error) {
        console.error('获取反馈统计失败:', error)
    }
}

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
// 按提问次数排序的用户统计
const sortedUserStats = computed(() => {
    return [...usageData.value.userStats].sort((a, b) => b.questionCount - a.questionCount)
})

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
            getFeedbackStats(),
            getPromptsStats(),
            getFileGroupStats(),
            getFileStats(),
            getUserStats(),
            getUsageStats()
        ])
    } catch (error) {
        ElMessage.error('加载统计数据失败')
    } finally {
        loading.value = false
    }
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 计算用户有效率
const calculateEffectiveRate = (user: UserStat) => {
    if (user.questionCount === 0) return 0
    return Math.round((user.validAnswerCount / user.questionCount) * 100)
}

// 获取有效率标签类型
const getEffectiveRateType = (rate: number) => {
    if (rate >= 90) return 'success'
    if (rate >= 70) return 'warning'
    return 'danger'
}

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
        <div class="flex justify-between items-center mb-6">
            <div>
                <h1 class="text-3xl font-bold text-gray-800 mb-2">{{ BASE_PROJECT_NAME }}系统仪表盘</h1>
                <!-- <p class="text-gray-600">系统运行状态和数据统计概览</p> -->
            </div>
            <el-button :loading="loading" @click="refreshData" class="refresh-btn">
                <el-icon>
                    <Refresh />
                </el-icon>
            </el-button>
        </div>

        <!-- 使用情况统计区域 -->
        <div class="mt-8 mb-8">
            <!-- 使用情况统计标题 -->
            <div class="mb-6">
                <h2 class="text-2xl font-bold text-gray-800 mb-2">使用情况统计</h2>
                <p v-if="weekStartTimeFormatted" class="text-sm text-gray-500 mt-1">
                    统计周期：{{ weekStartTimeFormatted }} 开始的本周
                </p>
            </div>

            <!-- 使用情况统计卡片 -->
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6 mb-8">
                <!-- 历史总提问卡片 -->
                <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-blue-500">
                    <div class="flex items-center justify-between">
                        <div>
                            <h3 class="text-lg font-semibold text-gray-700 mb-2">历史总提问</h3>
                            <p class="text-3xl font-bold text-blue-600">{{ usageData.totalQuestions.toLocaleString() }}
                            </p>
                            <p class="text-sm text-gray-500 mt-1">累计提问次数</p>
                        </div>
                        <div class="text-blue-500">
                            <el-icon size="32">
                                <ChatDotRound />
                            </el-icon>
                        </div>
                    </div>
                </div>

                <!-- 本周提问卡片 -->
                <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-green-500">
                    <div class="flex items-center justify-between">
                        <div>
                            <h3 class="text-lg font-semibold text-gray-700 mb-2">本周提问</h3>
                            <p class="text-3xl font-bold text-green-600">{{ usageData.weekQuestions }}</p>
                            <p class="text-sm text-gray-500 mt-1">本周提问次数</p>
                        </div>
                        <div class="text-green-500">
                            <el-icon size="32">
                                <ChatLineRound />
                            </el-icon>
                        </div>
                    </div>
                </div>

                <!-- 本周有效回答卡片 -->
                <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-purple-500">
                    <div class="flex items-center justify-between">
                        <div>
                            <h3 class="text-lg font-semibold text-gray-700 mb-2">本周有效回答</h3>
                            <p class="text-3xl font-bold text-purple-600">{{ usageData.weekValidAnswers }}</p>
                            <p class="text-sm text-gray-500 mt-1">有效回答数</p>
                        </div>
                        <div class="text-purple-500">
                            <el-icon size="32"><Select /></el-icon>
                        </div>
                    </div>
                </div>

                <!-- 本周问题反馈卡片 -->
                <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-orange-500">
                    <div class="flex items-center justify-between">
                        <div>
                            <h3 class="text-lg font-semibold text-gray-700 mb-2">本周问题反馈</h3>
                            <p class="text-3xl font-bold text-orange-600">{{ usageData.weekTotalFeedbacks }}</p>
                            <p class="text-sm text-gray-500 mt-1">反馈总数</p>
                        </div>
                        <div class="text-orange-500">
                            <el-icon size="32">
                                <MessageBox />
                            </el-icon>
                        </div>
                    </div>
                </div>

                <!-- 活跃用户卡片 -->
                <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-pink-500">
                    <div class="flex items-center justify-between">
                        <div>
                            <h3 class="text-lg font-semibold text-gray-700 mb-2">活跃用户</h3>
                            <p class="text-3xl font-bold text-pink-600">{{ activeUserCount }}</p>
                            <p class="text-sm text-gray-500 mt-1">本周活跃用户</p>
                        </div>
                        <div class="text-pink-500">
                            <el-icon size="32">
                                <User />
                            </el-icon>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 本周整体有效率和统计概览 -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                <div class="stats-card bg-white rounded-lg shadow-md p-6">
                    <h3 class="text-xl font-semibold text-gray-700 mb-4 flex items-center">
                        <el-icon class="text-indigo-500 mr-2">
                            <TrendCharts />
                        </el-icon>
                        本周整体有效率
                    </h3>
                    <div class="text-center h-full flex flex-col items-center justify-center pb-20">
                        <div class="text-6xl font-bold mb-2" :class="{
                            'text-green-500': weekEffectiveRate >= 90,
                            'text-yellow-500': weekEffectiveRate >= 70 && weekEffectiveRate < 90,
                            'text-red-500': weekEffectiveRate < 70
                        }">
                            {{ weekEffectiveRate }}%
                        </div>
                        <p class="text-gray-600">
                            有效回答 {{ usageData.weekValidAnswers }} / 总提问 {{ usageData.weekQuestions }}
                        </p>
                    </div>
                </div>

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
                                {{ activeUserCount > 0 ? Math.round(usageData.weekTotalFeedbacks / activeUserCount) : 0
                                }} 次
                            </span>
                        </div>
                                                 <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                             <span class="text-gray-600">反馈率</span>
                             <span class="font-bold text-purple-600">
                                 {{ usageData.weekQuestions > 0 ? Math.round((usageData.weekTotalFeedbacks /
                                 usageData.weekQuestions) * 100) : 0 }}%
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

            <!-- 用户统计表格 -->
            <div class="bg-white rounded-lg shadow-md">
                <div class="p-6 border-b border-gray-200">
                    <div class="flex justify-between items-center">
                        <h3 class="text-xl font-semibold text-gray-700 flex items-center">
                            <el-icon class="text-indigo-500 mr-2">
                                <UserFilled />
                            </el-icon>
                            本周用户提问统计
                        </h3>
                                                 <span class="text-sm text-gray-500">
                             共 {{ sortedUserStats.length }} 位活跃用户
                         </span>
                    </div>
                </div>

                <div class="p-6">
                                         <div v-if="sortedUserStats.length === 0" class="text-center py-12">
                        <el-icon size="48" class="text-gray-400 mb-4">
                            <User />
                        </el-icon>
                        <p class="text-gray-500">暂无用户数据</p>
                    </div>

                                                              <div v-else class="overflow-x-auto max-h-96 overflow-y-auto border border-gray-200 rounded-lg">
                         <table class="w-full table-auto">
                                                          <thead class="sticky top-0 z-10">
                                 <tr class="bg-gray-50">
                                     <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700 border-b">排名</th>
                                     <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700 border-b">用户名
                                     </th>
                                     <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">提问次数
                                     </th>
                                     <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">有效回答数
                                     </th>
                                     <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">无效回答数
                                     </th>
                                     <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">反馈次数
                                     </th>
                                     <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">有效率
                                     </th>
                                     <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">文件数量
                                     </th>
                                     <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">文件大小
                                     </th>
                                     <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">切片数量
                                     </th>
                                 </tr>
                             </thead>
                            <tbody>
                                                                 <tr v-for="(user, index) in sortedUserStats" :key="user.userId"
                                     class="hover:bg-gray-50 transition-colors duration-200"
                                     :class="{ 'bg-blue-50': index < 3 }">
                                    <td class="px-4 py-3 border-b">
                                        <div class="flex items-center">
                                            <span
                                                class="inline-flex items-center justify-center w-6 h-6 rounded-full text-sm font-bold"
                                                :class="{
                                                    'bg-yellow-400 text-white': index === 0,
                                                    'bg-gray-300 text-white': index === 1,
                                                    'bg-orange-400 text-white': index === 2,
                                                    'bg-gray-100 text-gray-600': index > 2
                                                }">
                                                {{ index + 1 }}
                                            </span>
                                        </div>
                                    </td>
                                    <td class="px-4 py-3 border-b">
                                        <div class="flex items-center">
                                            <el-icon class="text-gray-400 mr-2">
                                                <User />
                                            </el-icon>
                                            <span class="font-medium text-gray-900">{{ user.username }}</span>
                                        </div>
                                    </td>
                                    <td class="px-4 py-3 text-center border-b">
                                        <span class="font-semibold text-blue-600">{{ user.questionCount }}</span>
                                    </td>
                                    <td class="px-4 py-3 text-center border-b">
                                        <span class="font-semibold text-green-600">{{ user.validAnswerCount }}</span>
                                    </td>
                                    <td class="px-4 py-3 text-center border-b">
                                        <span class="font-semibold text-red-600">{{ user.invalidAnswerCount }}</span>
                                    </td>
                                    <td class="px-4 py-3 text-center border-b">
                                        <span class="font-semibold text-purple-600">{{ user.feedbackCount }}</span>
                                    </td>
                                                                         <td class="px-4 py-3 text-center border-b">
                                         <el-tag :type="getEffectiveRateType(calculateEffectiveRate(user))" size="small"
                                             class="font-semibold">
                                             {{ calculateEffectiveRate(user) }}%
                                         </el-tag>
                                     </td>
                                     <td class="px-4 py-3 text-center border-b">
                                         <span class="font-semibold text-indigo-600">{{ user.weekFileCount || 0 }}</span>
                                     </td>
                                     <td class="px-4 py-3 text-center border-b">
                                         <span class="font-semibold text-cyan-600">{{ formatFileSize(user.weekFileSize || 0) }}</span>
                                     </td>
                                     <td class="px-4 py-3 text-center border-b">
                                         <span class="font-semibold text-teal-600">{{ user.weekDocumentNum || 0 }}</span>
                                     </td>
                                 </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>


        <!-- 统计卡片网格 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
            <!-- 用户统计卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-blue-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">用户统计</h3>
                        <p class="text-3xl font-bold text-blue-600">{{ userStats.totalUserCount || 0 }}</p>
                        <p class="text-sm text-gray-500 mt-1">总用户数</p>
                    </div>
                    <div class="text-blue-500">
                        <i class="el-icon-user text-4xl"></i>
                    </div>
                </div>
                <div class="mt-4 grid grid-cols-3 gap-2 text-sm">
                    <div class="text-center">
                        <p class="font-medium text-blue-600">{{ userStats.maleCount || 0 }}</p>
                        <p class="text-gray-500">男性</p>
                    </div>
                    <div class="text-center">
                        <p class="font-medium text-pink-600">{{ userStats.femaleCount || 0 }}</p>
                        <p class="text-gray-500">女性</p>
                    </div>
                    <div class="text-center">
                        <p class="font-medium text-gray-600">{{ userStats.unknownGenderCount || 0 }}</p>
                        <p class="text-gray-500">未知</p>
                    </div>
                </div>
            </div>

            <!-- 反馈统计卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-green-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">反馈统计</h3>
                        <p class="text-3xl font-bold text-green-600">{{ feedbackStats.totalCount || 0 }}</p>
                        <p class="text-sm text-gray-500 mt-1">总反馈数</p>
                    </div>
                    <div class="text-green-500">
                        <i class="el-icon-chat-dot-round text-4xl"></i>
                    </div>
                </div>
                <div class="mt-4 grid grid-cols-3 gap-2 text-sm">
                    <div class="text-center">
                        <p class="font-medium text-green-600">{{ feedbackStats.likeCount || 0 }}</p>
                        <p class="text-gray-500">点赞</p>
                    </div>
                    <div class="text-center">
                        <p class="font-medium text-red-600">{{ feedbackStats.dislikeCount || 0 }}</p>
                        <p class="text-gray-500">点踩</p>
                    </div>
                    <div class="text-center">
                        <p class="font-medium text-gray-600">{{ feedbackStats.endCount || 0 }}</p>
                        <p class="text-gray-500">结束</p>
                    </div>
                </div>
            </div>

            <!-- 提示词统计卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-purple-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">提示词统计</h3>
                        <p class="text-3xl font-bold text-purple-600">{{ promptsStats.totalCount || 0 }}</p>
                        <p class="text-sm text-gray-500 mt-1">总提示词数</p>
                    </div>
                    <div class="text-purple-500">
                        <i class="el-icon-edit text-4xl"></i>
                    </div>
                </div>
            </div>

            <!-- 文件组统计卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-orange-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">文件组统计</h3>
                        <p class="text-3xl font-bold text-orange-600">{{ fileGroupStats.totalCount || 0 }}</p>
                        <p class="text-sm text-gray-500 mt-1">总文件组数</p>
                    </div>
                    <div class="text-orange-500">
                        <i class="el-icon-folder text-4xl"></i>
                    </div>
                </div>
            </div>
        </div>

        <!-- 文件详细统计卡片 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- 文件统计详情 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6">
                <h3 class="text-xl font-semibold text-gray-700 mb-4 flex items-center">
                    <i class="el-icon-document text-indigo-500 mr-2"></i>
                    文件统计详情
                </h3>
                <div class="space-y-4">
                    <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                        <span class="text-gray-600">文件总数</span>
                        <span class="font-bold text-indigo-600">{{ fileStats.totalFileCount || 0 }} 个</span>
                    </div>
                    <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                        <span class="text-gray-600">文件总大小</span>
                        <span class="font-bold text-indigo-600">{{ formatFileSize(fileStats.totalFileSize || 0)
                        }}</span>
                    </div>
                    <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                        <span class="text-gray-600">文档切片总数</span>
                        <span class="font-bold text-indigo-600">{{ fileStats.totalDocumentNum || 0 }} 个</span>
                    </div>
                    <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                        <span class="text-gray-600">上传用户数</span>
                        <span class="font-bold text-indigo-600">{{ fileStats.uniqueUploaderCount || 0 }} 人</span>
                    </div>
                </div>
            </div>

            <!-- 用户性别分布 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6">
                <h3 class="text-xl font-semibold text-gray-700 mb-4 flex items-center">
                    <i class="el-icon-pie-chart text-pink-500 mr-2"></i>
                    用户性别分布
                </h3>
                <div class="space-y-4">
                    <div class="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
                        <span class="text-gray-600">男性用户</span>
                        <div class="text-right">
                            <span class="font-bold text-blue-600">{{ userStats.maleCount || 0 }} 人</span>
                            <span class="text-sm text-gray-500 ml-2">({{ userStats.maleRatio || 0 }}%)</span>
                        </div>
                    </div>
                    <div class="flex justify-between items-center p-3 bg-pink-50 rounded-lg">
                        <span class="text-gray-600">女性用户</span>
                        <div class="text-right">
                            <span class="font-bold text-pink-600">{{ userStats.femaleCount || 0 }} 人</span>
                            <span class="text-sm text-gray-500 ml-2">({{ userStats.femaleRatio || 0 }}%)</span>
                        </div>
                    </div>
                    <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                        <span class="text-gray-600">未知性别</span>
                        <div class="text-right">
                            <span class="font-bold text-gray-600">{{ userStats.unknownGenderCount || 0 }} 人</span>
                            <span class="text-sm text-gray-500 ml-2">({{ userStats.unknownRatio || 0 }}%)</span>
                        </div>
                    </div>
                </div>
            </div>
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
    max-height: 24rem; /* 384px */
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