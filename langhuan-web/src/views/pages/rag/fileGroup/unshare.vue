<template>
    <div>
        <div v-if="isLoading" class="text-center py-8">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span class="ml-2">加载中...</span>
        </div>

        <div
            v-else-if="shareList.length === 0"
            class="text-center py-8 text-gray-500"
        >
            <el-empty description="暂无共享用户" />
        </div>

        <div v-else>
            <el-table
                ref="tableRef"
                :data="shareList"
                @selection-change="handleSelectionChange"
                style="width: 100%"
            >
                <el-table-column type="selection" width="55" />
                <el-table-column
                    prop="sharedWithUserName"
                    label="用户名"
                    width="120"
                />
                <el-table-column
                    prop="sharedByUserName"
                    label="共享者"
                    width="100"
                />
                <el-table-column prop="sharedAt" label="共享时间" width="200" />
                <el-table-column label="权限" min-width="200">
                    <template #default="{ row }">
                        <div class="flex flex-wrap gap-1">
                            <el-tag
                                v-if="row.canRead"
                                type="success"
                                size="small"
                                >读取</el-tag
                            >
                            <el-tag v-if="row.canAdd" type="info" size="small"
                                >新增</el-tag
                            >
                            <el-tag
                                v-if="row.canUpdate"
                                type="warning"
                                size="small"
                                >更新</el-tag
                            >
                            <el-tag
                                v-if="row.canDelete"
                                type="danger"
                                size="small"
                                >删除</el-tag
                            >
                        </div>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <div class="form-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button
                type="danger"
                :disabled="selectedUsers.length === 0"
                @click="handleConfirm"
            >
                取消共享 ({{ selectedUsers.length }})
            </el-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox, ElTable } from "element-plus";
import { Loading } from "@element-plus/icons-vue";
import { http } from "@/plugins/axios";

interface ShareItem {
    id: number;
    fileGroupId: number;
    sharedWith: string;
    userName: string;
    canRead: boolean;
    canAdd: boolean;
    canUpdate: boolean;
    canDelete: boolean;
    sharedBy: string;
    sharedAt: string;
}

interface Props {
    fileGroupId?: string | number;
}

const props = withDefaults(defineProps<Props>(), {
    fileGroupId: "",
});

const emit = defineEmits<{
    confirm: [];
    cancel: [];
}>();

const tableRef = ref<InstanceType<typeof ElTable>>();
const isLoading = ref(false);
const shareList = ref<ShareItem[]>([]);
const selectedUsers = ref<ShareItem[]>([]);

// 获取共享列表
const getShareList = async () => {
    if (!props.fileGroupId) {
        ElMessage.error("文件组ID不能为空");
        return;
    }

    isLoading.value = true;
    try {
        const res = await http.request<any>({
            url: "/rag/file-group/shareList",
            method: "post",
            q_spinning: false,
            q_contentType: "form",
            data: {
                fileGroupId: props.fileGroupId,
                sharedWith: "",
            },
        });

        if (res.code === 200) {
            shareList.value = res.data || [];
        } else {
            ElMessage.error(res.message || "获取共享列表失败");
        }
    } catch (error) {
        console.error("获取共享列表失败:", error);
        ElMessage.error("获取共享列表失败");
    } finally {
        isLoading.value = false;
    }
};

// 处理选择变化
const handleSelectionChange = (selection: ShareItem[]) => {
    selectedUsers.value = selection;
};

// 确认取消共享
const handleConfirm = async () => {
    if (selectedUsers.value.length === 0) {
        ElMessage.warning("请选择要取消共享的用户");
        return;
    }

    try {
        await ElMessageBox.confirm(
            `确认取消 ${selectedUsers.value.length} 个用户的共享权限吗？`,
            "确认取消共享",
            {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning",
            }
        );

        // 将选中的用户转换为逗号分隔的字符串
        const sharedWithList = selectedUsers.value
            .map((user) => user.sharedWith)
            .join(",");

        const res = await http.request<any>({
            url: "/rag/file-group/unshare",
            method: "post",
            q_spinning: true,
            q_contentType: "form",
            data: {
                fileGroupId: props.fileGroupId,
                sharedWith: sharedWithList,
            },
        });

        if (res.code === 200) {
            ElMessage.success("取消共享成功");
            selectedUsers.value = []; // 清空选中的用户
            emit("confirm");
        } else {
            selectedUsers.value = []; // 清空选中的用户
            ElMessage.error(res.message || "取消共享失败");
        }
    } catch (error) {
        if (error !== "cancel") {
            selectedUsers.value = []; // 清空选中的用户
            console.error("取消共享失败:", error);
            ElMessage.error("取消共享失败");
        }
    }
};

// 取消操作
const handleCancel = () => {
    selectedUsers.value = []; // 清空选中的用户
    emit("cancel");
};

// 刷新列表
const refreshList = () => {
    getShareList();
};

// 暴露方法给父组件
defineExpose({
    refreshList,
});

onMounted(() => {
    getShareList();
});
</script>

<style scoped lang="scss">


.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;
}

.is-loading {
    animation: rotating 2s linear infinite;
}

@keyframes rotating {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}
</style>
