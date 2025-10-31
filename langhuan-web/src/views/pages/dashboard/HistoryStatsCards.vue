<script setup lang="ts">
import type { UsageStatsData, FileGroupStats } from "./types";

interface Props {
    usageData: UsageStatsData;
    fileGroupStats: FileGroupStats;
}

defineProps<Props>();
</script>

<template>
    <div>
        <!-- 使用情况统计卡片-总体情况 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6 mb-8">
            <!-- 历史总提问卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-green-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">
                            历史总提问
                        </h3>
                        <p class="text-3xl font-bold text-green-600">
                            {{ usageData.totalQuestions.toLocaleString() }}
                        </p>
                        <p class="text-sm text-gray-500 mt-1">累计提问次数</p>
                    </div>
                    <div class="text-green-500">
                        <el-icon size="32">
                            <ChatLineRound />
                        </el-icon>
                    </div>
                </div>
            </div>
            <!-- 历史总有效回答卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-purple-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">
                            历史有效回答
                        </h3>
                        <p class="text-3xl font-bold text-purple-600">
                            {{ usageData.totalValidAnswers.toLocaleString() }}
                        </p>
                        <p class="text-sm text-gray-500 mt-1">
                            累计有效回答次数
                        </p>
                    </div>
                    <div class="text-purple-500">
                        <el-icon size="32"><Select /></el-icon>
                    </div>
                </div>
            </div>
            <!-- 文件组统计卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-blue-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">
                            文件组统计
                        </h3>
                        <p class="text-3xl font-bold text-blue-600">
                            {{ fileGroupStats.totalCount || 0 }}
                        </p>
                        <p class="text-sm text-gray-500 mt-1">总文件组数</p>
                    </div>
                    <div class="text-blue-500">
                        <i class="el-icon-folder text-4xl"></i>
                    </div>
                </div>
            </div>
            <!-- 历史问题反馈卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-orange-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">
                            历史问题反馈
                        </h3>
                        <p class="text-3xl font-bold text-orange-600">
                            {{ usageData.totalFeedbackCount }}
                        </p>
                        <p class="text-sm text-gray-500 mt-1">累计反馈总数</p>
                    </div>
                    <div class="text-orange-500">
                        <el-icon size="32">
                            <MessageBox />
                        </el-icon>
                    </div>
                </div>
            </div>
            <!-- 反馈统计卡片 -->
            <div class="stats-card bg-white rounded-lg shadow-md p-6 border-l-4 border-green-500">
                <div class="flex items-center justify-between">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-700 mb-2">
                            历史反馈类别统计
                        </h3>
                    </div>
                    <div class="text-green-500">
                        <i class="el-icon-chat-dot-round text-4xl"></i>
                    </div>
                </div>
                <div class="mt-4 grid grid-cols-3 gap-2 text-base">
                    <div class="text-center">
                        <p class="font-semibold text-green-600">
                            {{ usageData.feedbackTypeStats?.like ?? 0 }}
                        </p>
                        <p class="text-gray-500 text-sm">点赞</p>
                    </div>
                    <div class="text-center">
                        <p class="font-semibold text-red-600">
                            {{ usageData.feedbackTypeStats?.dislike ?? 0 }}
                        </p>
                        <p class="text-gray-500 text-sm">点踩</p>
                    </div>
                    <div class="text-center">
                        <p class="font-semibold text-gray-600">
                            {{ usageData.feedbackTypeStats?.end ?? 0 }}
                        </p>
                        <p class="text-gray-500 text-sm">结束</p>
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
