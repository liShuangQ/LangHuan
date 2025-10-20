<template>
    <div class="box-border p-3 bg-white">
        <!-- 操作按钮区域 -->
        <div class="mb-4 flex gap-2">
            <el-button type="primary" @click="addAndChangeFormShowFun('add')">
                创建通知
            </el-button>
            <el-button type="warning" @click="batchArchive" :disabled="!selectedNotifications.length">
                批量归档
            </el-button>
            <el-button type="danger" @click="batchDelete" :disabled="!selectedNotifications.length">
                批量删除
            </el-button>
        </div>

        <!-- 查询表单 -->
        <div class="ml-1">
            <ElementFormC ref="formComRef" :formConfig="formConfig" :formItemConfig="formItemConfig"
                @handle="formHandle">
                <template #custom-button>
                    <div class="float-right">
                        <el-button type="primary" @click="getNotificationList">查询</el-button>
                        <el-button @click="formComRef!.resetForm()">重置</el-button>
                    </div>
                </template>
            </ElementFormC>
        </div>



        <!-- 通知列表表格 -->
        <div style="height: calc(100% - 160px)" class="mt-2">
            <ElementTableC ref="tableComRef" :paginationConfig="paginationConfig" :tableColumnConfig="tableColumnConfig"
                :tableConfig="tableConfig" :tableData="tableData" @handle="tableHandle">
                <template #content-buttonSlot="props">
                    <!-- <el-button v-if="!props.row.isRead && props.row.userId" link type="primary"
                        @click="markAsRead(props.row.id)">
                        标记已读
                    </el-button> -->
                    <el-button link type="warning" @click="archiveNotification(props.row.id)">
                        归档
                    </el-button>
                    <el-button link type="danger" @click="deleteNotification(props.row.id)">
                        删除
                    </el-button>
                </template>
            </ElementTableC>
        </div>

        <!-- 创建/编辑通知对话框 -->
        <el-dialog v-model="addAndChangeFormVisible" :title="addAndChangeFormDialogTit" width="800px">
            <ElementFormC ref="addAndChangeFormComRef" :formConfig="addAndChangeForm"
                :formItemConfig="addAndChangeFormItem" @handle="changeFormHandle">
            </ElementFormC>
            <template #footer>
                <el-button @click="addAndChangeFormShowFun('close')">取消</el-button>
                <el-button type="primary" @click="saveNotification">保存</el-button>
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
import { http } from "@/plugins/axios";
import { TableDefineExpose } from "@/components/globalComponents/ElementTableC/table-component";
import {
    FormDefineExpose,
} from "@/components/globalComponents/ElementFormC/form-component";
import { formConfig, formItemConfig } from "./formConfig";
import {
    paginationConfig,
    tableColumnConfig,
    tableConfig,
    tableData,
    formatNotificationLevel,
    formatNotificationType,
} from "./tableConfig";
import {
    addAndChangeForm,
    addAndChangeFormItem,
} from "./addAndChangeformConfig";
import dayjs from "dayjs";
import { ElMessageBox, ElMessage } from "element-plus";

const formComRef = ref<FormDefineExpose>();
const tableComRef = ref<TableDefineExpose>();
const addAndChangeFormComRef = ref<FormDefineExpose>();
let userOption: any = [];
// 选中的通知列表
const selectedNotifications = ref<number[]>([]);



// 对话框相关
let addAndChangeFormVisible = ref(false);
let addAndChangeFormDialogTit = ref("");
let addOrChange = ref("add");

const formHandle = (type: string, key: string, data: any, other: any) => {
    return;
};

const changeFormHandle = (type: string, key: string, data: any, other: any) => {
    return;
};

const tableHandle = (t: string, d: any, key: string) => {

    if (t === "handleCurrentChange" || t === "handleSizeChange") {
        getNotificationList();
    }
    if (t === "selection") {
        selectedNotifications.value = d.map((item: any) => item.id);
    }
};

