import { ref, computed } from "vue";
import type { ChatSettings, RagGroup } from "../types";
import aimodel from "@/store/aimodel";
import * as api from "../api";

export function useSettings() {
    const showSettings = ref(true);
    const settings = ref<ChatSettings>({
        modelName: "",
        promptTemplate: "",
        ragGroup: null,
        isReRank: false,
        isExpertMode: false,
        expertFileGroups: [],
        expertConversationRounds: 1,
    });

    const availableModels = ref<{ label: string; value: string }[]>([]);

    const ragGroups = ref<RagGroup[]>([]);
    const getSetInfo = async () => {
        availableModels.value = (await aimodel().getModelOptions()) as {
            label: string;
            value: string;
        }[];

        // 默认模型选择
        const getBaseModelRes = await api.getBaseModel();
        settings.value.modelName = getBaseModelRes.data;
        api.getRagGroupOptionList().then((res) => {
            ragGroups.value = res.data.map((item: any) => ({
                id: String(item.id),
                name: item.groupName,
            }));
        });
    };
    getSetInfo();

    const toggleSettings = async () => {
        showSettings.value = !showSettings.value;
    };

    // HACK 根据后端参数改变
    const getChatParams = computed(() => ({
        modelName: settings.value.modelName,
        prompt: settings.value.promptTemplate,
        isRag: !!settings.value.ragGroup?.id,
        ragGroupId: settings.value.ragGroup?.id || "",
        isReRank: settings.value.isReRank,
    }));

    return {
        showSettings,
        settings,
        availableModels,
        ragGroups,
        toggleSettings,
        getChatParams,
    };
}
