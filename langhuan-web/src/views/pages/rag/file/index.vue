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
                <template #content-documentNum="props">
                    <el-button
                        link
                        type="primary"
                        @click="openDocumentNum(props.row)"
                        >{{ props.row.documentNum }}
                    </el-button>
                </template>
                <template #content-buttonSlot="props">
                    <el-button
                        link
                        type="primary"
                        :disabled="!props.row.canUpdate"
                        @click="addAndChangeFormShowFun('change', props.row)"
                        >修改
                    </el-button>
                    <el-button
                        link
                        type="primary"
                        :disabled="!props.row.canDelete"
                        @click="addAndChangeFormShowFun('delete', props.row)"
                        >删除
                    </el-button>
                    <el-button
                        link
                        type="primary"
                        @click="
                            addAndChangeFormShowFun(
                                'fileRecallTesting',
                                props.row
                            )
                        "
                        >文件召回
                    </el-button>
                    <el-button
                        link
                        type="primary"
                        @click="
                            addAndChangeFormShowFun('fileExport', props.row)
                        "
                        >文件导出
                    </el-button>
                    <el-button
                        v-if="pageConfig.relation"
                        link
                        type="primary"
                        @click="addAndChangeFormShowFun('relation', props.row)"
                        >{{ pageConfig.relationBtnName }}
                    </el-button>
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

        <el-dialog
            v-model="relevancyVisible"
            :title="addAndChangeFormDialogTit"
            width="800"
        >
            <el-checkbox
                v-model="checkAll"
                :indeterminate="isIndeterminate"
                @change="handleCheckAllChange"
            >
                Check all
            </el-checkbox>
            <el-checkbox-group
                v-model="checkedRoles"
                @change="handleCheckedCitiesChange"
            >
                <el-checkbox
                    v-for="item in relation"
                    :key="item.relation_id"
                    :label="item.relation_id"
                    :value="item.relation_id"
                >
                    {{ item.relation_name }}
                </el-checkbox>
            </el-checkbox-group>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="relevancyShowFun('close')"
                        >取消</el-button
                    >
                    <el-button type="primary" @click="relevancyShowFun('save')">
                        确定
                    </el-button>
                </div>
            </template>
        </el-dialog>
        <el-dialog v-model="documentNumVisible" :title="'文档列表'" width="900">
            <div class="flex flex-col h-[70vh]">
                <el-form :model="documentQueryForm" size="small" inline>
                    <el-form-item label="内容查询">
                        <el-input
                            style="width: 500px"
                            v-model="documentQueryForm.content"
                            placeholder="输入查询内容"
                            clearable
                        />
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="handleDocumentQuery"
                            >查询</el-button
                        >
                    </el-form-item>
                </el-form>
                <div class="flex-1 overflow-y-scroll">
                    <div
                        v-for="(item, index) in documentNumData"
                        :key="index"
                        class="mb-4"
                    >
                        <div v-if="!item.isEditing">
                            <div>
                                <div style="white-space: pre-wrap">
                                    {{ item.content }}
                                </div>
                                <div class="float-right">
                                    <el-button
                                        type="primary"
                                        link
                                        :disabled="!nowRow.canUpdate"
                                        @click="
                                            documentHandle('edit', index, item)
                                        "
                                        >修改</el-button
                                    >
                                    <el-button
                                        type="primary"
                                        link
                                        :disabled="!nowRow.canDelete"
                                        @click="
                                            documentHandle(
                                                'delete',
                                                index,
                                                item
                                            )
                                        "
                                        >删除</el-button
                                    >
                                </div>
                            </div>
                        </div>
                        <div v-else>
                            <el-input
                                v-model="item.tempContent"
                                type="textarea"
                                :rows="7"
                                class="mb-2"
                            />
                            <div class="flex justify-end gap-2">
                                <el-button
                                    size="small"
                                    @click="
                                        documentHandle('cancel', index, item)
                                    "
                                    >取消</el-button
                                >
                                <el-button
                                    size="small"
                                    type="primary"
                                    @click="documentHandle('save', index, item)"
                                    >确定</el-button
                                >
                            </div>
                        </div>
                        <el-divider />
                    </div>
                </div>
                <el-pagination
                    v-model:current-page="documentPagination.pageNum"
                    class="mt-2"
                    v-model:page-size="documentPagination.pageSize"
                    :background="true"
                    :layout="'total, sizes, prev, pager, next, jumper'"
                    :page-sizes="[10, 20]"
                    :small="true"
                    :total="Number(documentPagination.total)"
                    @size-change="handleDocumentPageChange"
                    @current-change="handleDocumentPageChange"
                />
            </div>
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
import { getFileGroupOption } from "../addFile/addFileFormconfig";
import { useRouter } from "vue-router";
import useUserStore from "@/store/user";
const userStore = useUserStore();
const router = useRouter();
const formComRef = ref<FormDefineExpose>();
const tableComRef = ref<TableDefineExpose>();

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
    return http
        .request<any>({
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
            res.data[pageConfig.search_tableData_key] = res.data[
                pageConfig.search_tableData_key
            ].map((e: any, i: number) => {
                return {
                    ...e,
                    fileGroupName: fileGroupOption.find(
                        (ee: any) => ee.value === e.fileGroupId
                    )?.label,
                };
            });
            tableData.value = res.data[pageConfig.search_tableData_key];
            paginationConfig.value.total =
                res.data[pageConfig.search_paginationConfig_key];
        })
        .catch((err) => {
            console.log(err);
        });
};
let fileGroupOption: any = [];
nextTick(async () => {
    const httpGetFileGroupOption: any = await getFileGroupOption();
    fileGroupOption = httpGetFileGroupOption.data.map((e: any) => {
        return {
            label: e.groupName,
            value: String(e.id),
        };
    });
    await getUserPageList();
});

