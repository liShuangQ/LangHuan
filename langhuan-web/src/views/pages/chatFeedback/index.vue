<template>

    <div class="box-border p-3 bg-white">
        <div class="ml-1">
            <ElementFormC ref="formComRef" :formConfig="formConfig" :formItemConfig="formItemConfig"
                @handle="formHandle">
                <template #custom-button>
                    <div class="float-right">
                        <el-button type="primary" @click="getUserPageList">查询</el-button>
                        <el-button @click="formComRef!.resetForm()">重置</el-button>
                    </div>
                </template>
            </ElementFormC>
        </div>
        <div style="height: calc(100% - 70px)" class="mt-2">
            <ElementTableC ref="tableComRef" :paginationConfig="paginationConfig" :tableColumnConfig="tableColumnConfig"
                :tableConfig="tableConfig" :tableData="tableData" @handle="tableHandle">
                <template #content-knowledgeBaseIds="props">
                    <el-button link type="primary" @click="openDocumentIds(props.row)">知识库片段
                    </el-button>
                </template>
            </ElementTableC>
        </div>


        <el-dialog v-model="addAndChangeFormVisible" :title="addAndChangeFormDialogTit" width="800">
            <ElementFormC ref="addAndChangeFormComRef" :formConfig="addAndChangeFormConfig"
                :formItemConfig="addAndChangeFormItemConfig" @handle="addAndChangeFormHandle">
            </ElementFormC>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="addAndChangeFormShowFun('close')">取消</el-button>
                    <el-button type="primary" @click="addAndChangeFormShowFun('save')">
                        确定
                    </el-button>
                </div>
            </template>
        </el-dialog>

        <el-dialog v-model="documentIdsVisible" :title="'文档列表(修改后文档将自动归档)'" width="900">
            <div class="h-[70vh] overflow-y-scroll">
                <div v-for="(item, index) in documentIdsData" :key="index" class="mb-4">
                    <div v-if="!item.isEditing">
                        <div>
                            <div style="white-space: pre-wrap">{{ item.content }}</div>
                            <div class=" float-right">
                                <el-button type="primary" link
                                    @click="documentHandle('edit', index, item)">修改</el-button>
                            </div>
                        </div>
                    </div>
                    <div v-else>
                        <el-input v-model="item.tempContent" type="textarea" :rows="7" class="mb-2" />
                        <div class="flex justify-end gap-2">
                            <el-button size="small" @click="documentHandle('cancel', index, item)">取消</el-button>
                            <el-button size="small" type="primary"
                                @click="documentHandle('save', index, item)">确定</el-button>
                        </div>
                    </div>
                    <el-divider />
                </div>
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
import {
    FormDefineExpose,
} from "@/components/globalComponents/ElementFormC/form-component";
import { formConfig, formItemConfig } from "./formConfig";
import {
    paginationConfig,
    tableColumnConfig,
    tableConfig,
    tableData,
} from "./tableConfig";
import {
    addAndChangeFormConfig,
    addAndChangeFormItemConfig
} from "./addAndChangeformConfig";
import dayjs from "dayjs";
import { CheckboxValueType, ElMessage, ElMessageBox } from "element-plus";
import pageConfig from "./pageConfig";
import { nextTick, ref } from "vue";
const formComRef = ref<FormDefineExpose>();
const tableComRef = ref<TableDefineExpose>();
let documentIdsVisible = ref(false)
let documentIdsData = ref<any[]>([]);
const formHandle = (type: string, key: string, data: any, other: any) => {
    console.log(type, key, data, other);
};
const addAndChangeFormHandle = (type: string, key: string, data: any, other: any) => {
    console.log(type, key, data, other);
};

const tableHandle = (t: string, d: any, key: string) => {
    console.log("tableHandle:::", t, d, key);
    if (t === 'handleCurrentChange' || t === 'handleSizeChange') {
        getUserPageList()
    }
};

