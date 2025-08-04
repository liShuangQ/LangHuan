<script setup lang="ts">
import { computed } from 'vue'

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

interface Props {
    userStats: UserStat[]
}

const props = defineProps<Props>()

// 按提问次数排序的用户统计
const sortedUserStats = computed(() => {
    return [...props.userStats].sort((a, b) => b.questionCount - a.questionCount)
})

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
</script>

<template>
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
                            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700 border-b">用户名</th>
                            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">提问次数</th>
                            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">有效回答数</th>
                            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">无效回答数</th>
                            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">反馈次数</th>
                            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">有效率</th>
                            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">文件数量</th>
                            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">文件大小</th>
                            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700 border-b">切片数量</th>
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
                                    <span class="font-medium text-gray-900">{{ user.name }}</span>
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
</template>

<style scoped>
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