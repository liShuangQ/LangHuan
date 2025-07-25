<template>

    <div class="box-border p-3 pb-0 pt-7 bg-white">
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
                <template #content-interaction="props">
                    <el-tag v-if="props.row.interaction === 'like'" type="success">{{ props.row.interaction }}</el-tag>
                    <el-tag v-if="props.row.interaction === 'dislike'" type="warning">{{ props.row.interaction
                        }}</el-tag>
                    <el-tag v-if="props.row.interaction === 'end'" type="info">{{ props.row.interaction
                        }}</el-tag>
                </template>
                <template #content-knowledgeBaseIds="props">
                    <el-button link type="primary" @click="openDocumentIds(props.row)">知识库片段
                    </el-button>
                </template>
                <template #content-buttonSlot="props">
                    <el-button type="primary" link @click="tableBtnHandle('addText', props.row)">添加文字</el-button>
                    <el-button type="primary" link @click="tableBtnHandle('end', props.row)"
                        :disabled="props.row.interaction === 'end' || props.row.interaction === 'like'">标记完成</el-button>
                    <el-button link type="primary" @click="tableBtnHandle('del', props.row)">删除
                    </el-button>
                </template>
            </ElementTableC>
        </div>


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
                    <div class=" flex gap-1 mt-2">
                        <el-tag>文件名：{{ item.filename }}</el-tag>
                        <el-tag>文件组：{{ item.groupname }}</el-tag>
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
import dayjs from "dayjs";
import { CheckboxValueType, ElMessage, ElMessageBox } from "element-plus";
import { nextTick, ref } from "vue";
import router from "@/router";
const formComRef = ref<FormDefineExpose>();
const tableComRef = ref<TableDefineExpose>();
let documentIdsVisible = ref(false)
let documentIdsData = ref<any[]>([]);
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
        url: '/chatFeedback/search',
        method: 'post',
        q_spinning: true,
        data: {
            ...formComRef.value!.getFromValue(),
            pageNum: paginationConfig.value.currentPage,
            pageSize: paginationConfig.value.pageSize,
        },
    }).then(res => {
        const search_dayTransformation = ['interactionTime']
        if (search_dayTransformation && search_dayTransformation.length >= 0) {
            res.data.records.forEach((e: any) => {
                search_dayTransformation.forEach((ee: string) => {
                    e[ee] = dayjs(e[ee]).format('YYYY-MM-DD HH:mm:ss')
                })
            })
        }
        tableData.value = res.data['records'];
        paginationConfig.value.total = res.data['total'];
    }).catch(err => {
        console.log(err)
    })
};

const openDocumentIds = (row: any) => {
    documentIdsData.value = []
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
                isEditing: false,
                tempContent: e.content,
                ...e
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
const sendSystemMessage = (row: any) => {
    ElMessageBox.prompt('请输入内容:', '发送通知', {
        confirmButtonText: '发送',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入内容描述...',
        inputValidator: (value) => {
            if (!value || value.trim() === '') {
                return '内容不能为空';
            }
            return true;
        }
    }).then(({ value }) => {
        http
            .request<any>({
                url: "/notifications/create",
                method: "post",
                q_spinning: true,
                q_contentType: "json",
                data: {
                    title: "问题修复：" + row.questionContent,
                    content: `您的建议: ${row.questionContent} 当前已被标记修改完成
                        提出时间：${row.interactionTime}
                        修复内容：${value},

                        感谢您的反馈。
                        `,
                    notificationLevel: 'info',
                    notificationType: 'update',
                    referenceId: '',
                    referenceType: '',
                    expiresAt: dayjs().add(365, 'day').format('YYYY-MM-DD HH:mm:ss'),
                },
                params: {
                    userIds: row.userId,
                },
            })
            .then((res) => {
                if (res.code === 200) {
                    ElMessage.success("创建通知成功");
                } else {
                    ElMessage.error(res.message || "创建通知失败");
                }
            })
            .catch((err) => {
                console.log(err);
                ElMessage.error("创建通知失败");
            });
    }).catch(() => {
        // 用户取消输入
    });

}
const tableBtnHandle = (type: string, row: any) => {
    if (type === 'addText') {
        router.push({
            path: "/pages/rag/addDocuments",
            query: {
                tipText: `问题内容：${row.questionContent} ； 附加建议：${row.suggestion}`,
            }
        })
    }
    if (type === 'end') {
        ElMessageBox.confirm('是否完成该条记录?', '提示', {
            type: 'warning',
            showCancelButton: true,
            cancelButtonText: '取消',
            confirmButtonText: '确定',
        }).then(() => {
            http.request<any>({
                url: '/chatFeedback/changeInteractionToEnd',
                method: 'post',
                q_spinning: true,
                data: {
                    id: row.id
                },
            }).then(res => {
                if (res.code === 200) {
                    ElMessage.success(res.data)
                    getUserPageList()
                    sendSystemMessage(row)
                }
            })
        })
    }
    if (type === 'del') {
        ElMessageBox.confirm('是否删除该条记录?', '提示', {
            type: 'warning',
            showCancelButton: true,
            cancelButtonText: '取消',
            confirmButtonText: '确定',
        }).then(() => {
            http.request<any>({
                url: '/chatFeedback/delete',
                method: 'post',
                q_spinning: true,
                data: {
                    id: row.id
                },
            }).then(res => {
                if (res.code === 200) {
                    ElMessage.success(res.data)
                    getUserPageList()
                }
            })
        })
    }
}
nextTick(() => {
    getUserPageList()
})


</script>

<style scoped></style>