const getUserPageList = () => {
    http.request<any>({
        url: pageConfig.searchUrl,
        method: 'post',
        q_spinning: true,
        data: {
            ...formComRef.value!.getFromValue(),
            pageNum: paginationConfig.value.currentPage,
            pageSize: paginationConfig.value.pageSize,
        },
    }).then(res => {
        if (pageConfig.search_dayTransformation && pageConfig.search_dayTransformation.length >= 0) {
            res.data.records.forEach((e: any) => {
                pageConfig.search_dayTransformation.forEach((ee: string) => {
                    e[ee] = dayjs(e[ee]).format('YYYY-MM-DD HH:mm:ss')
                })
            })
        }
        tableData.value = res.data[pageConfig.search_tableData_key];
        paginationConfig.value.total = res.data[pageConfig.search_paginationConfig_key];
    }).catch(err => {
        console.log(err)
    })
};
nextTick(() => {
    getUserPageList()
})

const addAndChangeFormComRef = ref<FormDefineExpose>();
let addAndChangeFormVisible = ref(false)
let addAndChangeFormDialogTit = ref("")
let relevancyVisible = ref(false)
const checkAll = ref(false)
const isIndeterminate = ref(true)
const checkedRoles = ref<string[]>([])
const addAndChangeFormShowFun = async (t: string, d: any = null) => {
    if (t === 'change') {
        addAndChangeFormDialogTit.value = '修改'
        addAndChangeFormVisible.value = true
        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm()
            addAndChangeFormComRef.value!.setFormOption(Object.entries(d).map(([k, v]) => {
                return {
                    key: k,
                    value: String(v)
                }
            }))
        })
    }
    if (t === 'delete') {
        ElMessageBox.confirm(
            '确认删除?',
            '通知',
            {
                confirmButtonText: '确定',
                cancelButtonText: '返回',
                type: 'warning',
            }
        )
            .then(() => {
                http.request<any>({
                    url: pageConfig.deleteUrl,
                    method: 'post',
                    q_spinning: true,
                    q_contentType: 'form',
                    data: {
                        id: d.id
                    },
                }).then(res => {
                    if (res.code === 200) {
                        ElMessage.success('操作成功')
                        getUserPageList()
                    }
                })
            })
            .catch(() => {
                ElMessage({
                    type: 'info',
                    message: '取消删除',
                })
            })
    }
    if (t === 'save') {
        let url = ''
        if (addAndChangeFormDialogTit.value === '新增') {
            url = pageConfig.addUrl
        }
        if (addAndChangeFormDialogTit.value === '修改') {
            url = pageConfig.updataUrl
        }
        addAndChangeFormComRef
            .value!.submitForm()
            .then((res) => {
                let sd: any = addAndChangeFormComRef.value!.getFromValue()
                http.request<any>({
                    url: url,
                    method: 'post',
                    q_spinning: true,
                    q_contentType: 'json',
                    data: sd,
                }).then(res => {
                    if (res.code === 200) {
                        ElMessage.success('操作成功')
                        addAndChangeFormVisible.value = false
                        getUserPageList()
                    }
                })
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
    if (t === 'close') {
        addAndChangeFormDialogTit.value = ''
        addAndChangeFormVisible.value = false
    }
}
const openDocumentIds = (row: any) => {
    http.request<any>({
        url: '/chatFeedback/queryDocumentsByIds',
        method: 'post',
        q_spinning: true,
        q_contentType: 'form',
        data: {
            fileIds: row.knowledgeBaseIds,
        },
    }).then(res => {
        documentIdsVisible.value = true
        nextTick(() => {
            documentIdsData.value = res.data.map((e: any) => ({
                id: e.id,
                content: e.content,
                isEditing: false,
                tempContent: e.content
            }))
        })
    })
}
const documentHandle = (type: string, index: number, item: any) => {
    if (type === 'edit') {
        documentIdsData.value[index].isEditing = true;
        documentIdsData.value[index].tempContent = documentIdsData.value[index].content;
    }
    if (type === 'cancel') {
        documentIdsData.value[index].isEditing = false;
    }
    if (type === 'save') {
        documentIdsData.value[index].content = documentIdsData.value[index].tempContent;

        http.request<any>({
            url: '/chatFeedback/changeDocumentTextByString',
            method: 'post',
            q_spinning: true,
            data: {
                documentId: item.id,
                document: item.tempContent
            },
        }).then(res => {
            if (res.code === 200) {
                documentIdsData.value[index].isEditing = false;
                ElMessage.success(res.data)
            }
        })
    }
}


</script>

<style scoped></style>