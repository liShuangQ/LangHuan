<template>
    <div class="request-log-container">
        <!-- 查询表单 -->
        <div class="search-form-wrapper mb-4">
            <ElementFormC
                ref="searchFormRef"
                :formConfig="formConfig"
                :formItemConfig="formItemConfig"
                @handle="formHandle"
            >
                <template #custom-button>
                    <div class="float-right">
                        <el-button type="primary" @click="handleSearch">
                            查询
                        </el-button>
                        <el-button @click="handleReset"> 重置 </el-button>
                        <!-- <el-button type="warning" @click="handleCleanHistory">
                    <el-icon><Delete /></el-icon>
                    清理历史
                </el-button>
                <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedRows.length">
                    <el-icon><Delete /></el-icon>
                    批量删除 ({{ selectedRows.length }})
                </el-button> -->
                    </div>
                </template>
            </ElementFormC>
        </div>

        <!-- 统计信息 -->
        <!-- <div class="statistics-wrapper mb-4 p-4">
            <div class="flex gap-6">
                <div class="stat-item">
                    <span class="text-gray-500">总调用次数：</span>
                    <span class="text-2xl font-bold text-blue-600">{{ statistics.totalCount || 0 }}</span>
                </div>
                <div class="stat-item">
                    <span class="text-gray-500">成功率：</span>
                    <span class="text-2xl font-bold text-green-600">{{ statistics.successRate || '0%' }}</span>
                </div>
                <div class="stat-item">
                    <span class="text-gray-500">平均响应时间：</span>
                    <span class="text-2xl font-bold text-orange-600">{{ statistics.avgResponseTime || '0ms' }}</span>
                </div>
            </div>
        </div> -->

        <!-- 数据表格 -->
        <div style="height: calc(100% - 66px)">
            <ElementTableC
                ref="tableRef"
                :paginationConfig="paginationConfig"
                :tableColumnConfig="tableColumnConfig"
                :tableConfig="tableConfig"
                :tableData="tableData"
                @handle="tableHandle"
            >
                <!-- HTTP方法插槽 -->
                <template #content-httpMethod="scope">
                    <el-tag
                        :type="getHttpMethodType(scope.row.httpMethod) as any"
                        size="small"
                    >
                        {{ scope.row.httpMethod }}
                    </el-tag>
                </template>

                <!-- 执行时长插槽 -->
                <template #content-executionTime="scope">
                    <span
                        :class="getExecutionTimeClass(scope.row.executionTime)"
                    >
                        {{ scope.row.executionTime }}ms
                    </span>
                </template>

                <!-- 执行状态插槽 -->
                <template #content-isSuccess="scope">
                    <el-tag
                        :type="scope.row.isSuccess ? 'success' : 'danger'"
                        size="small"
                    >
                        {{ scope.row.isSuccess ? "成功" : "失败" }}
                    </el-tag>
                </template>

                <!-- 创建时间插槽 -->
                <template #content-createTime="scope">
                    {{ formatDateTime(scope.row.createTime) }}
                </template>

                <!-- 操作按钮插槽 -->
                <template #content-actions="scope">
                    <div class="flex gap-2">
                        <el-button
                            type="primary"
                            size="small"
                            link
                            @click="handleViewDetail(scope.row)"
                        >
                            查看详情
                        </el-button>
                        <el-button
                            type="danger"
                            size="small"
                            link
                            @click="handleDelete(scope.row)"
                        >
                            删除
                        </el-button>
                    </div>
                </template>

                <!-- 展开行插槽 -->
                <template #expand="props">
                    <div class="p-4 bg-gray-50">
                        <div class="grid grid-cols-2 gap-4">
                            <div>
                                <h4 class="font-semibold mb-2 text-gray-700">
                                    接口信息
                                </h4>
                                <p>
                                    <span class="font-medium">接口说明：</span
                                    >{{ props.row.apiDescription || "无" }}
                                </p>
                                <p>
                                    <span class="font-medium">请求IP：</span
                                    >{{ props.row.requestIp }}
                                </p>
                            </div>
                            <div>
                                <h4 class="font-semibold mb-2 text-gray-700">
                                    执行信息
                                </h4>
                                <p>
                                    <span class="font-medium">用户ID：</span
                                    >{{ props.row.userId }}
                                </p>
                                <p>
                                    <span class="font-medium">错误信息：</span
                                    >{{ props.row.errorMessage || "无" }}
                                </p>
                            </div>
                        </div>
                        <div class="mt-4">
                            <h4 class="font-semibold mb-2 text-gray-700">
                                请求参数
                            </h4>
                            <pre
                                class="bg-white p-2 rounded border text-sm overflow-auto max-h-32"
                                >{{
                                    formatJsonData(props.row.requestParams)
                                }}</pre
                            >
                        </div>
                        <div class="mt-4">
                            <h4 class="font-semibold mb-2 text-gray-700">
                                响应数据
                            </h4>
                            <pre
                                class="bg-white p-2 rounded border text-sm overflow-auto max-h-32"
                                >{{
                                    formatJsonData(props.row.responseData)
                                }}</pre
                            >
                        </div>
                    </div>
                </template>
            </ElementTableC>
        </div>

        <!-- 详情对话框 -->
        <el-dialog
            v-model="detailDialogVisible"
            title="接口调用详情"
            width="800px"
            :close-on-click-modal="false"
        >
            <div v-if="currentDetailRow">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="接口名称">{{
                        currentDetailRow.apiName
                    }}</el-descriptions-item>
                    <el-descriptions-item label="接口URL">{{
                        currentDetailRow.apiUrl
                    }}</el-descriptions-item>
                    <el-descriptions-item label="HTTP方法">
                        <el-tag
                            :type="getHttpMethodType(currentDetailRow.httpMethod) as any"
                            size="small"
                        >
                            {{ currentDetailRow.httpMethod }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="执行状态">
                        <el-tag
                            :type="
                                currentDetailRow.isSuccess
                                    ? 'success'
                                    : 'danger'
                            "
                            size="small"
                        >
                            {{ currentDetailRow.isSuccess ? "成功" : "失败" }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="用户名">{{
                        currentDetailRow.username
                    }}</el-descriptions-item>
                    <el-descriptions-item label="请求IP">{{
                        currentDetailRow.requestIp
                    }}</el-descriptions-item>
                    <el-descriptions-item label="执行时长"
                        >{{
                            currentDetailRow.executionTime
                        }}ms</el-descriptions-item
                    >
                    <el-descriptions-item label="创建时间">{{
                        formatDateTime(currentDetailRow.createTime)
                    }}</el-descriptions-item>
                    <el-descriptions-item label="接口说明" :span="2">{{
                        currentDetailRow.apiDescription || "无"
                    }}</el-descriptions-item>
                    <el-descriptions-item label="错误信息" :span="2">{{
                        currentDetailRow.errorMessage || "无"
                    }}</el-descriptions-item>
                </el-descriptions>

                <div class="mt-4">
                    <h4 class="font-semibold mb-2">请求参数</h4>
                    <el-input
                        type="textarea"
                        :value="formatJsonData(currentDetailRow.requestParams)"
                        readonly
                        :rows="6"
                    />
                </div>

                <div class="mt-4">
                    <h4 class="font-semibold mb-2">响应数据</h4>
                    <el-input
                        type="textarea"
                        :value="formatJsonData(currentDetailRow.responseData)"
                        readonly
                        :rows="6"
                    />
                </div>
            </div>
        </el-dialog>

        <!-- 清理历史对话框 -->
        <el-dialog
            v-model="cleanDialogVisible"
            title="清理历史日志"
            width="400px"
            :close-on-click-modal="false"
        >
            <div>
                <p class="mb-4">
                    请选择要保留的天数，将删除指定天数之前的所有日志记录。
                </p>
                <el-form>
                    <el-form-item label="保留天数">
                        <el-input-number
                            v-model="cleanDays"
                            :min="1"
                            :max="365"
                            placeholder="请输入保留天数"
                        />
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <div class="flex gap-2 justify-end">
                    <el-button @click="cleanDialogVisible = false"
                        >取消</el-button
                    >
                    <el-button type="warning" @click="confirmCleanHistory"
                        >确认清理</el-button
                    >
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

<script lang="ts" setup>
import { FormDefineExpose } from "@/components/globalComponents/ElementFormC/form-component";
import { TableDefineExpose } from "@/components/globalComponents/ElementTableC/table-component";
import { formConfig, formItemConfig } from "./formConfig";
import {
    tableColumnConfig,
    tableConfig,
    tableData,
    paginationConfig,
} from "./tableConfig";
import {
    searchApiLogs,
    getApiLogStatistics,
    deleteApiLog,
    batchDeleteApiLogs,
    cleanHistoryLogs,
} from "./api";

// 引用
const searchFormRef = ref<FormDefineExpose>();
const tableRef = ref<TableDefineExpose>();

// 响应式数据
const selectedRows = ref<any[]>([]);
const statistics = ref<any>({});
const detailDialogVisible = ref(false);
const cleanDialogVisible = ref(false);
const currentDetailRow = ref<any>(null);
const cleanDays = ref(30);

// 生命周期
onMounted(() => {
    loadData();
    loadStatistics();
});

// 方法
const loadData = async () => {
    try {
        const formData = searchFormRef.value?.getFromValue() || {};
        const params = {
            current: paginationConfig.value.currentPage,
            size: paginationConfig.value.pageSize,
            apiName: (formData as any).apiName,
            username: (formData as any).username,
            httpMethod: (formData as any).httpMethod,
            isSuccess: (formData as any).isSuccess,
            startTime: (formData as any).timeRange?.[0],
            endTime: (formData as any).timeRange?.[1],
        };

        const res = await searchApiLogs(params);

        tableData.value = res.data.records || [];
        paginationConfig.value.total = res.data.total || 0;
    } catch (error) {
        console.error("加载数据失败:", error);
        ElMessage.error("加载数据失败");
    }
};

const loadStatistics = async () => {
    return;
};

const handleSearch = () => {
    paginationConfig.value.currentPage = 1;
    loadData();
};

const handleReset = () => {
    searchFormRef.value?.resetForm();
    paginationConfig.value.currentPage = 1;
    loadData();
};

const handleViewDetail = (row: any) => {
    currentDetailRow.value = row;
    detailDialogVisible.value = true;
};

const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm("确定要删除这条日志记录吗？", "确认删除", {
            type: "warning",
        });

        const res = await deleteApiLog(row.id);
        if (res.success) {
            ElMessage.success("删除成功");
            loadData();
            loadStatistics();
        } else {
            ElMessage.error(res.message || "删除失败");
        }
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除失败:", error);
            ElMessage.error("删除失败");
        }
    }
};

