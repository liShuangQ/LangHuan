<template>
    <div class="share-manage-container">
        <el-tabs v-model="activeTab" class="share-tabs">
            <!-- 管理共享 Tab -->
            <el-tab-pane label="管理共享" name="unshare">
                <div class="tab-content">
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
                            <el-table-column
                                prop="sharedAt"
                                label="共享时间"
                                width="200"
                            />
                            <el-table-column label="权限" min-width="200">
                                <template #default="{ row }">
                                    <div class="flex flex-wrap gap-1">
                                        <el-tag
                                            v-if="row.canRead"
                                            type="success"
                                            size="small"
                                            >读取</el-tag
                                        >
                                        <el-tag
                                            v-if="row.canAdd"
                                            type="info"
                                            size="small"
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
                        <el-button @click="handleUnshareCancel">取消</el-button>
                        <el-button
                            type="danger"
                            :disabled="selectedUsers.length === 0"
                            @click="handleUnshareConfirm"
                        >
                            取消共享 ({{ selectedUsers.length }})
                        </el-button>
                    </div>
                </div>
            </el-tab-pane>

            <!-- 添加共享 Tab -->
            <el-tab-pane label="添加共享" name="share">
                <div class="tab-content">
                    <el-form
                        ref="shareFormRef"
                        :model="shareForm"
                        :rules="shareFormRules"
                        label-width="120px"
                        class="share-form"
                    >
                        <el-form-item label="共享用户" prop="sharedUsers">
                            <el-select
                                v-model="shareForm.sharedUsers"
                                placeholder="请选择共享用户"
                                style="width: 100%"
                                clearable
                                multiple
                                filterable
                                remote
                                @change="userChangeMethod"
                                :remote-method="userRemoteMethod"
                            >
                                <el-option
                                    v-for="user in userOptions"
                                    :key="user.value"
                                    :label="user.label"
                                    :value="user.value"
                                />
                            </el-select>
                        </el-form-item>

                        <el-form-item label="权限配置">
                            <div class="permission-grid">
                                <div class="permission-item">
                                    <div class="permission-label">读取权限</div>
                                    <el-switch
                                        v-model="shareForm.canRead"
                                        active-text="开启"
                                        inactive-text="关闭"
                                    />
                                </div>
                                <div class="permission-item">
                                    <div class="permission-label">新增权限</div>
                                    <el-switch
                                        v-model="shareForm.canAdd"
                                        active-text="开启"
                                        inactive-text="关闭"
                                    />
                                </div>
                                <div class="permission-item">
                                    <div class="permission-label">更新权限</div>
                                    <el-switch
                                        v-model="shareForm.canUpdate"
                                        active-text="开启"
                                        inactive-text="关闭"
                                    />
                                </div>
                                <div class="permission-item">
                                    <div class="permission-label">删除权限</div>
                                    <el-switch
                                        v-model="shareForm.canDelete"
                                        active-text="开启"
                                        inactive-text="关闭"
                                    />
                                </div>
                            </div>
                        </el-form-item>
                    </el-form>

                    <div class="form-actions">
                        <el-button @click="handleShareCancel">取消</el-button>
                        <el-button type="primary" @click="handleShareConfirm"
                            >确定</el-button
                        >
                    </div>
                </div>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from "vue";
import {
    ElMessage,
    ElMessageBox,
    ElTable,
    FormInstance,
    FormRules,
} from "element-plus";
import { Loading } from "@element-plus/icons-vue";
import { http } from "@/plugins/axios";

interface ShareForm {
    sharedUsers: string[];
    canRead: boolean;
    canAdd: boolean;
    canUpdate: boolean;
    canDelete: boolean;
}

interface UserOption {
    label: string;
    value: string;
}

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
    sharedWithUserName?: string;
    sharedByUserName?: string;
}

interface Props {
    fileGroupId?: string | number;
    visible?: boolean;
    userStoreInfo?: any;
}

const props = withDefaults(defineProps<Props>(), {
    fileGroupId: "",
    visible: false,
    userStoreInfo: null,
});

const emit = defineEmits<{
    shareConfirm: [data: ShareForm];
    unshareConfirm: [];
    cancel: [];
}>();

// Tab 相关
const activeTab = ref("unshare");

