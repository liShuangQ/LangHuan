<script lang="ts">
export default {
    auth: true,
}
</script>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { http } from '@/plugins/axios'
import { ElMessage } from 'element-plus'

// 定义响应式数据
const loading = ref(false)
const feedbackStats = ref<any>({})
const promptsStats = ref<any>({})
const fileGroupStats = ref<any>({})
const fileStats = ref<any>({})
const userStats = ref<any>({})
// 基础项目名称
const BASE_PROJECT_NAME = computed(() => {
    return process.env.BASE_PROJECT_NAME as string
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

// 加载所有统计数据
const loadAllStats = async () => {
    loading.value = true
    try {
        await Promise.all([
            getFeedbackStats(),
            getPromptsStats(),
            getFileGroupStats(),
            getFileStats(),
            getUserStats()
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
                <p class="text-gray-600">系统运行状态和数据统计概览</p>
            </div>
            <el-button :loading="loading" @click="refreshData" class="refresh-btn">
                <el-icon><Refresh /></el-icon>
            </el-button>
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

/* 自定义滚动条 */
::-webkit-scrollbar {
    width: 6px;
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
</style>