const addAndChangeFormComRef = ref<FormDefineExpose>();
let addAndChangeFormVisible = ref(false);
let addAndChangeFormDialogTit = ref("");
let relevancyVisible = ref(false);
let documentNumVisible = ref(false);
let documentNumData = ref<any>([]);
const documentQueryForm = reactive({
    content: "",
});
const documentPagination = reactive({
    pageNum: 1,
    pageSize: 20,
    total: 0,
});
const checkAll = ref(false);
const isIndeterminate = ref(true);
const checkedRoles = ref<string[]>([]);
const relation = ref<{ relation_name: string; relation_id: string }[]>([]);
let nowUser: any = null;
let nowRow: any = null;

const addAndChangeFormShowFun = async (t: string, d: any = null) => {
    if (t === "add") {
        router.push("/pages/rag/addRagDocuments");
    }
    if (t === "change") {
        nowRow = d;
        addAndChangeFormDialogTit.value = "修改";
        addAndChangeFormVisible.value = true;

        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm();
            addAndChangeFormComRef.value!.setFormOption([
                {
                    key: "fileGroupId",
                    option: fileGroupOption,
                },
            ]);
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
    if (t === "relation") {
        nowUser = d;
        relevancyVisible.value = true;
        checkedRoles.value = [];
        relation.value = [];
        await nextTick(async () => {
            await http
                .request<any>({
                    url: "/user/getUserRoles",
                    method: "post",
                    q_spinning: true,
                    q_contentType: "form",
                    data: {},
                })
                .then((res) => {
                    if (res.code === 200) {
                        relation.value = res.data;
                    }
                });
            await http
                .request<any>({
                    url: "/user/getUserRoles",
                    method: "post",
                    q_spinning: true,
                    q_contentType: "form",
                    data: {
                        id: d.id,
                    },
                })
                .then((res) => {
                    if (res.code === 200) {
                        checkedRoles.value = res.data.map(
                            (e: any) => e.relation_id
                        );
                    }
                });
            handleCheckedCitiesChange(checkedRoles.value);
        });
    }
    if (t === "save") {
        let url = "";
        // if (addAndChangeFormDialogTit.value === '新增') {
        //     url = pageConfig.addUrl
        // }
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
                    data: {
                        ...sd,
                        id: nowRow.id,
                    },
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
                Object.keys(rej).forEach((k) => {
                    rej[k].forEach((e: any) => {
                        ElMessage.warning(e.message);
                    });
                });
            });
    }
    if (t === "close") {
        addAndChangeFormDialogTit.value = "";
        addAndChangeFormVisible.value = false;
    }
    if (t === "fileRecallTesting") {
        router.push({
            path: "/pages/rag/recallTesting",
            query: {
                fileId: d.id,
                groupId: d.fileGroupId,
            },
        });
    }
    if (t === "fileExport") {
        http.request<any>({
            url: "/rag/file/generateDocumentStreamByFileId",
            method: "post",
            q_spinning: true,
            q_contentType: "form",
            responseType: "blob",
            data: {
                fileId: d.id,
            },
        }).then((res: any) => {
            const blob = new Blob([res]);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = d.fileName.includes(".")
                ? d.fileName.substring(0, d.fileName.lastIndexOf(".")) + ".txt"
                : d.fileName + ".txt";
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        });
    }
};

