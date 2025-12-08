<template>
    <!-- 遮罩层 -->
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
        <!-- 对话框 -->
        <div class="bg-white rounded-lg shadow-xl w-[900px] max-w-[90vw] max-h-[90vh] flex flex-col">
            <!-- 对话框头部 -->
            <div class="flex items-center justify-between p-4 border-b border-gray-200">
                <h3 class="text-lg font-semibold text-gray-900">个人空间</h3>
                <button
                    @click="visible = false"
                    class="text-gray-400 hover:text-gray-600 transition-colors"
                >
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                </button>
            </div>

            <!-- 对话框内容 -->
            <div class="flex flex-col h-[70vh] p-4">
                <!-- 查询表单 -->
                <div class="flex items-center gap-3 mb-3">
                    <label class="text-xs font-medium text-gray-700">内容查询</label>
                    <div class="relative flex-1 max-w-sm">
                        <input
                            v-model="documentQueryForm.content"
                            type="text"
                            placeholder="输入查询内容"
                            class="w-full px-2 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-transparent"
                        />
                        <button
                            v-if="documentQueryForm.content"
                            @click="documentQueryForm.content = ''"
                            class="absolute right-1.5 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                        >
                            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                            </svg>
                        </button>
                    </div>
                    <button
                        @click="handleDocumentQuery"
                        class="px-3 py-1.5 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:ring-offset-1 transition-colors"
                    >
                        查询
                    </button>
                </div>

                <!-- 文档列表 -->
                <div class="flex-1 overflow-y-auto border border-gray-200 rounded">
                    <div class="p-3">
                        <div
                            v-for="(item, index) in documentNumData"
                            :key="index"
                            class="mb-4 last:mb-0"
                        >
                            <!-- 查看模式 -->
                            <div v-if="!item.isEditing" class="group">
                                <div class="bg-gray-50 p-3 rounded">
                                    <div class="whitespace-pre-wrap text-gray-800 text-sm leading-normal mb-2">
                                        {{ item.content }}
                                    </div>
                                    <div class="flex justify-end gap-1">
                                        <button
                                            :disabled="!nowRow.canUpdate"
                                            @click="documentHandle('edit', index, item)"
                                            :class="[
                                                'px-2 py-1 text-xs rounded transition-colors',
                                                nowRow.canUpdate
                                                    ? 'text-blue-600 hover:text-blue-800 hover:bg-blue-50'
                                                    : 'text-gray-400 cursor-not-allowed'
                                            ]"
                                        >
                                            修改
                                        </button>
                                        <button
                                            :disabled="!nowRow.canDelete"
                                            @click="documentHandle('delete', index, item)"
                                            :class="[
                                                'px-2 py-1 text-xs rounded transition-colors',
                                                nowRow.canDelete
                                                    ? 'text-red-600 hover:text-red-800 hover:bg-red-50'
                                                    : 'text-gray-400 cursor-not-allowed'
                                            ]"
                                        >
                                            删除
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <!-- 编辑模式 -->
                            <div v-else class="space-y-2">
                                <textarea
                                    v-model="item.tempContent"
                                    rows="5"
                                    class="w-full px-2 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-transparent resize-none"
                                ></textarea>
                                <div class="flex justify-end gap-1">
                                    <button
                                        @click="documentHandle('cancel', index, item)"
                                        class="px-2 py-1 text-xs text-gray-600 border border-gray-300 rounded hover:bg-gray-50 transition-colors"
                                    >
                                        取消
                                    </button>
                                    <button
                                        @click="documentHandle('save', index, item)"
                                        class="px-2 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
                                    >
                                        确定
                                    </button>
                                </div>
                            </div>

                            <!-- 分割线 -->
                            <div v-if="index < documentNumData.length - 1" class="border-b border-gray-200 mt-3"></div>
                        </div>
                    </div>
                </div>

                <!-- 分页 -->
                <div class="flex flex-wrap items-center justify-between mt-4 pt-4 border-t border-gray-200">
                    <div class="text-sm text-gray-700">
                        共 {{ documentPagination.total }} 条记录
                    </div>
                    <div class="flex flex-wrap items-center justify-end gap-2">
                        <!-- 每页条数选择 -->
                        <select
                            v-model="documentPagination.pageSize"
                            @change="handleDocumentPageChange"
                            class="px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="10">10条/页</option>
                            <option value="20">20条/页</option>
                        </select>

                        <!-- 分页按钮 -->
                        <div class="flex items-center gap-1">
                            <button
                                @click="goToPage(1)"
                                :disabled="documentPagination.pageNum === 1"
                                :class="[
                                    'px-2 py-1 text-sm border rounded transition-colors',
                                    documentPagination.pageNum === 1
                                        ? 'text-gray-400 border-gray-200 cursor-not-allowed'
                                        : 'text-gray-700 border-gray-300 hover:bg-gray-50'
                                ]"
                            >
                                首页
                            </button>
                            <button
                                @click="goToPage(documentPagination.pageNum - 1)"
                                :disabled="documentPagination.pageNum === 1"
                                :class="[
                                    'px-2 py-1 text-sm border rounded transition-colors',
                                    documentPagination.pageNum === 1
                                        ? 'text-gray-400 border-gray-200 cursor-not-allowed'
                                        : 'text-gray-700 border-gray-300 hover:bg-gray-50'
                                ]"
                            >
                                上一页
                            </button>

                            <!-- 页码输入 -->
                            <div class="flex items-center gap-1 text-sm text-gray-700">
                                <input
                                    v-model.number="currentPageInput"
                                    @keyup.enter="goToPageInput"
                                    type="number"
                                    :min="1"
                                    :max="totalPages"
                                    class="w-12 px-1 py-1 text-center border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                                <span>/ {{ totalPages }}</span>
                            </div>

                            <button
                                @click="goToPage(documentPagination.pageNum + 1)"
                                :disabled="documentPagination.pageNum >= totalPages"
                                :class="[
                                    'px-2 py-1 text-sm border rounded transition-colors',
                                    documentPagination.pageNum >= totalPages
                                        ? 'text-gray-400 border-gray-200 cursor-not-allowed'
                                        : 'text-gray-700 border-gray-300 hover:bg-gray-50'
                                ]"
                            >
                                下一页
                            </button>
                            <button
                                @click="goToPage(totalPages)"
                                :disabled="documentPagination.pageNum >= totalPages"
                                :class="[
                                    'px-2 py-1 text-sm border rounded transition-colors',
                                    documentPagination.pageNum >= totalPages
                                        ? 'text-gray-400 border-gray-200 cursor-not-allowed'
                                        : 'text-gray-700 border-gray-300 hover:bg-gray-50'
                                ]"
                            >
                                尾页
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
export default {
    auto: true,
};
</script>

