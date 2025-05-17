<template>

    <div class="box-border p-3 bg-white">
        <div class="mb-2">
            <el-button type="primary" @click="addAndChangeFormShowFun('add')">注册</el-button>
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
                    <el-button link type="primary" @click="addAndChangeFormShowFun('change', props.row)">修改</el-button>
                    <el-button link type="primary" @click="addAndChangeFormShowFun('delete', props.row)">删除</el-button>
                    <el-button link type="primary" @click="addAndChangeFormShowFun('role', props.row)">关联角色</el-button>
                </template>
            </ElementTableC>
        </div>


        <el-dialog v-model="addAndChangeFormVisible" :title="addAndChangeFormDialogTit" width="800">
            <signUp ref="signUpRef" @handle="signUpHandle"></signUp>
        </el-dialog>


        <el-dialog v-model="relevancyVisible" :title="addAndChangeFormDialogTit" width="800">
            <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">
                Check all
            </el-checkbox>
            <el-checkbox-group v-model="checkedRoles" @change="handleCheckedCitiesChange">
                <el-checkbox v-for="item in roles" :key="item.role_id" :label="item.role_id" :value="item.role_id">
                    {{ item.role_name }}
                </el-checkbox>
            </el-checkbox-group>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="relevancyShowFun('close')">取消</el-button>
                    <el-button type="primary" @click="relevancyShowFun('save')">
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
import dayjs from "dayjs";
import { CheckboxValueType, ElMessageBox } from "element-plus";
import signUp from "./signUp.vue";
const formComRef = ref<FormDefineExpose>();
const tableComRef = ref<TableDefineExpose>();
const signUpRef = ref<any>();

const formHandle = (type: string, key: string, data: any, other: any) => {
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
        url: '/user/getUserPageList',
        method: 'post',
        q_spinning: true,
        data: {
            ...formComRef.value!.getFromValue(),
            pageNum: paginationConfig.value.currentPage,
            pageSize: paginationConfig.value.pageSize,
        },
    }).then(res => {
        res.data.records.forEach((e: any) => {
            e.formatCreationTime = dayjs(e.creationTime).format('YYYY-MM-DD HH:mm:ss')
            e.formatLastLoginTime = dayjs(e.lastLoginTime).format('YYYY-MM-DD HH:mm:ss')
        })
        tableData.value = res.data.records;
        paginationConfig.value.total = res.data.total;
    }).catch(err => {
        console.log(err)
    })
};
nextTick(() => {
    getUserPageList()
})

let addAndChangeFormVisible = ref(false)
let addAndChangeFormDialogTit = ref("")
let relevancyVisible = ref(false)
const checkAll = ref(false)
const isIndeterminate = ref(true)
const checkedRoles = ref<string[]>([])
const roles = ref<{ role_name: string, role_id: string }[]>([])
let nowUser: any = null;
const signUpHandle = (t: string, d: any = null) => {
    if (t === 'saveEnd') {
        addAndChangeFormVisible.value = false
        getUserPageList()
    }
    if (t === 'close') {
        addAndChangeFormDialogTit.value = ''
        addAndChangeFormVisible.value = false
    }
}
const addAndChangeFormShowFun = async (t: string, d: any = null) => {
    if (t === 'add') {
        addAndChangeFormDialogTit.value = '新增用户信息'
        addAndChangeFormVisible.value = true
        nextTick(() => {
            signUpRef.value!.handleFun('add', d)
        })

    }
    if (t === 'change') {
        addAndChangeFormDialogTit.value = '修改用户信息'
        addAndChangeFormVisible.value = true
        nextTick(() => {
            signUpRef.value!.handleFun('change', d)
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
                    url: "/user/delete",
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
    if (t === 'role') {
        nowUser = d
        relevancyVisible.value = true
        checkedRoles.value = []
        roles.value = []
        await nextTick(async () => {
            await http.request<any>({
                url: '/user/getUserRoles',
                method: 'post',
                q_spinning: true,
                q_contentType: 'form',
                data: {},
            }).then(res => {
                if (res.code === 200) {
                    roles.value = res.data
                }
            })
            await http.request<any>({
                url: '/user/getUserRoles',
                method: 'post',
                q_spinning: true,
                q_contentType: 'form',
                data: {
                    id: d.id
                },
            }).then(res => {
                if (res.code === 200) {
                    checkedRoles.value = res.data.map((e: any) => e.role_id)
                }
            })
            handleCheckedCitiesChange(checkedRoles.value)
        })
    }
}

const handleCheckAllChange = (val: boolean | CheckboxValueType) => {
    checkedRoles.value = val ? roles.value.map(e => e.role_id) : []
    isIndeterminate.value = false
}
const handleCheckedCitiesChange = (value: string[] | CheckboxValueType[]) => {
    const checkedCount = value.length
    checkAll.value = checkedCount === roles.value.length
    isIndeterminate.value = checkedCount > 0 && checkedCount < roles.value.length
}
const relevancyShowFun = (t: string, d: any = null) => {
    if (t === 'save') {
        http.request<any>({
            url: '/user/relevancyRoles',
            method: 'post',
            q_spinning: true,
            q_contentType: 'form',
            data: {
                id: nowUser.id,
                roleIds: checkedRoles.value.join(',')
            },
        }).then(res => {
            if (res.code === 200) {
                relevancyVisible.value = false
                ElMessage.success("操作成功")
            }
        })

    }
    if (t === 'close') {
        relevancyVisible.value = false
    }
}
</script>

<style scoped></style>