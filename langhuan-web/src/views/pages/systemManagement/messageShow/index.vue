<template>
    <div class="notification-window">
        <div class="notification-layout">
            <!-- 左侧筛选菜单 -->
            <div class="filter-sidebar">
                <!-- 通知级别筛选 -->
                <div class="filter-section">
                    <h3 class="filter-title">通知级别</h3>
                    <div class="level-menu">
                        <div class="level-menu-item" :class="{ 'active': filterForm.notificationLevel === '' }"
                            @click="setLevelFilter('')">
                            <div class="level-icon all">
                                <el-icon>
                                    <List />
                                </el-icon>
                            </div>
                            <div class="level-info">
                                <span class="level-name">全部</span>
                                <span class="level-count">{{ statistics.totalUnread }}</span>
                            </div>
                        </div>

                        <div class="level-menu-item critical"
                            :class="{ 'active': filterForm.notificationLevel === 'critical' }"
                            @click="setLevelFilter('critical')">
                            <div class="level-icon">
                                <el-icon>
                                    <Warning />
                                </el-icon>
                            </div>
                            <div class="level-info">
                                <span class="level-name">紧急</span>
                                <span class="level-count">{{ statistics.critical }}</span>
                            </div>
                        </div>
                        <div class="level-menu-item warning"
                            :class="{ 'active': filterForm.notificationLevel === 'warning' }"
                            @click="setLevelFilter('warning')">
                            <div class="level-icon">
                                <el-icon>
                                    <WarningFilled />
                                </el-icon>
                            </div>
                            <div class="level-info">
                                <span class="level-name">警告</span>
                                <span class="level-count">{{ statistics.warning }}</span>
                            </div>
                        </div>

                        <div class="level-menu-item error"
                            :class="{ 'active': filterForm.notificationLevel === 'error' }"
                            @click="setLevelFilter('error')">
                            <div class="level-icon">
                                <el-icon>
                                    <CircleClose />
                                </el-icon>
                            </div>
                            <div class="level-info">
                                <span class="level-name">错误</span>
                                <span class="level-count">{{ statistics.error }}</span>
                            </div>
                        </div>


                        <div class="level-menu-item info" :class="{ 'active': filterForm.notificationLevel === 'info' }"
                            @click="setLevelFilter('info')">
                            <div class="level-icon">
                                <el-icon>
                                    <InfoFilled />
                                </el-icon>
                            </div>
                            <div class="level-info">
                                <span class="level-name">信息</span>
                                <span class="level-count">{{ statistics.info }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 通知类型筛选 -->
                <div class="filter-section">
                    <h3 class="filter-title">通知类型</h3>
                    <div class="type-menu">
                        <div class="type-menu-item" :class="{ 'active': filterForm.notificationType === '' }"
                            @click="setTypeFilter('')">
                            <el-icon>
                                <List />
                            </el-icon>
                            <span>全部类型</span>
                        </div>
                        <div class="type-menu-item" :class="{ 'active': filterForm.notificationType === 'system' }"
                            @click="setTypeFilter('system')">
                            <el-icon>
                                <Bell />
                            </el-icon>
                            <span>系统通知</span>
                        </div>
                        <div class="type-menu-item" :class="{ 'active': filterForm.notificationType === 'reminder' }"
                            @click="setTypeFilter('reminder')">
                            <el-icon>
                                <Clock />
                            </el-icon>
                            <span>提醒通知</span>
                        </div>
                        <div class="type-menu-item" :class="{ 'active': filterForm.notificationType === 'alert' }"
                            @click="setTypeFilter('alert')">
                            <el-icon>
                                <Warning />
                            </el-icon>
                            <span>警报通知</span>
                        </div>
                        <div class="type-menu-item" :class="{ 'active': filterForm.notificationType === 'message' }"
                            @click="setTypeFilter('message')">
                            <el-icon>
                                <ChatDotRound />
                            </el-icon>
                            <span>消息通知</span>
                        </div>
                        <div class="type-menu-item" :class="{ 'active': filterForm.notificationType === 'update' }"
                            @click="setTypeFilter('update')">
                            <el-icon>
                                <Refresh />
                            </el-icon>
                            <span>更新通知</span>
                        </div>
                    </div>
                </div>


            </div>

            <!-- 右侧主要内容区域 -->
            <div class="main-content">
                <!-- 通知窗口头部 -->
                <div class="notification-header">
                    <div class="flex items-center justify-between">
                        <h2 class="page-title">消息通知</h2>
                        <el-button type="primary" size="small" @click="refreshPage" :loading="loading" circle>
                            <el-icon>
                                <Refresh />
                            </el-icon>
                        </el-button>
                    </div>
                </div>

                <!-- 通知列表 -->
                <div class="notification-list-container">
                    <el-scrollbar height="500px">
                        <div v-if="notifications.length === 0" class="empty-state">
                            <el-empty description="暂无通知" />
                        </div>
                        <div v-else class="notification-list">
                            <div v-for="notification in notifications" :key="notification.id" class="notification-card"
                                :class="{
                                    'critical': notification.notificationLevel === 'critical',
                                    'error': notification.notificationLevel === 'error',
                                    'warning': notification.notificationLevel === 'warning',
                                    'info': notification.notificationLevel === 'info',
                                    'read': notification.isRead
                                }" @click="showNotificationDetail(notification)">
                                <div class="notification-card-header">
                                    <div class="notification-meta">
                                        <el-tag :type="getLevelTagType(notification.notificationLevel) as any"
                                            size="small" effect="dark">
                                            {{ getLevelText(notification.notificationLevel) }}
                                        </el-tag>

                                        <el-tag type="info" size="small" effect="plain">
                                            {{ getTypeText(notification.notificationType) }}
                                        </el-tag>

                                        <el-tag v-if="notification.userId" type="success" size="small" effect="light">
                                            {{ notification.isRead ? '已读' : '未读' }}
                                        </el-tag>

                                        <el-tag v-if="!notification.userId" type="warning" size="small" effect="light">
                                            全局
                                        </el-tag>
                                    </div>

                                    <div class="notification-actions">
                                        <el-button v-if="!notification.isRead && notification.userId" type="primary"
                                            size="small" text @click.stop="markAsRead(notification.id)">
                                            <el-icon>
                                                <Check />
                                            </el-icon>
                                            标记已读
                                        </el-button>
                                    </div>
                                </div>

                                <div class="notification-card-body">
                                    <h4 class="notification-title">{{ notification.title }}</h4>
                                    <p class="notification-content">{{ notification.content }}</p>
                                    <div class="notification-time">
                                        <el-icon>
                                            <Clock />
                                        </el-icon>
                                        {{ formatDate(notification.createdAt) }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-scrollbar>
                </div>

                <!-- 分页 -->
                <div class="notification-pagination">
                    <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                        :page-sizes="[10, 20, 50]" :total="pagination.total" layout="total, prev, pager, next"
                        @current-change="handleCurrentChange" small />
                </div>
            </div>
        </div>

        <!-- 通知详情弹窗 -->
        <el-dialog v-model="detailDialogVisible" title="通知详情" width="650px" :before-close="handleDetailClose"
            class="notification-detail-dialog">
            <div v-if="selectedNotification" class="notification-detail">
                <div class="detail-header">
                    <div class="detail-meta">
                        <el-tag :type="getLevelTagType(selectedNotification.notificationLevel) as any" size="default"
                            effect="dark">
                            {{ getLevelText(selectedNotification.notificationLevel) }}
                        </el-tag>

                        <el-tag type="info" size="default" effect="plain">
                            {{ getTypeText(selectedNotification.notificationType) }}
                        </el-tag>

                        <el-tag v-if="!selectedNotification.isRead" type="success" size="default" effect="light">
                            未读
                        </el-tag>

                        <el-tag v-if="!selectedNotification.userId" type="warning" size="default" effect="light">
                            全局通知
                        </el-tag>
                    </div>

                    <h3 class="detail-title">
                        {{ selectedNotification.title }}
                    </h3>
                </div>
                <h4 class="section-title">通知内容</h4>
                <div class="detail-content">
                    <div class="content-section">
                        <div class="content-body">
                            <v-md-preview :text="selectedNotification.content"></v-md-preview>
                        </div>
                    </div>

                </div>
                <div class="detail-info">
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="info-label">创建时间:</span>
                            <span class="info-value">{{ formatDate(selectedNotification.createdAt) }}</span>
                        </div>
                        <div v-if="selectedNotification.expiresAt" class="info-item">
                            <span class="info-label">过期时间:</span>
                            <span class="info-value">{{ formatDate(selectedNotification.expiresAt) }}</span>
                        </div>
                        <!-- <div v-if="selectedNotification.userId" class="info-item">
                            <span class="info-label">目标用户:</span>
                            <span class="info-value">{{ selectedNotification.userId }}</span>
                        </div> -->
                        <div v-if="selectedNotification.referenceId" class="info-item">
                            <span class="info-label">关联ID:</span>
                            <span class="info-value">{{ selectedNotification.referenceId }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button
                        v-if="selectedNotification && !selectedNotification.isRead && selectedNotification.userId"
                        type="primary" @click="markAsRead(selectedNotification.id)">
                        <el-icon>
                            <Check />
                        </el-icon>
                        标记已读
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts">
export default {
    auto: true,
};
</script>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
    Refresh, Bell, List, Warning, CircleClose, WarningFilled, InfoFilled,
    Check, Clock
} from '@element-plus/icons-vue'
import { http } from '@/plugins/axios'
import user from '@/store/user';

// 接口类型定义
interface Notification {
    id: number
    userId?: string
    templateId?: number
    title: string
    content: string
    notificationLevel: 'info' | 'warning' | 'error' | 'critical'
    notificationType: 'system' | 'reminder' | 'alert' | 'message' | 'update'
    isRead: boolean
    isArchived: boolean
    referenceId?: string
    referenceType?: string
    expiresAt?: string
    createdAt: string
}

interface Statistics {
    totalUnread: number
    critical: number
    error: number
    warning: number
    info: number
}

// 响应式数据
const loading = ref(false)
const notifications = ref<Notification[]>([])
const selectedNotification = ref<Notification | null>(null)
const detailDialogVisible = ref(false)

// 筛选表单
const filterForm = reactive({
    notificationLevel: '',
    notificationType: ''
})

// 分页数据
const pagination = reactive({
    pageNum: 1,
    pageSize: 20,
    total: 0
})

// 统计数据
const statistics = ref<Statistics>({
    totalUnread: 0,
    critical: 0,
    error: 0,
    warning: 0,
    info: 0
})

// 当前用户ID
const currentUserId = user().info.user.username
// 设置级别筛选
const setLevelFilter = (level: string) => {
    filterForm.notificationLevel = level
    pagination.pageNum = 1
    loadNotifications()
}

// 设置类型筛选
const setTypeFilter = (type: string) => {
    filterForm.notificationType = type
    pagination.pageNum = 1
    loadNotifications()
}





// 获取级别标签类型
const getLevelTagType = (level: string) => {
    const typeMap: Record<string, string> = {
        critical: 'danger',
        error: 'error',
        warning: 'warning',
        info: 'info'
    }
    return typeMap[level] || 'info'
}

// 获取级别文本
const getLevelText = (level: string) => {
    const textMap: Record<string, string> = {
        critical: '紧急',
        error: '错误',
        warning: '警告',
        info: '信息'
    }
    return textMap[level] || level
}

// 获取类型文本
const getTypeText = (type: string) => {
    const textMap: Record<string, string> = {
        system: '系统',
        reminder: '提醒',
        alert: '警报',
        message: '消息',
        update: '更新'
    }
    return textMap[type] || type
}

// 格式化日期
const formatDate = (dateStr: string) => {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    })
}

