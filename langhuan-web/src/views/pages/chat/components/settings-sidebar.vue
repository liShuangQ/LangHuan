<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { Close } from "@element-plus/icons-vue";
import type { ChatSettings, RagGroup } from "../types";
import PersonalSpace from "./personal-space.vue";
import userStore from "@/store/user";
import { fa } from "element-plus/es/locale";
const user = userStore();
defineOptions({
    name: "SettingsSidebar",
});

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
let ragGroupOnceSwitch = false;

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
            let base: string[] = [];
            if (ragGroupOnceSwitch) {
                base = [];
            } else {
                base = [
                    props.ragGroups.find(
                        (g) =>
                            g.name ===
                            user.info.user.username + "_知识空间文件组"
                    )?.id || "",
                ].filter((e) => !!e);
            }
            emitUpdateRagGroup(base);
            return base;
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
            ragGroupOnceSwitch = true;
        }, 500);
    });
});
</script>

<template>
    <aside class="flex flex-row-reverse">
        <div
            class="relative h-screen w-60 overflow-y-auto border-l border-slate-300 bg-slate-50 py-8 dark:border-slate-700 dark:bg-slate-900 sm:w-64"
        >
            <div class="mb-4 flex items-center justify-between px-4">
                <h2
                    class="text-lg font-medium text-slate-800 dark:text-slate-200"
                >
                    设置
                </h2>
                <el-button link @click="$emit('close')" :icon="Close" />
            </div>

            <div class="px-4 space-y-6">
                <!-- 模型选择 -->
                <div>
                    <label
                        class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1"
                        >使用的模型</label
                    >
                    <el-select v-model="modelName" size="small" class="w-full">
                        <el-option
                            v-for="model in availableModels"
                            :key="model.value"
                            :label="model.label"
                            :value="model.value"
                        />
                    </el-select>
                </div>

                <!-- 提示词 -->
                <div>
                    <label
                        class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1"
                        >提示词</label
                    >
                    <el-input
                        v-model="promptTemplate"
                        type="textarea"
                        size="small"
                        :rows="3"
                        placeholder="输入提示词..."
                    />
                </div>
                <!-- RAG组选择 -->
                <div>
                    <label
                        class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1"
                        >RAG文件组</label
                    >
                    <el-select
                        v-model="ragGroup"
                        multiple
                        :multiple-limit="5"
                        clearable
                        size="small"
                        class="w-full"
                        placeholder="选择RAG文件组"
                        :disabled="isExpertMode"
                    >
                        <el-option
                            v-for="group in ragGroups"
                            :key="group.id"
                            :label="group.name"
                            :value="group.id"
                        />
                    </el-select>
                    <div
                        v-if="isExpertMode"
                        class="text-xs text-slate-500 dark:text-slate-400 mt-1"
                    >
                        专家模式下RAG文件组不可用
                    </div>
                </div>

                <!-- ReRank模型开关 -->
                <div>
                    <label
                        class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1"
                        >启用ReRank模型</label
                    >
                    <el-switch
                        v-model="isReRank"
                        size="small"
                        active-text="开启"
                        inactive-text="关闭"
                        :disabled="isReRankDisabled"
                    />
                    <div
                        v-if="isReRankDisabled"
                        class="text-xs text-slate-500 dark:text-slate-400 mt-1"
                    >
                        需要选择RAG文件组才能启用ReRank模型
                    </div>
                </div>

                <!-- 个人空间 -->
                <div>
                    <label
                        class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1"
                        >个人空间(Beta)</label
                    >
                    <el-button size="small" @click="openPersonalSpace"
                        >进入个人空间</el-button
                    >
                </div>

                <!-- 专家模式 -->
                <div>
                    <label
                        class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1"
                        >多专家模式(Beta)</label
                    >
                    <el-switch
                        v-model="isExpertMode"
                        size="small"
                        active-text="开启"
                        inactive-text="关闭"
                    />
                    <div
                        v-if="isExpertMode"
                        class="text-xs text-slate-500 dark:text-slate-400 mt-1"
                    >
                        <span>建议：专家选择数 x 对话轮数 &lt;= 30</span>
                    </div>
                    <!-- 文件组选择 -->
                    <div v-if="isExpertMode" class="my-1">
                        <el-select
                            v-model="expertFileGroups"
                            size="small"
                            class="w-full"
                            placeholder="选择文件组"
                            multiple
                        >
                            <el-option
                                v-for="group in ragGroups.filter(
                                    (item) => item.name != '无'
                                )"
                                :key="group.id"
                                :label="group.name"
                                :value="group.id"
                            />
                        </el-select>
                    </div>

                    <!-- 对话轮数 -->
                    <div v-if="isExpertMode">
                        <span class="text-xs">对话轮数 : </span>
                        <el-slider
                            class="px-1"
                            v-model="expertConversationRounds"
                            show-stops
                            :max="20"
                            :min="1"
                            :step="1"
                        />
                    </div>
                </div>
            </div>
        </div>

        <!-- 个人空间组件 -->
        <PersonalSpace
            ref="personalSpaceRef"
            v-model="isPersonalSpaceVisible"
        />
    </aside>
</template>
