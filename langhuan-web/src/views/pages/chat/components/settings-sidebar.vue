<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { Close } from "@element-plus/icons-vue";
import type { ChatSettings, RagGroup } from "../types";
import PersonalSpace from "./personal-space.vue";
import userStore from "@/store/user";
import { useBreakpoints } from "@vueuse/core";
const user = userStore();
defineOptions({
    name: "SettingsSidebar",
});

const breakpoints = useBreakpoints({
    sm: 640,
    md: 768,
    lg: 1024,
    xl: 1280,
})

const isMobile = breakpoints.smaller('sm')
const props = defineProps<{
    modelValue: ChatSettings; // 改用 v-model 的标准命名
    availableModels: { label: string; value: string }[];
    ragGroups: RagGroup[];
}>();

const emit = defineEmits<{
    (e: "update:modelValue", value: ChatSettings): void;
    (e: "close"): void;
}>();

// 使用计算属性处理双向绑定
const modelName = computed({
    get: () => props.modelValue.modelName,
    set: (value) =>
        emit("update:modelValue", { ...props.modelValue, modelName: value }),
});

const promptTemplate = computed({
    get: () => props.modelValue.promptTemplate,
    set: (value) =>
        emit("update:modelValue", {
            ...props.modelValue,
            promptTemplate: value,
        }),
});
// 初始化RAG文件组的默认值，无值去找是不是有默认重置，有值后将长度>0 满足直接清空
let ragGroupOnceBase: string[] = [];
// 配置的默认文件组，${username}会被替换为用户名
const GROUP_BASE_NAMES = computed(() => {
    return process.env.GROUP_BASE_NAMES?.replaceAll(
        "${username}",
        user.info.user.username
    )?.split(",") as string[];
});
const emitUpdateRagGroup = (value: any) => {
    const selectedGroup = {
        id: value.join(","),
        name: value
            .map(
                (item: string) =>
                    props.ragGroups.find((g) => g.id === item)?.name || ""
            )
            .join(","),
    };

    const newSettings = {
        ...props.modelValue,
        ragGroup: selectedGroup || null,
    };

    // 当不选RAG文件组时，自动关闭ReRank
    if (value.length === 0) {
        newSettings.isReRank = false;
    }
    emit("update:modelValue", newSettings);
};

// 文件组-修改计算属性处理方式
const ragGroup = computed({
    get: () => {
        // 当专家模式开启时，返回空值
        if (isExpertMode.value) {
            return [];
        }
        if (props.modelValue.ragGroup?.id) {
            return props.modelValue.ragGroup.id.split(",").filter((e) => !!e);
        } else {
            if (ragGroupOnceBase.length > 0) {
                ragGroupOnceBase = [];
            } else {
                ragGroupOnceBase = GROUP_BASE_NAMES.value
                    .map((e: string) => {
                        return (
                            props.ragGroups.find((g) => g.name === e)?.id || ""
                        );
                    })
                    .filter((e) => !!e);
            }
            return ragGroupOnceBase;
        }
    },
    set: (value: any) => {
        // 当专家模式开启时，不允许设置值
        if (isExpertMode.value) {
            return;
        }
        emitUpdateRagGroup(value);
    },
});

const isReRank = computed({
    get: () => props.modelValue.isReRank,
    set: (value) =>
        emit("update:modelValue", { ...props.modelValue, isReRank: value }),
});

// 计算ReRank开关是否应该被禁用
const isReRankDisabled = computed(() => !props.modelValue.ragGroup?.id);

// 个人空间相关
const personalSpaceRef = ref<InstanceType<typeof PersonalSpace>>();
const isPersonalSpaceVisible = ref(false);

// 打开个人空间
const openPersonalSpace = () => {
    isPersonalSpaceVisible.value = true;
    personalSpaceRef.value!.openDocumentNum();
};

// 多专家模式
const isExpertMode = ref(false);

// 专家模式文件组（多选）
const expertFileGroups = computed({
    get: () => props.modelValue.expertFileGroups?.map((e) => e.id) || [],
    set: (value) => {
        emit("update:modelValue", {
            ...props.modelValue,
            expertFileGroups: value.map((e: any) => {
                return {
                    id: e,
                    name: props.ragGroups.find((g) => g.id === e)?.name || "",
                };
            }),
        });
        return value;
    },
});

// 专家模式对话轮数
const expertConversationRounds = computed({
    get: () => props.modelValue.expertConversationRounds || 1,
    set: (value) =>
        emit("update:modelValue", {
            ...props.modelValue,
            expertConversationRounds: value,
        }),
});

// 监听专家模式变化，当开启时清空ragGroup，关闭时清空专家模式相关设置
watch(isExpertMode, (newValue) => {
    if (newValue) {
        // 当开启专家模式时，清空ragGroup并关闭ReRank
        const newSettings = {
            ...props.modelValue,
            ragGroup: null,
            isReRank: false,
            isExpertMode: newValue,
        };
        emit("update:modelValue", newSettings);
    } else {
        // 当关闭专家模式时，清空专家模式相关设置
        const newSettings = {
            ...props.modelValue,
            isExpertMode: newValue,
            expertFileGroups: [],
            expertConversationRounds: 1,
        };
        emit("update:modelValue", newSettings);
    }
});