const handleCheckAllChange = (val: boolean | CheckboxValueType) => {
    checkedRoles.value = val ? relation.value.map((e) => e.relation_id) : [];
    isIndeterminate.value = false;
};
const handleCheckedCitiesChange = (value: string[] | CheckboxValueType[]) => {
    const checkedCount = value.length;
    checkAll.value = checkedCount === relation.value.length;
    isIndeterminate.value =
        checkedCount > 0 && checkedCount < relation.value.length;
};
const relevancyShowFun = (t: string, d: any = null) => {
    if (t === "save") {
        http.request<any>({
            url: "/user/relevancyRoles",
            method: "post",
            q_spinning: true,
            q_contentType: "form",
            data: {
                id: nowUser.id,
                roleIds: checkedRoles.value.join(","),
            },
        }).then((res) => {
            if (res.code === 200) {
                relevancyVisible.value = false;
                ElMessage.success("操作成功");
            }
        });
    }
    if (t === "close") {
        relevancyVisible.value = false;
    }
};
const openDocumentNum = (row: any) => {
    nowRow = JSON.parse(JSON.stringify(row));
    documentQueryForm.content = "";
    documentPagination.total = 0;
    documentPagination.pageNum = 1;
    documentPagination.pageSize = 10;
    fetchDocuments();
};

const fetchDocuments = () => {
    http.request<any>({
        url: "/rag/file/queryDocumentsByFileId",
        method: "post",
        q_spinning: true,
        q_contentType: "form",
        data: {
            fileId: nowRow.id,
            content: documentQueryForm.content,
            pageNum: documentPagination.pageNum,
            pageSize: documentPagination.pageSize,
        },
    }).then((res) => {
        documentNumVisible.value = true;
        nextTick(() => {
            documentNumData.value = res.data.list.map((e: any) => ({
                id: e.id,
                content: e.content,
                isEditing: false,
                tempContent: e.content,
            }));
            documentPagination.total = res.data.count;
        });
    });
};

const handleDocumentQuery = () => {
    documentPagination.pageNum = 1;
    fetchDocuments();
};

const handleDocumentPageChange = () => {
    fetchDocuments();
};
const documentHandle = (type: string, index: number, item: any) => {
    if (type === "edit") {
        documentNumData.value[index].isEditing = true;
        documentNumData.value[index].tempContent =
            documentNumData.value[index].content;
    }
    if (type === "delete") {
        ElMessageBox.confirm("确认删除?", "通知", {
            confirmButtonText: "确定",
            cancelButtonText: "返回",
            type: "warning",
        })
            .then(() => {
                http.request<any>({
                    url: "/rag/deleteDocumentText",
                    method: "post",
                    q_spinning: true,
                    q_contentType: "json",
                    data: {
                        ragFile: nowRow,
                        documentId: item.id,
                    },
                }).then((res) => {
                    if (res.code === 200) {
                        ElMessage.success(res.data);
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
    if (type === "cancel") {
        documentNumData.value[index].isEditing = false;
    }
    if (type === "save") {
        documentNumData.value[index].content =
            documentNumData.value[index].tempContent;

        http.request<any>({
            url: "/rag/changeDocumentText",
            method: "post",
            q_spinning: true,
            q_contentType: "json",
            data: {
                ragFile: nowRow,
                documentId: item.id,
                documents: item.tempContent,
            },
        }).then((res) => {
            if (res.code === 200) {
                documentNumData.value[index].isEditing = false;
                ElMessage.success(res.data);
            }
        });
    }
};
</script>

<style scoped></style>