// 加载通知列表
const loadNotifications = async () => {
    loading.value = true
    try {
        const params = {
            userId: currentUserId,
            // includeRead: false,
            notificationLevel: filterForm.notificationLevel || undefined,
            notificationType: filterForm.notificationType || undefined,
            pageNum: pagination.pageNum,
            pageSize: pagination.pageSize
        }



        const response = await http.request<any>({
            url: '/notifications/getUserNotifications',
            method: 'post',
            q_spinning: false,
            q_contentType: 'form',
            data: params
        })

        if (response.code === 200) {
            notifications.value = response.data.records || []
            pagination.total = response.data.total || 0
        } else {
            ElMessage.error(response.message || '获取通知列表失败')
        }
    } catch (error) {
        console.error('获取通知列表失败:', error)
        ElMessage.error('获取通知列表失败')
    } finally {
        loading.value = false
    }
}

// 加载统计信息
const loadStatistics = async () => {
    try {
        const response = await http.request<any>({
            url: '/notifications/getStatistics',
            method: 'post',
            q_spinning: false,
            q_contentType: 'form',
            data: {
                userId: currentUserId
            }
        })

        if (response.code === 200) {
            statistics.value = response.data
        }
    } catch (error) {
        console.error('获取统计信息失败:', error)
    }
}