onMounted(() => {
    nextTick(() => {
        setTimeout(() => {
            emitUpdateRagGroup(ragGroupOnceBase);
        }, 500);
    });
});
</script>

<template>
    <aside :class="[
        'flex flex-row-reverse',
        !isMobile ? 'relative' : ['fixed', 'inset-0', 'z-50', 'bg-black/20', 'backdrop-blur-sm']
    ]">
        <div :class="[
            'relative h-screen w-64 overflow-y-auto bg-white py-6 shadow-xl',
            'border-l border-slate-200 dark:border-slate-700',
            'dark:bg-slate-900',
            !isMobile ? 'w-64' : 'w-80 max-w-[80vw]'
        ]">
            <!-- Header -->
            <div class="mb-6 flex items-center justify-between px-5">
                <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
                    设置
                </h2>
                <button @click="$emit('close')"
                    class="rounded-md p-2 text-slate-500 hover:bg-slate-100 hover:text-slate-700 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-200 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1">
                    <Close class="h-4 w-4" />
                    <span class="sr-only">关闭设置面板</span>
                </button>
            </div>

            <div class="px-5 space-y-5">
                <!-- 模型选择 -->
                <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700 dark:text-slate-300">
                        使用的模型
                    </label>
                    <el-select v-model="modelName" size="default" class="w-full" filterable placeholder="请选择模型">
                        <el-option v-for="model in availableModels" :key="model.value" :label="model.label"
                            :value="model.value" />
                    </el-select>
                </div>

                <!-- 提示词 -->
                <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700 dark:text-slate-300">
                        提示词
                    </label>
                    <el-input v-model="promptTemplate" type="textarea" size="default" :rows="4"
                        placeholder="输入自定义提示词...(非必要不建议填写)" resize="none" />
                </div>

                <!-- RAG组选择 -->
                <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700 dark:text-slate-300">
                        RAG文件组
                    </label>
                    <el-select v-model="ragGroup" multiple filterable :multiple-limit="5" clearable size="default"
                        class="w-full" placeholder="选择RAG文件组（最多5个）" :disabled="isExpertMode">
                        <el-option v-for="group in ragGroups" :key="group.id" :label="group.name" :value="group.id" />
                    </el-select>
                    <div v-if="isExpertMode" class="text-xs text-amber-600 dark:text-amber-400 mt-1">
                        专家模式下RAG文件组不可用
                    </div>
                </div>

                <!-- ReRank模型开关 -->
                <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700 dark:text-slate-300">
                        启用ReRank模型
                    </label>
                    <div class="flex items-center space-x-3">
                        <el-switch v-model="isReRank" size="default" active-text="开启" inactive-text="关闭"
                            :disabled="isReRankDisabled" />

                    </div>
                    <div v-if="isReRankDisabled" class="text-xs text-slate-500 dark:text-slate-400">
                        需要先选择RAG文件组
                    </div>
                </div>

                <!-- 个人空间 -->
                <div class="space-y-2">
                    <el-button size="default" type="default" @click="openPersonalSpace" class="w-full">
                        进入个人空间
                    </el-button>
                </div>

                <!-- 专家模式 -->
                <div class="space-y-3 border-t border-slate-200 dark:border-slate-700 pt-4">
                    <div class="space-y-2">
                        <label class="block text-sm font-medium text-slate-700 dark:text-slate-300">
                            多专家模式
                            <span class="ml-1 text-xs text-amber-600 dark:text-amber-400">(Beta)</span>
                        </label>
                        <div class="flex items-center space-x-3">
                            <el-switch v-model="isExpertMode" size="default" active-text="开启" inactive-text="关闭" />
                        </div>
                        <div v-if="isExpertMode"
                            class="text-xs text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/20 p-2 rounded-md">
                            建议：专家选择数 × 对话轮数 ≤ 30
                        </div>
                    </div>

                    <!-- 专家模式配置 -->
                    <div v-if="isExpertMode" class="space-y-3 pl-3 border-l-2 border-amber-200 dark:border-amber-800">
                        <!-- 文件组选择 -->
                        <div class="space-y-2">
                            <label class="block text-sm font-medium text-slate-700 dark:text-slate-300">
                                选择文件组
                            </label>
                            <el-select v-model="expertFileGroups" size="default" class="w-full" placeholder="请选择文件组"
                                multiple>
                                <el-option v-for="group in ragGroups.filter((item) => item.name !== '无')"
                                    :key="group.id" :label="group.name" :value="group.id" />
                            </el-select>
                        </div>

                        <!-- 对话轮数 -->
                        <div class="space-y-2">
                            <div class="flex items-center justify-between">
                                <label class="text-sm font-medium text-slate-700 dark:text-slate-300">
                                    对话轮数
                                </label>
                                <span
                                    class="text-sm font-mono text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/20 px-2 py-0.5 rounded">
                                    {{ expertConversationRounds }}
                                </span>
                            </div>
                            <el-slider class="px-2" v-model="expertConversationRounds" show-stops :max="20" :min="1"
                                :step="1" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 移动端遮罩层 -->
            <div v-if="isMobile" @click="$emit('close')" class="fixed inset-0 bg-black/20 backdrop-blur-sm -z-10">
            </div>
        </div>

        <!-- 个人空间组件 -->
        <PersonalSpace ref="personalSpaceRef" v-model="isPersonalSpaceVisible" />
    </aside>
</template>