// 共享表单相关
const shareFormRef = ref<FormInstance>();
const userOptions = ref<UserOption[]>([]);

const shareForm = reactive<ShareForm>({
    sharedUsers: [],
    canRead: true,
    canAdd: false,
    canUpdate: false,
    canDelete: false,
});

const shareFormRules: FormRules = {
    sharedUsers: [
        {
            required: true,
            message: "请选择至少一个共享用户",
            trigger: "change",
        },
    ],
};

// 取消共享相关
const tableRef = ref<InstanceType<typeof ElTable>>();
const isLoading = ref(false);
const shareList = ref<ShareItem[]>([]);
const selectedUsers = ref<ShareItem[]>([]);

// 用户搜索方法
const userRemoteMethod = (query: string) => {
    clearTimeout((window as any).remoteSearchTimer);
    (window as any).remoteSearchTimer = setTimeout(() => {
        http.request<any>({
            url: "/user/getUserPageList",
            method: "post",
            data: {
                name: query,
                pageNum: 1,
                pageSize: 20,
            },
        })
            .then((res) => {
                userOptions.value = res.data.records
                    .filter(
                        (user: any) =>
                            user.username !== props.userStoreInfo.user.username
                    )
                    .map((user: any) => ({
                        label: user.name,
                        value: user.username,
                    }));
            })
            .catch((err) => {
                console.log(err);
            });
    }, 500);
};

// 共享用户改变,获取用户当前的权限
const userChangeMethod = (value: string) => {
    http.request<any>({
        url: "/rag/file-group/shareList",
        method: "post",
        q_spinning: true,
        data: {
            fileGroupId: props.fileGroupId,
            sharedWith: value,
        },
    }).then((res) => {
        if (res.data.length > 0) {
            shareForm.canRead = res.data[0].canRead;
            shareForm.canAdd = res.data[0].canAdd;
            shareForm.canUpdate = res.data[0].canUpdate;
            shareForm.canDelete = res.data[0].canDelete;
        } else {
            shareForm.canRead = false;
            shareForm.canAdd = false;
            shareForm.canUpdate = false;
            shareForm.canDelete = false;
        }
    });
};

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

// 共享表单方法
const resetShareForm = (data: ShareForm | null) => {
    shareForm.sharedUsers = [];
    shareForm.canRead = false;
    shareForm.canAdd = false;
    shareForm.canUpdate = false;
    shareForm.canDelete = false;
};

const setShareFormData = (data: Partial<ShareForm>) => {
    Object.assign(shareForm, data);
};

const getShareFormData = (): ShareForm => {
    return { ...shareForm };
};

// 共享确定按钮处理
const handleShareConfirm = async () => {
    try {
        await shareFormRef.value?.validate();
        emit("shareConfirm", getShareFormData());
    } catch (error) {
        console.error("表单验证失败:", error);
    }
};

// 共享取消按钮处理
const handleShareCancel = () => {
    emit("cancel");
};

// 确认取消共享
const handleUnshareConfirm = async () => {
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
            await getShareList(); // 刷新列表
            emit("unshareConfirm");
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

// 取消共享取消操作
const handleUnshareCancel = () => {
    selectedUsers.value = []; // 清空选中的用户
    emit("cancel");
};

// 刷新列表
const refreshList = () => {
    getShareList();
};

// 监听 tab 切换，当切换到管理共享时刷新列表
watch(activeTab, (newTab) => {
    if (newTab === "unshare") {
        getShareList();
    }
});

// 暴露方法给父组件
defineExpose({
    resetShareForm,
    getShareList,
    setShareFormData,
    getShareFormData,
    refreshList,
});

onMounted(() => {
    nextTick(() => {
    });
});
</script>

<style scoped lang="scss">
.share-manage-container {
    .share-tabs {
        .tab-content {
            padding: 20px 0;
        }
    }

    .share-form {
        .permission-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 16px;

            .permission-item {
                display: flex;
                flex-direction: column;
                align-items: flex-start;
                justify-content: space-between;
                padding: 12px;
                border: 1px solid #e4e7ed;
                border-radius: 6px;
                background-color: #fafafa;

                .permission-label {
                    font-size: 14px;
                    color: #606266;
                    font-weight: 500;
                }
            }
        }
    }

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
}
</style>
