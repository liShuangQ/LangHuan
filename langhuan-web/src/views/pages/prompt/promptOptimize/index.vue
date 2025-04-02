<template>
    <div>
        <div class="flex h-full w-full p-4 gap-4">
            <!-- 左侧优化区块 -->
            <div class="flex-1 border rounded p-4">
                <h2 class="text-lg font-bold mb-4">提示词优化</h2>

                <!-- 原始提示词输入 -->
                <div class="mb-4">
                    <label class="block text-sm font-medium mb-1">原始提示词</label>
                    <el-input v-model="originalPrompt" type="textarea" :rows="5" placeholder="请输入原始提示词" />
                </div>

                <!-- 优化选项行 -->
                <div class="flex gap-4 mb-4">
                    <div class="flex-1">
                        <label class="block text-sm font-medium mb-1">优化模型</label>
                        <el-select v-model="optimizeModel" placeholder="请选择优化模型" class="w-full">
                            <el-option v-for="item in modelOptions as any" :key="item.value" :label="item.label"
                                :value="item.value" />
                        </el-select>
                    </div>

                    <div class="flex-1">
                        <label class="block text-sm font-medium mb-1">优化提示词</label>
                        <el-select v-model="optimizePrompt" placeholder="请选择优化提示词" class="w-full">
                            <el-option v-for="item in promptOptions as any" :key="item.value" :label="item.label"
                                :value="item.value">
                                <div>{{ item.label }}---{{ item.desc }}</div>
                            </el-option>
                        </el-select>
                    </div>

                    <div class="flex items-end">
                        <el-button type="primary" @click="handleOptimize">
                            开始优化
                        </el-button>
                    </div>
                </div>

                <!-- 优化结果 -->
                <div>
                    <div class="flex items-center justify-between mb-1">
                    <label class="block text-sm font-medium">优化后的提示词</label>
                    <el-button size="small" @click="copyOptimizedPrompt">
                        <el-icon><DocumentCopy /></el-icon>
                    </el-button>
                </div>
                    <el-input v-model="optimizedPrompt" type="textarea" :rows="14" placeholder="优化后的提示词将显示在这里"
                        readonly />
                    <el-button style="float:right;margin-top: 8px;" type="primary"
                        @click="addToUsePrompt('add')">添加到在用提示词</el-button>
                </div>
            </div>

            <!-- 右侧测试区块 -->
            <div class="flex-1 border rounded p-4">
                <h2 class="text-lg font-bold mb-4">测试对比</h2>

                <!-- 测试内容输入 -->
                <div class="mb-4">
                    <label class="block text-sm font-medium mb-1">测试内容</label>
                    <el-input v-model="testContent" type="textarea" :rows="5" placeholder="请输入测试内容" />
                </div>

                <!-- 测试选项行 -->
                <div class="flex gap-4 mb-4 items-end">
                    <div class="flex-1">
                        <label class="block text-sm font-medium mb-1">模型</label>
                        <el-select v-model="testModel" placeholder="请选择测试模型" class="w-full">
                            <el-option v-for="item in modelOptions as any" :key="item.value" :label="item.label"
                                :value="item.value" />
                        </el-select>
                    </div>

                    <div class="flex items-center gap-2">
                        <span class="text-sm">对比模式</span>
                        <el-switch v-model="compareMode" />
                    </div>

                    <div class="flex items-end">
                        <el-button type="primary" @click="handleCompare">
                            开始对比
                        </el-button>
                    </div>
                </div>

                <!-- 对比结果 -->
                <div class="flex gap-4">
                    <div class="flex-1" v-if="compareMode">
                        <label class="block text-sm font-medium mb-1">原始提示词结果</label>
                        <el-input v-model="originalResult" type="textarea" :rows="17" placeholder="原始提示词结果将显示在这里"
                            readonly />
                    </div>

                    <div class="flex-1">
                        <label class="block text-sm font-medium mb-1">优化后提示词结果</label>
                        <el-input v-model="optimizedResult" type="textarea" :rows="17" placeholder="优化后提示词结果将显示在这里"
                            readonly />
                    </div>
                </div>
            </div>
        </div>
        <el-dialog v-model="addFormVisible" title="添加在用提示词" width="800">
            <ElementFormC ref="addFormComRef" :formConfig="addFormConfig" :formItemConfig="addFormItemConfig">
            </ElementFormC>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="addToUsePrompt('close')">取消</el-button>
                    <el-button type="primary" @click="addToUsePrompt('save')">
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
<script lang="ts" setup>
import { http } from "@/plugins/axios";
import { ref } from 'vue'
import { DocumentCopy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
    addAndChangeFormConfig as addFormConfig,
    addAndChangeFormItemConfig as addFormItemConfig,
} from "../usePrompt/addAndChangeformConfig";
import {
    FormDefineExpose,
} from "@/components/globalComponents/ElementFormC/form-component";
// 左侧区块数据
const originalPrompt = ref('')
const optimizeModel = ref('')
const optimizePrompt = ref('')
const optimizedPrompt = ref('')

