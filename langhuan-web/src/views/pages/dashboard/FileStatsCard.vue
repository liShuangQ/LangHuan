<script setup lang="ts">
interface FileStats {
    totalFileCount: number
    totalFileSize: number
    totalDocumentNum: number
    uniqueUploaderCount: number
}

interface Props {
    fileStats: FileStats
}

defineProps<Props>()

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
                <span class="font-bold text-indigo-600">{{ formatFileSize(fileStats.totalFileSize || 0) }}</span>
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