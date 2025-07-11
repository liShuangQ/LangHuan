<template>

    <div class="box-border p-3 bg-white">
        <div class="mb-2">
            <el-button @click="addAndChangeFormShowFun('add')">新增</el-button>
            <el-button type="primary" @click="refreshPrompt()">立即生效</el-button>
        </div>
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
        <div style="height: calc(100% - 120px)" class="mt-2">
            <ElementTableC ref="tableComRef" :paginationConfig="paginationConfig" :tableColumnConfig="tableColumnConfig"
                :tableConfig="tableConfig" :tableData="tableData" @handle="tableHandle">
                <template #content-buttonSlot="props">
                    <!-- :disabled="props.row.category.indexOf('system') !== -1" -->
                    <el-button link type="primary" @click="addAndChangeFormShowFun('change', props.row)">修改</el-button>
                    <el-button link type="primary" @click="addAndChangeFormShowFun('delete', props.row)">删除</el-button>
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

const addAndChangeFormComRef = ref<FormDefineExpose>();
let addAndChangeFormVisible = ref(false)
let addAndChangeFormDialogTit = ref("")
const addAndChangeFormShowFun = async (t: string, d: any = null) => {
    if (t === 'add') {
        addAndChangeFormDialogTit.value = '新增'
        addAndChangeFormVisible.value = true
        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm()
        })
    }
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

const refreshPrompt = () => {
    http.request<any>({
        url: '/prompts/usePrompt/refresh',
        method: 'post',
        q_spinning: true,
        data: {},
    }).then(res => {
        ElMessage.success(res.data)
    })
}

nextTick(() => {
    getUserPageList()
})
</script>

<style scoped></style>