// 定义事件发射
const emit = defineEmits(['unread-count-changed'])

// 标记为已读
const markAsRead = async (id: number) => {
    try {
        const response = await http.request<any>({
            url: '/notifications/mark-read',
            method: 'post',
            q_spinning: true,
            q_contentType: 'form',
            data: { id }
        })

        if (response.code === 200) {
            ElMessage.success('标记已读成功')
            // 更新本地数据
            const notification = notifications.value.find(n => n.id === id)
            if (notification) {
                notification.isRead = true
            }
            if (selectedNotification.value && selectedNotification.value.id === id) {
                selectedNotification.value.isRead = true
            }
            // 重新加载统计信息
            loadStatistics()
            loadNotifications()
            // 通知父组件更新未读消息数量
            emit('unread-count-changed')
        } else {
            ElMessage.error(response.message || '标记已读失败')
        }
    } catch (error) {
        console.error('标记已读失败:', error)
        ElMessage.error('标记已读失败')
    }
}

// 显示通知详情
const showNotificationDetail = (notification: Notification) => {
    selectedNotification.value = notification
    detailDialogVisible.value = true
}

// 关闭详情弹窗
const handleDetailClose = () => {
    detailDialogVisible.value = false
    selectedNotification.value = null
}

// 当前页改变
const handleCurrentChange = (page: number) => {
    pagination.pageNum = page
    loadNotifications()
}
const refreshPage = () => {
    loadNotifications()
    loadStatistics()
}

