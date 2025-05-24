import { ref, computed } from "vue";
import type { ChatSettings, RagGroup } from "../types";
import aimodel from "@/store/aimodel";
import * as api from "../api";

export function useSettings() {
    const showSettings = ref(false);
    const settings = ref<ChatSettings>({
        modelName: "",
        promptTemplate: "",
        ragGroup: null,
    });

    const availableModels = ref();

    const ragGroups = ref<RagGroup[]>([]);

    const toggleSettings = async () => {
        availableModels.value = await aimodel().getModelOptions();
        if (settings.value.modelName === "") {
            settings.value.modelName = availableModels.value[0].value;
        }
        api.getRagGroupOptionList().then((res) => {
            ragGroups.value = res.data.map((item: any) => ({
                id: item.id,
                name: item.groupName,
            }));
            ragGroups.value.unshift({ id: "", name: "无" });
        });
        showSettings.value = !showSettings.value;
    };

    const getChatParams = computed(() => ({
        modelName: settings.value.modelName,
        p: settings.value.promptTemplate,
        isRag: !!settings.value.ragGroup?.id,
        groupId: settings.value.ragGroup?.id || "",
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
