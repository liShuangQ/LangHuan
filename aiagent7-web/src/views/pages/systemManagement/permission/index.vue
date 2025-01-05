<template>

    <div class="box-border p-3 bg-white">
        <div class="mb-2">
            <el-button type="primary" @click="addAndChangeFormShowFun('add')">新增</el-button>
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
        <div class="h-[600px] mt-2">
            <ElementTableC ref="tableComRef" :paginationConfig="paginationConfig" :tableColumnConfig="tableColumnConfig"
                :tableConfig="tableConfig" :tableData="tableData" @handle="tableHandle">
                <template #content-buttonSlot="props">
                    <el-button link type="primary" @click="addAndChangeFormShowFun('change', props.row)">修改</el-button>
                    <el-button link type="primary" @click="addAndChangeFormShowFun('delete', props.row)">删除</el-button>
                    <!--                    <div m="4">-->
                    <!--                        <p m="t-0 b-2">State: state</p>-->
                    <!--                        <p m="t-0 b-2">City: city</p>-->
                    <!--                    </div>-->
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
    FormConfig,
    FormDefineExpose,
    FormItemConfig,
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
};

const getUserPageList = () => {
    http.request<any>({
        url: '/permission/getPageList',
        method: 'post',
        q_spinning: true,
        data: {
            ...formComRef.value!.getFromValue(),
            pageNum: 1,
            pageSize: 10,
        },
    }).then(res => {
        tableData.value = res.data.records;
        paginationConfig.value.total = res.data.total;
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
const addAndChangeFormShowFun = (t: string, d: any = null) => {
    if (t === 'add') {
        addAndChangeFormDialogTit.value = '新增角色信息'
        addAndChangeFormVisible.value = true
        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm()
            addAndChangeFormComRef.value!.setFormOption([
                {
                    key: 'username',
                    disabled: false
                }
            ])
        })
    }
    if (t === 'change') {
        addAndChangeFormDialogTit.value = '修改角色信息'
        addAndChangeFormVisible.value = true
        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm()
            addAndChangeFormComRef.value!.setFormOption(Object.entries(d).map(([k, v]) => {
                return {
                    key: k,
                    value: String(v)
                }
            }))
            addAndChangeFormComRef.value!.setFormOption([
                {
                    key: 'username',
                    disabled: true
                },
                {
                    key: 'password',
                    value: ""
                }
            ])
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
                    url: "/permission/delete",
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
        if (addAndChangeFormDialogTit.value === '新增角色信息') {
            url = '/permission/add'
        }
        if (addAndChangeFormDialogTit.value === '修改角色信息') {
            url = '/permission/change'
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
                    } else {
                        ElMessage.error(res.message)
                    }
                })
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
    if (t === 'close') {
        addAndChangeFormDialogTit.value = ''
        addAndChangeFormVisible.value = false
    }
}
</script>

<style scoped></style>