<template>
    <div class="box-border p-3 bg-white">
        <div class="mb-2">
            <el-button type="primary" @click="addAndChangeFormShowFun('add')"
                >新增</el-button
            >
        </div>
        <div class="ml-1">
            <ElementFormC
                ref="formComRef"
                :formConfig="formConfig"
                :formItemConfig="formItemConfig"
                @handle="formHandle"
            >
                <template #custom-button>
                    <div class="float-right">
                        <el-button type="primary" @click="getUserPageList"
                            >查询</el-button
                        >
                        <el-button @click="formComRef!.resetForm()"
                            >重置</el-button
                        >
                    </div>
                </template>
            </ElementFormC>
        </div>
        <div style="height: calc(100% - 100px)" class="mt-2">
            <ElementTableC
                ref="tableComRef"
                :paginationConfig="paginationConfig"
                :tableColumnConfig="tableColumnConfig"
                :tableConfig="tableConfig"
                :tableData="tableData"
                @handle="tableHandle"
            >
                <template #content-visibility="props">
                    <div>
                        {{
                            props.row.visibility === "public" ? "公开" : "私有"
                        }}
                    </div>
                </template>
                <template #content-buttonSlot="props">
                    <el-button
                        link
                        type="primary"
                        :disabled="
                            !userStore.getAdminAndSelf(props.row.createdBy)
                        "
                        @click="addAndChangeFormShowFun('change', props.row)"
                        >修改</el-button
                    >
                    <el-button
                        link
                        type="primary"
                        :disabled="
                            !userStore.getAdminAndSelf(props.row.createdBy)
                        "
                        @click="addAndChangeFormShowFun('delete', props.row)"
                        >删除</el-button
                    >
                    <el-button
                        link
                        type="primary"
                        :disabled="
                            !userStore.getAdminAndSelf(props.row.createdBy) ||
                            props.row.visibility === 'public'
                        "
                        @click="addAndChangeFormShowFun('share', props.row)"
                        >共享</el-button
                    >
                    <el-button
                        link
                        type="primary"
                        :disabled="
                            !userStore.getAdminAndSelf(props.row.createdBy) ||
                            props.row.visibility === 'public'
                        "
                        @click="addAndChangeFormShowFun('unshare', props.row)"
                        >共享信息</el-button
                    >
                    <el-button
                        v-if="pageConfig.relation"
                        link
                        type="primary"
                        @click="addAndChangeFormShowFun('relation', props.row)"
                        >{{ pageConfig.relationBtnName }}</el-button
                    >
                </template>
            </ElementTableC>
        </div>

        <el-dialog
            v-model="addAndChangeFormVisible"
            :title="addAndChangeFormDialogTit"
            width="800"
        >
            <ElementFormC
                ref="addAndChangeFormComRef"
                :formConfig="addAndChangeFormConfig"
                :formItemConfig="addAndChangeFormItemConfig"
                @handle="addAndChangeFormHandle"
            >
            </ElementFormC>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="addAndChangeFormShowFun('close')"
                        >取消</el-button
                    >
                    <el-button
                        type="primary"
                        @click="addAndChangeFormShowFun('save')"
                    >
                        确定
                    </el-button>
                </div>
            </template>
        </el-dialog>

        <el-dialog v-model="shareDialogVisible" title="共享配置" width="600">
            <ShareConfig
                ref="shareConfigRef"
                :file-group-id="currentFileGroupId"
                :original-data="currentFileGroupData"
                :userStoreInfo="toRaw(userStore.info)"
                @confirm="handleShareConfirm"
                @cancel="handleShareCancel"
            />
        </el-dialog>

        <el-dialog v-model="unshareDialogVisible" title="共享信息" width="900">
            <UnshareConfig
                ref="unshareConfigRef"
                :file-group-id="currentFileGroupId"
                @confirm="handleUnshareConfirm"
                @cancel="handleUnshareCancel"
            />
        </el-dialog>
    </div>
</template>
<script lang="ts">
export default {
    auto: true,
};
</script>
<script setup lang="ts">
import { toRaw } from "vue";
import { http } from "@/plugins/axios";
import { TableDefineExpose } from "@/components/globalComponents/ElementTableC/table-component";
import { FormDefineExpose } from "@/components/globalComponents/ElementFormC/form-component";
import { formConfig, formItemConfig } from "./formConfig";
import {
    paginationConfig,
    tableColumnConfig,
    tableConfig,
    tableData,
} from "./tableConfig";
import {
    addAndChangeFormConfig,
    addAndChangeFormItemConfig,
} from "./addAndChangeformConfig";
import dayjs from "dayjs";
import { CheckboxValueType, ElMessageBox } from "element-plus";
import pageConfig from "./pageConfig";
import useUserStore from "@/store/user";
import ShareConfig from "./share.vue";
import UnshareConfig from "./unshare.vue";

const formComRef = ref<FormDefineExpose>();
const tableComRef = ref<TableDefineExpose>();
const shareConfigRef = ref<InstanceType<typeof ShareConfig>>();
const unshareConfigRef = ref<InstanceType<typeof UnshareConfig>>();
const userStore = useUserStore();

// 共享配置相关状态
const shareDialogVisible = ref(false);
const unshareDialogVisible = ref(false);
const currentFileGroupId = ref<string | number>("");
const currentFileGroupData = ref<any>({});
const formHandle = (type: string, key: string, data: any, other: any) => {
    console.log(type, key, data, other);
};
const addAndChangeFormHandle = (
    type: string,
    key: string,
    data: any,
    other: any
) => {
    console.log(type, key, data, other);
};