const handleBatchDelete = async () => {
    if (!selectedRows.value.length) {
        ElMessage.warning("请选择要删除的记录");
        return;
    }

    try {
        await ElMessageBox.confirm(
            `确定要删除选中的 ${selectedRows.value.length} 条记录吗？`,
            "确认批量删除",
            {
                type: "warning",
            }
        );

        const ids = selectedRows.value.map((row) => row.id);
        const res = await batchDeleteApiLogs(ids);
        if (res.success) {
            ElMessage.success("批量删除成功");
            selectedRows.value = [];
            tableRef.value?.tableMethod("clearSelection");
            loadData();
            loadStatistics();
        } else {
            ElMessage.error(res.message || "批量删除失败");
        }
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除失败:", error);
            ElMessage.error("批量删除失败");
        }
    }
};

const handleCleanHistory = () => {
    cleanDialogVisible.value = true;
};

const confirmCleanHistory = async () => {
    try {
        await ElMessageBox.confirm(
            `确定要清理 ${cleanDays.value} 天前的历史日志吗？此操作不可恢复。`,
            "确认清理",
            {
                type: "warning",
            }
        );

        const res = await cleanHistoryLogs(cleanDays.value);
        if (res.success) {
            ElMessage.success(`成功清理了 ${res.data} 条历史记录`);
            cleanDialogVisible.value = false;
            loadData();
            loadStatistics();
        } else {
            ElMessage.error(res.message || "清理失败");
        }
    } catch (error) {
        if (error !== "cancel") {
            console.error("清理失败:", error);
            ElMessage.error("清理失败");
        }
    }
};

const getHttpMethodType = (method: string) => {
    const types: Record<string, string> = {
        GET: "success",
        POST: "",
        PUT: "warning",
        DELETE: "danger",
        PATCH: "info",
    };
    return types[method] || "info";
};

const getExecutionTimeClass = (time: number) => {
    if (time < 3000) return "text-green-600";
    if (time < 5000) return "text-orange-600";
    return "text-red-600";
};

const formatDateTime = (dateTime: string) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleString("zh-CN");
};

const formatJsonData = (data: string) => {
    if (!data) return "无";
    try {
        return JSON.stringify(JSON.parse(data), null, 2);
    } catch {
        return data;
    }
};

const formHandle = (type: string, key: string, data: any, other: any) => {
    // 表单事件处理
};

const tableHandle = (type: string, data: any, key: string) => {
    // 表格事件处理
    if (type === "selection") {
        selectedRows.value = data;
    }
    if (type === "handleCurrentChange" || type === "handleSizeChange") {
        loadData();
    }
};
</script>

<style scoped>
.request-log-container {
    padding: 16px;
    padding-bottom: 0;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

:deep(.el-descriptions__label) {
    font-weight: 600;
}
</style>
