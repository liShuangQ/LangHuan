<template>
    <div class="multi-ai-dialog mt-2">
        <div class="dialog-header">
            <h2>多AI角色对话</h2>
            <div class="settings">
                <el-button type="primary" @click="showSettingsDialog = true">设置</el-button>
            </div>
        </div>
        <el-dialog title="设置" v-model="showSettingsDialog" width="70%">
            <el-form :model="settings" label-width="120px">
                <el-form-item label="对话轮数">
                    <el-input-number v-model="settings.rounds" :min="1" :max="10"></el-input-number>
                </el-form-item>
                <el-form-item label="背景设定">
                    <el-input v-model="settings.background" type="textarea"></el-input>
                </el-form-item>
                <el-form-item label="初始提问">
                    <el-input v-model="settings.initialQuestions" type="textarea"></el-input>
                </el-form-item>
                <el-form-item label="AI角色提示词">
                    <div v-for="(prompt, index) in settings.aiPrompts" :key="index" class="flex items-center space-x-2 w-full">
                        <el-select v-model="settings.aiModels[index]" placeholder="选择模型">
                            <el-option v-for="model in predefinedModels" :key="model" :label="model" :value="model" />
                        </el-select>
                        <el-input v-model="settings.aiPrompts[index]" type="textarea" placeholder="AI角色提示词"></el-input>
                    </div>
                    <el-button type="primary" @click="addAIPrompt">添加AI角色</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
        <div class="dialog-content" style="height: 70vh;">
            <div v-for="(message, index) in messages" :key="index" class="message"
                :class="{ 'ai-message': message.isAI }">
                <span class="role">{{ message.role }}:</span>
                <span class="text">{{ message.text }}</span>
            </div>
        </div>
        <div class="dialog-footer">
            <el-button type="primary" @click="clearChatMemory()">清除记忆</el-button>
            <el-button type="primary" @click="startConversation">开始对话</el-button>
        </div>
    </div>
</template>
<script lang="ts">
export default {
    auto: true,
};
</script>
<script setup lang="ts">
import { http } from '@/plugins/axios';
import { reactive, ref } from 'vue';

interface Message {
    isAI: boolean;
    role: string;
    text: string;
}

const predefinedModels = ref<string[]>([]); // 预定义的模型列表
// 获取支持的模型列表
const getModelList = (): Promise<any> => {
    return http.request<any>({
        url: '/chatModel/getModelList',
        method: 'post',
        q_spinning: true,
        data: {},
    }).then((res) => {
        if (res.code === 200) {
            predefinedModels.value = res.data.data.filter((e: any) => {
                return e.id.indexOf('embed') === -1
            }).map((e: any) => {
                return e.id
            })
        }
    })
}
getModelList()
const settings = reactive({
    rounds: 5,
    background: '',
    initialQuestions: '请“实干家”分享一个关于快速启动新项目的实用技巧，并邀请“智者”和“梦想家”对此提出他们的看法和建议。',
    aiPrompts: [
        '你被称作“梦想家”，总是充满活力和创意，对未来持有无限憧憬。你的交流方式热情洋溢，常常能激发他人的想象力。你喜欢探讨可能性和创新思维，鼓励大家跳出常规去思考问题。',
        '你被称为“智者”，是一位拥有深厚知识底蕴和长远视野的导师形象。你的语言风格沉稳而富有哲理，擅长通过历史事件和经典案例来阐述观点。你喜欢引导他人思考，而不是直接给出答案。你的目标是在对话中促进理解和智慧的增长。',
        '你被称为“实干家”，是一位注重实际操作与结果的行动导向者。你擅长将理论转化为实际行动步骤，并且总是关注如何高效地达成目标。你的语言直接而明确，喜欢用具体的例子和成功案例来说明观点。在对话中，你鼓励其他角色一起探讨实现想法的具体方法，而不是仅仅停留在讨论层面。你的目标是通过自己的经验和见解，激励他人采取行动，把梦想变为现实。'
    ],
    aiModels: ['', '', ''], // 初始化模型选择
});

const showSettingsDialog = ref(false);
const messages = ref<Message[]>([]);

const addAIPrompt = () => {
    settings.aiPrompts.push('');
    settings.aiModels.push(`模型${settings.aiModels.length + 1}`); // 添加默认模型名
};
// 清空对话记忆
const clearChatMemory = (isList = false, id = 0): any => {
    return http.request<any>({
        url: '/chat/clearChatMemory',
        method: 'get',
        q_spinning: true,
        params: {
            id: 99999,
        }
    })

};
const startConversation = async () => {
    await clearChatMemory()
    let userText = ''
    messages.value = [];
    const { rounds, background, initialQuestions, aiPrompts, aiModels } = settings;
    let currentRound = 0;

    const addMessage = (role: string, text: string, isAI: boolean) => {
        messages.value.push({ role, text, isAI });
    };

    const simulateConversation = async () => {
        for (let index = 0; index < aiPrompts.length; index++) {
            const res = await http.request<any>({
                url: 'chat/stanford',
                method: 'post',
                q_spinning: false,
                params: {
                    id: 99999,
                    modelName: aiModels[index],
                    p: background + aiPrompts[index],
                    q: userText === '' ? initialQuestions : userText
                }
            })

            userText = res.data
            addMessage(`${aiModels[index]}(第${currentRound + 1}轮)`, userText, true);
        }

        currentRound++;
    };
    while (true) {
        if (currentRound >= rounds) return;
        await simulateConversation();
    }
};
</script>

<style scoped>
.multi-ai-dialog {
    @apply p-4 bg-white rounded-lg shadow-md;
}

.dialog-header {
    @apply flex justify-between items-center mb-4;
}

.settings-panel {
    @apply mb-4;
}

.dialog-content {
    @apply overflow-y-auto mb-4;
}

.message {
    @apply mb-2;
}

.ai-message {
    @apply bg-gray-100;
}

.role {
    @apply font-bold;
}

.text {
    @apply ml-2;
}

.dialog-footer {
    @apply flex justify-end;
}
</style>
