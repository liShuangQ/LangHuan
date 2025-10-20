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
        <div style="height: calc(100% - 100px)" class="mt-2">
            <ElementTableC ref="tableComRef" :paginationConfig="paginationConfig" :tableColumnConfig="tableColumnConfig"
                :tableConfig="tableConfig" :tableData="tableData" @handle="tableHandle">
                <template #content-buttonSlot="props">
                    <el-button link type="primary" @click="addAndChangeFormShowFun('change', props.row)">修改</el-button>
                    <el-button link type="primary" @click="addAndChangeFormShowFun('delete', props.row)">删除</el-button>
                    <el-button link type="primary"
                        @click="addAndChangeFormShowFun('permission', props.row)">关联权限</el-button>
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

        <el-dialog v-model="relevancyVisible" :title="addAndChangeFormDialogTit" width="800">
            <div v-for="permissions in allPermissions" :key="permissions.parent">
                <div class=" text-base"> {{ permissions.parent }}
                    <el-button type="text" @click="allPermissionsCheckAll(permissions)">
                        {{ '切换全选' }}
                    </el-button>
                </div>

                <el-checkbox-group v-model="permissions.checkedPermissions">
                    <el-checkbox v-for="item in permissions.children" :key="item.permission_id"
                        :label="item.permission_id" :value="item.permission_id">
                        {{ item.permission_name }}
                    </el-checkbox>
                </el-checkbox-group>
            </div>
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
import { CheckboxValueType, ElMessageBox } from "element-plus";
import {
    addAndChangeFormConfig,
    addAndChangeFormItemConfig
} from "./addAndChangeformConfig";
import dayjs from "dayjs";

const formComRef = ref<FormDefineExpose>();
const tableComRef = ref<TableDefineExpose>();

const formHandle = (type: string, key: string, data: any, other: any) => {
    return;
};
const addAndChangeFormHandle = (type: string, key: string, data: any, other: any) => {
    return;
};

const tableHandle = (t: string, d: any, key: string) => {

    if (t === 'handleCurrentChange' || t === 'handleSizeChange') {
        getUserPageList()
    }
};

const getUserPageList = () => {
    http.request<any>({
        url: '/role/getPageList',
        method: 'post',
        q_spinning: true,
        data: {
            ...formComRef.value!.getFromValue(),
            pageNum: paginationConfig.value.currentPage,
            pageSize: paginationConfig.value.pageSize,
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
let relevancyVisible = ref(false)
const allPermissions = ref<{ parent: string, checkAll: boolean, checkedPermissions: any, children: { permission_name: string, permission_id: string }[] }[]>()
let nowRole: any = null;
const addAndChangeFormShowFun = async (t: string, d: any = null) => {
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
                    url: "/role/delete",
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
    if (t === 'permission') {

        addAndChangeFormDialogTit.value = '关联权限'
        nowRole = d
        relevancyVisible.value = true
        allPermissions.value = []
        await nextTick(async () => {
            await http.request<any>({
                url: '/permission/getPageList',
                method: 'post',
                q_spinning: true,
                q_contentType: 'form',
                data: {
                    pageNum: 1,
                    pageSize: 100000
                },
            }).then(res => {
                if (res.code === 200) {
                    const parents = res.data.records.filter((e: any) => e.parentId === 0)
                    allPermissions.value = parents.map((e: any) => {
                        return {
                            parent: e.name,
                            checkAll: false,
                            checkedPermissions: ref([]),
                            children: res.data.records.filter((e1: any) => e1.parentId === e.id).map((e1: any) => {
                                return {
                                    permission_name: e1.name,
                                    permission_id: e1.id
                                }
                            })
                        }
                    })
                }
            })
            await http.request<any>({
                url: '/role/getRolePermission',
                method: 'post',
                q_spinning: true,
                q_contentType: 'form',
                data: {
                    id: d.id
                },
            }).then(res => {
                if (res.code === 200) {
                    allPermissions.value?.forEach((e) => {
                        e.checkedPermissions = ref(e.children.map((e1: any) => {
                            return res.data.find((e2: any) => e2.permission_id === e1.permission_id)?.permission_id ?? ''
                        }))
                    })
                }
            })
        })
    }
    if (t === 'save') {
        let url = ''
        if (addAndChangeFormDialogTit.value === '新增角色信息') {
            url = '/role/add'
        }
        if (addAndChangeFormDialogTit.value === '修改角色信息') {
            url = '/role/change'
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

const relevancyShowFun = (t: string, d: any = null) => {
    const allChecked = allPermissions.value?.map((e) => toRaw(e.checkedPermissions)).filter(e => e.length !== 0).flat(1).filter(e => e !== '')
    if (allChecked && t === 'save') {
        http.request<any>({
            url: '/role/relevancyRoles',
            method: 'post',
            q_spinning: true,
            q_contentType: 'form',
            data: {
                id: nowRole.id,
                permissionIds: allChecked.join(',')
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
const allPermissionsCheckAll = (e: any) => {
    e.checkAll = !e.checkAll
    e.checkedPermissions = e.checkAll ? e.children.map((e1: any) => e1.permission_id) : []
}
</script>

<style scoped></style>