// 获取通知列表
const getNotificationList = async () => {
    const formData = formComRef.value!.getFromValue();
    http
        .request<any>({
            url: "/notifications/admin/getAllNotifications",
            method: "post",
            q_spinning: true,
            q_contentType: "form",
            data: {
                ...formData,
                pageNum: paginationConfig.value.currentPage,
                pageSize: paginationConfig.value.pageSize,
            },
        })
        .then((res) => {
            if (res.code === 200) {
                res.data.records.forEach((e: any) => {
                    e.formatCreatedAt = dayjs(e.createdAt).format("YYYY-MM-DD HH:mm:ss");
                    e.formatExpiresAt = e.expiresAt
                        ? dayjs(e.expiresAt).format("YYYY-MM-DD HH:mm:ss")
                        : "永不过期";
                    e.userTypeText = e.userId ? "个人通知" : "全局通知";
                    e.levelColor = getLevelColor(e.notificationLevel);
                    e.statusText = getStatusText(e.isRead, e.isArchived);
                    // 格式化通知级别和类型为中文
                    e.notificationLevel = formatNotificationLevel(e.notificationLevel);
                    e.notificationType = formatNotificationType(e.notificationType);
                });
                tableData.value = res.data.records;
                paginationConfig.value.total = res.data.total;
            }
        })
        .catch((err) => {
            console.log(err);
            ElMessage.error("获取通知列表失败");
        });

};



// 获取通知级别颜色
const getLevelColor = (level: string) => {
    const colors = {
        critical: "#f56565",
        error: "#ed8936",
        warning: "#ecc94b",
        info: "#4299e1",
    };
    return colors[level as keyof typeof colors] || "#718096";
};

// 获取状态文本
const getStatusText = (isRead: boolean, isArchived: boolean) => {
    if (isArchived) return "已归档";
    if (isRead) return "已读";
    return "未读";
};

// 标记为已读
const markAsRead = (id: number) => {
    http
        .request<any>({
            url: "/notifications/mark-read",
            method: "post",
            q_spinning: true,
            q_contentType: "form",
            data: {
                id: id,
            },
        })
        .then((res) => {
            if (res.code === 200) {
                ElMessage.success("标记已读成功");
                getNotificationList();
            } else {
                ElMessage.error(res.message || "标记已读失败");
            }
        })
        .catch((err) => {
            console.log(err);
            ElMessage.error("标记已读失败");
        });
};

// 归档通知
const archiveNotification = (id: number) => {
    http
        .request<any>({
            url: "/notifications/archive",
            method: "post",
            q_spinning: true,
            q_contentType: "form",
            data: {
                ids: id.toString(),
            },
        })
        .then((res) => {
            if (res.code === 200) {
                ElMessage.success("归档成功");
                getNotificationList();
            } else {
                ElMessage.error(res.message || "归档失败");
            }
        })
        .catch((err) => {
            console.log(err);
            ElMessage.error("归档失败");
        });
};

// 删除通知
const deleteNotification = (id: number) => {
    ElMessageBox.confirm("确认删除这条通知？", "确认删除", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
    })
        .then(() => {
            http
                .request<any>({
                    url: "/notifications/delete",
                    method: "post",
                    q_spinning: true,
                    q_contentType: "form",
                    data: {
                        ids: id.toString(),
                    },
                })
                .then((res) => {
                    if (res.code === 200) {
                        ElMessage.success("删除成功");
                        getNotificationList();
                    } else {
                        ElMessage.error(res.message || "删除失败");
                    }
                })
                .catch((err) => {
                    console.log(err);
                    ElMessage.error("删除失败");
                });
        })
        .catch(() => {
            ElMessage.info("已取消删除");
        });
};