// 组件挂载时加载数据
onMounted(() => {
    loadNotifications()
    loadStatistics()
})
defineExpose({
    refreshPage
})
</script>

<style scoped>
/* 通知窗口整体样式 */
.notification-window {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    max-width: 800px;
    margin: 0 auto;
    height: 700px;
    overflow: hidden;
}

/* 布局容器 */
.notification-layout {
    display: flex;
    gap: 16px;
    height: 100%;
}

/* 左侧筛选菜单 */
.filter-sidebar {
    width: 180px;
    background: white;
    border-radius: 8px;
    padding: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow-y: auto;
    flex-shrink: 0;
}

/* 筛选区块 */
.filter-section {
    margin-bottom: 16px;
}

.filter-section:last-child {
    margin-bottom: 0;
}

.filter-title {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
    padding-bottom: 6px;
    border-bottom: 1px solid #e4e7ed;
}

/* 级别菜单样式 */
.level-menu {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.level-menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 10px;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 1px solid #e4e7ed;
    background: #fafafa;
}

.level-menu-item:hover {
    border-color: #409EFF;
    background: #f0f7ff;
}

.level-menu-item.active {
    border-color: #409EFF;
    background: #f0f7ff;
    box-shadow: 0 2px 6px rgba(64, 158, 255, 0.15);
}

.level-icon {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 12px;
    flex-shrink: 0;
}

.level-icon.all {
    background: #409EFF;
}

.level-menu-item.critical .level-icon {
    background: #F56C6C;
}

.level-menu-item.error .level-icon {
    background: #E6A23C;
}

.level-menu-item.warning .level-icon {
    background: #E6A23C;
}

.level-menu-item.info .level-icon {
    background: rgb(144, 147, 153);
}

.level-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.level-name {
    font-size: 12px;
    font-weight: 500;
    color: #303133;
}

.level-count {
    font-size: 14px;
    font-weight: bold;
    color: #409EFF;
}

/* 类型菜单样式 */
.type-menu {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.type-menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 10px;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
    color: #606266;
    font-size: 12px;
}

.type-menu-item:hover {
    background: #f0f7ff;
    color: #409EFF;
}