<script setup lang="ts">
import { http } from "@/plugins/axios";
import { ElMessageBox } from "element-plus";
import userStore from "@/store/user";
// 定义props和emits
const props = defineProps<{
    modelValue: boolean;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", value: boolean): void;
}>();
const user = userStore();
// 使用计算属性处理v-model
const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit("update:modelValue", value),
});

// 文档相关的响应式数据
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
let nowRow: any = null;

// 分页相关的响应式数据
const currentPageInput = ref(1);

// 计算属性
const totalPages = computed(() => {
    return Math.ceil(documentPagination.total / documentPagination.pageSize);
});

// 打开文档列表
// XXX 优化枚举，使用名称和后端的约定规则查询，注意后端规则修改的联动
const openDocumentNum = async () => {
    const row = await http.request<any>({
        url: "/rag/file/query",
        method: "post",
        q_spinning: true,
        data: {
            fileName: user.info.user.username + "_知识空间",
            fileType: "对话知识空间",
            fileGroupName: "",
            pageNum: 1,
            pageSize: 1,
        },
    });
    if (row.data.records.length === 0) {
        ElMessage({
            type: "info",
            message: "您还没有添加个人空间，请在对话中向我添加吧。",
            offset: 300,
        });
        return;
    }
    nowRow = row.data.records[0];
    documentQueryForm.content = "";
    documentPagination.total = 0;
    documentPagination.pageNum = 1;
    documentPagination.pageSize = 10;
    currentPageInput.value = 1;
    fetchDocuments();
};

// 获取文档数据
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
            currentPageInput.value = documentPagination.pageNum;
        });
    });
};

// 处理文档查询
const handleDocumentQuery = () => {
    documentPagination.pageNum = 1;
    currentPageInput.value = 1;
    fetchDocuments();
};

// 处理分页变化
const handleDocumentPageChange = () => {
    currentPageInput.value = documentPagination.pageNum;
    fetchDocuments();
};

// 跳转到指定页面
const goToPage = (page: number) => {
    if (page >= 1 && page <= totalPages.value) {
        documentPagination.pageNum = page;
        handleDocumentPageChange();
    }
};

// 通过输入框跳转页面
const goToPageInput = () => {
    const page = currentPageInput.value;
    if (page >= 1 && page <= totalPages.value) {
        documentPagination.pageNum = page;
        handleDocumentPageChange();
    } else {
        currentPageInput.value = documentPagination.pageNum;
    }
};

// 文档操作处理
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
                        handleDocumentQuery();
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

// 暴露方法供父组件调用
defineExpose({
    openDocumentNum,
});
</script>

<style scoped>
/* 自定义滚动条样式 */
.overflow-y-auto::-webkit-scrollbar {
    width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* 输入框聚焦动画 */
input:focus,
textarea:focus,
select:focus {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

/* 按钮悬停效果 */
button {
    transition: all 0.2s ease-in-out;
}

button:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

button:active:not(:disabled) {
    transform: translateY(0);
}

/* 文档卡片悬停效果 */
.group:hover .bg-gray-50 {
    background-color: #f9fafb;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.2s ease-in-out;
}

/* 对话框动画 */
.fixed {
    animation: fadeIn 0.2s ease-out;
}

.bg-white.rounded-lg {
    animation: slideIn 0.3s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: scale(0.95) translateY(-10px);
    }
    to {
        opacity: 1;
        transform: scale(1) translateY(0);
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .w-\[900px\] {
        width: 95vw;
    }

    .flex-1.max-w-md {
        max-width: 200px;
    }

    .gap-4 {
        gap: 0.5rem;
    }

    .px-4 {
        padding-left: 1rem;
        padding-right: 1rem;
    }
}
</style>