// 批量归档
const batchArchive = () => {
    if (selectedNotifications.value.length === 0) {
        ElMessage.warning("请选择要归档的通知");
        return;
    }

    ElMessageBox.confirm(
        `确认归档选中的 ${selectedNotifications.value.length} 条通知？`,
        "批量归档",
        {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        }
    )
        .then(() => {
            http
                .request<any>({
                    url: "/notifications/archive",
                    method: "post",
                    q_spinning: true,
                    q_contentType: "form",
                    data: {
                        ids: selectedNotifications.value.join(","),
                    },
                })
                .then((res) => {
                    if (res.code === 200) {
                        ElMessage.success("批量归档成功");
                        selectedNotifications.value = [];
                        getNotificationList();
                    } else {
                        ElMessage.error(res.message || "批量归档失败");
                    }
                })
                .catch((err) => {
                    console.log(err);
                    ElMessage.error("批量归档失败");
                });
        })
        .catch(() => {
            ElMessage.info("已取消归档");
        });
};

// 批量删除
const batchDelete = () => {
    if (selectedNotifications.value.length === 0) {
        ElMessage.warning("请选择要删除的通知");
        return;
    }

    ElMessageBox.confirm(
        `确认删除选中的 ${selectedNotifications.value.length} 条通知？删除后无法恢复！`,
        "批量删除",
        {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        }
    )
        .then(() => {
            http
                .request<any>({
                    url: "/notifications/delete",
                    method: "post",
                    q_spinning: true,
                    q_contentType: "form",
                    data: {
                        ids: selectedNotifications.value.join(","),
                    },
                })
                .then((res) => {
                    if (res.code === 200) {
                        ElMessage.success("批量删除成功");
                        selectedNotifications.value = [];
                        getNotificationList();
                    } else {
                        ElMessage.error(res.message || "批量删除失败");
                    }
                })
                .catch((err) => {
                    console.log(err);
                    ElMessage.error("批量删除失败");
                });
        })
        .catch(() => {
            ElMessage.info("已取消删除");
        });
};

// 显示创建/编辑对话框
const addAndChangeFormShowFun = (t: string, d: any = null) => {
    if (t === "add") {
        addAndChangeFormDialogTit.value = "创建通知";
        addOrChange.value = "add";
        addAndChangeFormVisible.value = true;
        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm();
            addAndChangeFormComRef.value!.setFormOption([
                {
                    key: "userIds",
                    option: userOption
                }
            ]);
        });
    }
    if (t === "close") {
        addAndChangeFormVisible.value = false;
        addAndChangeFormDialogTit.value = "";
    }
};

// 保存通知
const saveNotification = () => {
    addAndChangeFormComRef.value!.submitForm().then(res => {
        const formData: any = addAndChangeFormComRef.value!.getFromValue();

        http
            .request<any>({
                url: "/notifications/create",
                method: "post",
                q_spinning: true,
                q_contentType: "json",
                data: {
                    title: formData.title,
                    content: formData.content,
                    notificationLevel: formData.notificationLevel,
                    notificationType: formData.notificationType,
                    referenceId: formData.referenceId,
                    referenceType: formData.referenceType,
                    expiresAt: formData.expiresAt,
                },
                params: {
                    userIds: (formData.userIds === '' ? [] : formData.userIds).join(","),
                },
            })
            .then((res) => {
                if (res.code === 200) {
                    ElMessage.success(res.data);
                    addAndChangeFormVisible.value = false;
                    getNotificationList();
                } else {
                    ElMessage.error(res.message || "创建通知失败");
                }
            })
            .catch((err) => {
                console.log(err);
                ElMessage.error("创建通知失败");
            });
    })


};

// 获取用户信息
const getUserInfo = async () => {
    const userRes = await http.request<any>({
        url: "/user/getUserPageList",
        method: "post",
        q_spinning: true,
        data: {
            pageNum: 1,
            pageSize: 100000000,
        },
    });
    return userRes.data.records.map((e: any) => {
        return {
            label: e.name,
            value: e.username,
        };
    });
};

// 初始化数据
nextTick(async () => {
    userOption = await getUserInfo();
    getNotificationList();
});
</script>

<style scoped>
.notification-level-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: bold;
    color: white;
}
</style>