.type-menu-item.active {
    background: #409EFF;
    color: white;
    font-weight: 500;
}

/* 右侧主要内容区域 */
.main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow: hidden;
}

/* 通知窗口头部 */
.notification-header {
    padding: 16px 20px;
    border-bottom: 1px solid #e4e7ed;
    flex-shrink: 0;
}

.page-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0;
}

/* 通知列表容器 */
.notification-list-container {
    padding: 16px 20px;
    overflow: hidden;
}

.empty-state {
    text-align: center;
    padding: 60px 20px;
}

/* 通知卡片样式 */
.notification-card {
    background: #fafafa;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    border-left: 3px solid #e4e7ed;
    border: 1px solid #f0f0f0;
}

.notification-card:hover {
    background: white;
    border-color: #409EFF;
    box-shadow: 0 2px 6px rgba(64, 158, 255, 0.1);
}

.notification-card.critical {
    border-left-color: #F56C6C;
}

.notification-card.error {
    border-left-color: #E6A23C;
}

.notification-card.warning {
    border-left-color: #E6A23C;
}

.notification-card.info {
    border-left-color: #409EFF;
}

.notification-card.read {
    opacity: 0.7;
}

.notification-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.notification-meta {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
}

.notification-actions {
    display: flex;
    gap: 6px;
}

.notification-card-body {
    padding-left: 4px;
}

.notification-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 6px;
    line-height: 1.4;
}

.notification-content {
    font-size: 13px;
    color: #606266;
    line-height: 1.4;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.notification-time {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: #909399;
}

/* 分页样式 */
.notification-pagination {
    padding: 16px 20px;
    text-align: center;
    border-top: 1px solid #e4e7ed;
    flex-shrink: 0;
}

/* 详情弹窗样式 */
.notification-detail-dialog {
    border-radius: 8px;
    overflow: hidden;
}

.detail-header {
    margin-bottom: 16px;
}

.detail-meta {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    margin-bottom: 12px;
}

.detail-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    line-height: 1.4;
}

.detail-content {
    max-height: 60vh;
    overflow-y: auto;
    background-color: rgb(248, 250, 252);
}

.content-section {
    margin-bottom: 16px;
}

.section-title {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
    padding-bottom: 6px;
    border-bottom: 1px solid #e4e7ed;
}

.content-body {
    background: #f8f9fa;
    border-radius: 4px;
    height: 300px;
    padding: 12px;
    line-height: 1.5;
    color: #303133;
    font-size: 14px;
}

.detail-info {
    border-top: 1px solid #e4e7ed;
    padding-top: 12px;
}

.info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 8px;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 6px;
}

.info-label {
    font-size: 13px;
    color: #606266;
    font-weight: 500;
    min-width: 70px;
}

.info-value {
    font-size: 13px;
    color: #303133;
    flex: 1;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .notification-window {
        padding: 12px;
        margin: 12px;
        height: calc(100vh - 24px);
    }

    .notification-layout {
        flex-direction: column;
        gap: 12px;
    }

    .filter-sidebar {
        width: 100%;
        padding: 12px;
        max-height: 300px;
        overflow-y: auto;
    }

    .filter-section {
        margin-bottom: 12px;
    }

    .filter-title {
        font-size: 12px;
        margin-bottom: 6px;
        padding-bottom: 4px;
    }

    .level-menu {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 4px;
    }

    .level-menu-item {
        padding: 6px 8px;
        gap: 6px;
    }

    .level-icon {
        width: 20px;
        height: 20px;
        font-size: 10px;
    }

    .level-name {
        font-size: 11px;
    }

    .level-count {
        font-size: 12px;
    }

    .type-menu {
        gap: 3px;
    }

    .type-menu-item {
        padding: 5px 8px;
        font-size: 11px;
        gap: 6px;
    }

    .main-content {
        flex: 1;
        min-height: 0;
    }

    .notification-header {
        padding: 12px 16px;
    }

    .page-title {
        font-size: 16px;
    }

    .notification-list-container {
        padding: 12px 16px;
    }

    .notification-card {
        padding: 10px;
    }

    .notification-meta {
        flex-direction: column;
        gap: 4px;
    }

    .notification-pagination {
        padding: 12px 16px;
    }
}
</style>