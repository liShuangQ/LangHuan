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

// 本周有效率
const weekEffectiveRate = computed(() => {
    if (props.usageData.weekQuestions === 0) return 0
    return Math.round((props.usageData.weekValidAnswers / props.usageData.weekQuestions) * 100)
})

// 活跃用户数
const activeUserCount = computed(() => {
    return props.usageData.userStats.length
})

// 格式化周开始时间
const weekStartTimeFormatted = computed(() => {
    if (!props.usageData.weekStartTime) return ''
    return new Date(props.usageData.weekStartTime).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
})
</script>

<template>
    <div class="mt-8 mb-8">
        <!-- 使用情况统计卡片-本周情况 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6 mb-8">
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

            <!-- 本周整体有效率 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-blue-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">本周整体有效率</h3>
                        <div class="text-3xl font-bold text-blue-600" :class="{
                            'text-green-500': weekEffectiveRate >= 90,
                            'text-yellow-500': weekEffectiveRate >= 70 && weekEffectiveRate < 90,
                            'text-red-500': weekEffectiveRate < 70
                        }">
                            {{ weekEffectiveRate }}%
                        </div>
                        <p class="text-sm text-gray-500 mt-1"> 有效回答 {{ usageData.weekValidAnswers }} / 总提问 {{ usageData.weekQuestions }}</p>
                    </div>
                    <div class="text-blue-500">
                        <el-icon size="32">
                            <TrendCharts />
                        </el-icon>
                    </div>
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