// 右侧区块数据
const testContent = ref('')
const testModel = ref('')
const originalResult = ref('')
const optimizedResult = ref('')
const compareMode = ref(true)

const copyOptimizedPrompt = async () => {
    if (!optimizedPrompt.value) {
        ElMessage.warning('没有可复制的内容')
        return
    }
    try {
        if (navigator.clipboard) {
            await navigator.clipboard.writeText(optimizedPrompt.value)
            ElMessage.success('复制成功')
        } else {
            // 备用方案
            const textarea = document.createElement('textarea')
            textarea.value = optimizedPrompt.value
            document.body.appendChild(textarea)
            textarea.select()
            document.execCommand('copy')
            document.body.removeChild(textarea)
            ElMessage.success('复制成功')
        }
    } catch (err) {
        console.error('复制失败', err)
        ElMessage.error('复制失败')
    }
}

watch(compareMode, (newVal) => {
    if (newVal) {
        originalResult.value = ''
    }
})
// 添加
const addFormVisible = ref(false)
const addFormComRef = ref<FormDefineExpose>();
// 选项数据
const modelOptions = ref([])

const promptOptions = ref([])

// 优化处理
const handleOptimize = () => {
    if (optimizePrompt.value === '' || originalPrompt.value === '') {
        ElMessage.warning('请选择模型和优化提示词')
        return
    }
    http.request<any>({
        url: '/chat/easyChat',
        method: 'post',
        q_spinning: true,
        data: {
            p: optimizePrompt.value,
            q: originalPrompt.value,
            modelName: optimizeModel.value,
        },
    }).then((res) => {
        if (res.code === 200) {
            optimizedPrompt.value = res.data.chat
        }
    })
}
// 对比处理
const handleCompare = async () => {
    if (testContent.value === '') {
        ElMessage.warning('请输入测试内容')
        return
    }
    if (testModel.value === '') {
        ElMessage.warning('请选择模型')
        return
    }
    if (originalPrompt.value === '') {
        ElMessage.warning('请输入原始提示词')
        return
    }
    compareMode.value && await http.request<any>({
        url: '/chat/easyChat',
        method: 'post',
        q_spinning: true,
        data: {
            p: originalPrompt.value,
            q: testContent.value,
            modelName: testModel.value,
        },
    }).then((res) => {
        if (res.code === 200) {
            originalResult.value = res.data.chat
        }
    })
    if (optimizedPrompt.value === '') {
        ElMessage.warning('请先优化提示词')
        return
    }
    await http.request<any>({
        url: '/chat/easyChat',
        method: 'post',
        q_spinning: true,
        data: {
            p: optimizedPrompt.value,
            q: testContent.value,
            modelName: testModel.value,
        },
    }).then((res) => {
        if (res.code === 200) {
            optimizedResult.value = res.data.chat
        }
    })
}
// 获取支持的模型列表
const getModelList = (): Promise<any> => {
    return http.request<any>({
        url: '/chatModel/getModelList',
        method: 'post',
        q_spinning: true,
        data: {},
    }).then((res) => {
        if (res.code === 200) {
            modelOptions.value = res.data.data.filter((e: any) => {
                return e.id.indexOf('embed') === -1
            }).map((e: any) => {
                return {
                    label: e.id,
                    value: e.id
                }
            })
        }
    })
}
// 获取提示词的列表
const getPromptOptionList = (): Promise<any> => {
    return http.request<any>({
        url: '/prompts/usePrompt/query',
        method: 'post',
        q_spinning: true,
        data: {
            methodName: '',
            category: 'promptOptimize',
            description: '',
            button: '',
            pageNum: 1,
            pageSize: 10000
        },
    }).then((res) => {
        if (res.code === 200) {
            promptOptions.value = res.data.records.map((e: any) => {
                return {
                    label: e.methodName,
                    value: e.content,
                    desc: e.description,
                }
            })
        }
    })
}
// 添加到在用提示词
const addToUsePrompt = async (t: string, d: any = null) => {
    if (optimizedPrompt.value === '') {
        ElMessage.warning('请先优化提示词')
        return
    }
    if (t === 'add') {
        addFormVisible.value = true
        nextTick(() => {
            addFormComRef.value!.resetForm()
            addFormComRef.value!.setFormOption([
                {
                    key: 'content',
                    value: optimizedPrompt.value
                }
            ])
        })
    }
    if (t === 'save') {
        addFormComRef
            .value!.submitForm()
            .then((res) => {
                let sd: any = addFormComRef.value!.getFromValue()
                http.request<any>({
                    url: '/prompts/usePrompt/add',
                    method: 'post',
                    q_spinning: true,
                    q_contentType: 'json',
                    data: sd,
                }).then(res => {
                    if (res.code === 200) {
                        ElMessage.success('操作成功')
                        addFormVisible.value = false
                    }
                })
            })
            .catch((rej: any) => {
                console.log(rej, "失败");
            });
    }
    if (t === 'close') {
        addFormVisible.value = false
    }
}

// 初始化执行
nextTick(async () => {
    await getModelList()
    await getPromptOptionList()

})
</script>





<style scoped>
/* 可以添加一些自定义样式 */
</style>