const tableHandle = (t: string, d: any, key: string) => {
    console.log("tableHandle:::", t, d, key);
    if (t === "handleCurrentChange" || t === "handleSizeChange") {
        getUserPageList();
    }
};

const getUserPageList = () => {
    http.request<any>({
        url: pageConfig.searchUrl,
        method: "post",
        q_spinning: true,
        data: {
            ...formComRef.value!.getFromValue(),
            pageNum: paginationConfig.value.currentPage,
            pageSize: paginationConfig.value.pageSize,
        },
    })
        .then((res) => {
            if (
                pageConfig.search_dayTransformation &&
                pageConfig.search_dayTransformation.length >= 0
            ) {
                res.data.records.forEach((e: any) => {
                    pageConfig.search_dayTransformation.forEach(
                        (ee: string) => {
                            e[ee] = dayjs(e[ee]).format("YYYY-MM-DD HH:mm:ss");
                        }
                    );
                });
            }
            tableData.value = res.data[pageConfig.search_tableData_key];
            paginationConfig.value.total =
                res.data[pageConfig.search_paginationConfig_key];
        })
        .catch((err) => {
            console.log(err);
        });
};
nextTick(() => {
    getUserPageList();
});

// 处理共享配置确认
const handleShareConfirm = (shareData: any) => {
    // 将共享用户数组转换为逗号分隔的字符串
    // const sharedWith = shareData.sharedUsers.join(",");
    const sharedWith = shareData.sharedUsers;

    http.request<any>({
        url: "/rag/file-group/share",
        method: "post",
        q_spinning: true,
        q_contentType: "form",
        data: {
            fileGroupId: currentFileGroupId.value,
            sharedWith: sharedWith,
            canRead: shareData.canRead,
            canAdd: shareData.canAdd,
            canUpdate: shareData.canUpdate,
            canDelete: shareData.canDelete,
        },
    })
        .then((res) => {
            if (res.code === 200) {
                ElMessage.success("共享配置保存成功");
                shareDialogVisible.value = false;
                getUserPageList();
            }
        })
        .catch((err) => {
            console.error("共享配置保存失败:", err);
            ElMessage.error("共享配置保存失败");
        });
};

// 处理共享配置取消
const handleShareCancel = () => {
    shareDialogVisible.value = false;
};

// 处理共享信息确认
const handleUnshareConfirm = () => {
    unshareDialogVisible.value = false;
    getUserPageList(); // 刷新列表
};

// 处理共享信息取消
const handleUnshareCancel = () => {
    unshareDialogVisible.value = false;
};

const addAndChangeFormComRef = ref<FormDefineExpose>();
let addAndChangeFormVisible = ref(false);
let addAndChangeFormDialogTit = ref("");

const addAndChangeFormShowFun = async (t: string, d: any = null) => {
    if (t === "add") {
        addAndChangeFormDialogTit.value = "新增";
        addAndChangeFormVisible.value = true;
        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm();
        });
    }
    if (t === "change") {
        addAndChangeFormDialogTit.value = "修改";
        addAndChangeFormVisible.value = true;
        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm();
            addAndChangeFormComRef.value!.setFormOption(
                Object.entries(d).map(([k, v]) => {
                    return {
                        key: k,
                        value: String(v),
                    };
                })
            );
        });
    }
    if (t === "delete") {
        ElMessageBox.confirm("确认删除?", "通知", {
            confirmButtonText: "确定",
            cancelButtonText: "返回",
            type: "warning",
        })
            .then(() => {
                http.request<any>({
                    url: pageConfig.deleteUrl,
                    method: "post",
                    q_spinning: true,
                    q_contentType: "form",
                    data: {
                        id: d.id,
                    },
                }).then((res) => {
                    if (res.code === 200) {
                        ElMessage.success("操作成功");
                        getUserPageList();
                    }
                });
            })
            .catch(() => {
                ElMessage({
                    type: "info",
                    message: "取消删除",
                });
            });
    }
    if (t === "save") {
        let url = "";
        if (addAndChangeFormDialogTit.value === "新增") {
            url = pageConfig.addUrl;
        }
        if (addAndChangeFormDialogTit.value === "修改") {
            url = pageConfig.updataUrl;
        }
        addAndChangeFormComRef
            .value!.submitForm()
            .then((res) => {
                let sd: any = addAndChangeFormComRef.value!.getFromValue();
                http.request<any>({
                    url: url,
                    method: "post",
                    q_spinning: true,
                    q_contentType: "json",
                    data: sd,
                }).then((res) => {
                    if (res.code === 200) {
                        ElMessage.success("操作成功");
                        addAndChangeFormVisible.value = false;
                        getUserPageList();
                    }
                });
            })
            .catch((rej: any) => {
                console.log(rej, "失败");
                // Object.keys(rej).forEach((k) => {
                //     rej[k].forEach((e: any) => {
                //         ElMessage.warning(e.message);
                //     });
                // });
            });
    }
    if (t === "close") {
        addAndChangeFormDialogTit.value = "";
        addAndChangeFormVisible.value = false;
    }
    if (t === "share") {
        currentFileGroupId.value = d.id;
        currentFileGroupData.value = d;
        shareDialogVisible.value = true;
        nextTick(() => {
            shareConfigRef.value!.resetForm({
                canRead: d.canRead || false,
                canAdd: d.canAdd || false,
                canUpdate: d.canUpdate || false,
                canDelete: d.canDelete || false,
            } as any);
        });
    }

    if (t === "unshare") {
        currentFileGroupId.value = d.id;
        unshareDialogVisible.value = true;
        nextTick(() => {
            unshareConfigRef.value?.refreshList();
        });
    }
};
</script>

<style scoped></style>
