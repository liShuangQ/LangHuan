/**
 * 聊天设置功能组合式函数
 * 管理聊天相关的设置，包括模型选择、RAG配置等
 */
import { ref, computed } from "vue";
import type { ChatSettings, RagGroup } from "../types";
import aimodel from "@/store/aimodel";
import * as api from "../api";

export function useSettings() {
    // 设置面板显示状态
    const showSettings = ref(true);

    // 聊天设置配置
    const settings = ref<ChatSettings>({
        modelName: "", // 模型名称
        promptTemplate: "", // 提示词模板
        ragGroup: null, // RAG文件组
        isReRank: false, // 是否启用重排序
        isExpertMode: false, // 是否启用专家模式
        expertFileGroups: [], // 专家模式文件组列表
        expertConversationRounds: 1, // 专家模式对话轮数
    });

    // 可用模型列表
    const availableModels = ref<{ label: string; value: string }[]>([]);

    // RAG文件组列表
    const ragGroups = ref<RagGroup[]>([]);

    /**
     * 获取设置信息
     * 包括可用模型、默认模型、RAG文件组等
     */
    const getSetInfo = async () => {
        // 获取可用模型列表
        availableModels.value = (await aimodel().getModelOptions()) as {
            label: string;
            value: string;
        }[];

        // 获取并设置默认模型
        const getBaseModelRes = await api.getBaseModel();
        settings.value.modelName = getBaseModelRes.data;

        // 获取RAG文件组列表
        api.getRagGroupOptionList().then((res) => {
            ragGroups.value = res.data.map((item: any) => ({
                id: String(item.id),
                name: item.groupName,
            }));
        });
    };

    // 初始化设置信息
    getSetInfo();

    /**
     * 切换设置面板显示状态
     */
    const toggleSettings = async () => {
        showSettings.value = !showSettings.value;
    };

    /**
     * 获取聊天参数配置，根据当前设置转换为后端对应的参数格式
     * TODO: HACK 根据后端参数改变
     */
    const getChatParams = computed(() => ({
        modelName: settings.value.modelName,
        prompt: settings.value.promptTemplate,
        isRag: !!settings.value.ragGroup?.id,
        ragGroupId: settings.value.ragGroup?.id || "",
        isReRank: settings.value.isReRank,
    }));

    /**
     * 设置参数配置，挑选参数更新
     */
    const setChatParams = (params: Partial<ChatSettings>) => {
        const keys = Object.keys(params) as Array<keyof ChatSettings>;
        if (keys.includes("modelName")) {
            settings.value.modelName =
                params.modelName || settings.value.modelName;
        }
        if (keys.includes("ragGroup")) {
            // 如果是手清空的应该是{"id": "","name": ""}，用来区分是手清空的还是新创建的窗口。
            settings.value.ragGroup = params.ragGroup || null;
        }
        if (keys.includes("promptTemplate")) {
            settings.value.promptTemplate =
                params.promptTemplate || settings.value.promptTemplate;
        }
    };

    /**
     * 更新线上参数配置信息，注意存入的是settings信息
     */
    const updateSettingsConfig = async (
        currentWindowId: string,
        newSettings: ChatSettings
    ) => {
        await api.setChatMemoryConversationConfig(
            currentWindowId,
            JSON.stringify(newSettings)
        );
    };

    // 返回需要对外暴露的属性和方法
    return {
        showSettings,
        settings,
        availableModels,
        ragGroups,
        toggleSettings,
        getChatParams,
        setChatParams,
        updateSettingsConfig,
    };
}
