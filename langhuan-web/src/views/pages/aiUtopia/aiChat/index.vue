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
                    <el-input v-model="settings.background" type="textarea"
                        placeholder="会把AI角色提示词和初始提问的'***'替换为当前填写内容"></el-input>
                </el-form-item>
                <el-form-item label="AI角色提示词">
                    <div style="height: 40vh;width: 100%;overflow: auto;">
                        <div v-for="(prompt, index) in settings.aiPrompts" :key="index"
                            class="flex items-center space-x-2 w-full">
                            <el-select v-model="settings.aiModels[index]" placeholder="选择模型">
                                <el-option v-for="model in predefinedModels" :key="model" :label="model"
                                    :value="model" />
                            </el-select>
                            <el-input v-model="settings.aiPrompts[index]" type="textarea"
                                placeholder="AI角色提示词"></el-input>
                        </div>
                    </div>
                    <el-button style="margin-left: 80%;" type="primary" @click="addAIPrompt">添加AI角色</el-button>
                </el-form-item>

                <el-form-item label="初始提问">
                    <el-input v-model="settings.initialQuestions" type="textarea"></el-input>
                </el-form-item>
            </el-form>

            <el-button style="margin-left: 90%;" type="primary" @click="startConversation">开始对话</el-button>

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
        </div>
    </div>
</template>
<script lang="ts">
export default {
    auto: true
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
    // initialQuestions: '请“实干家”分享一个关于快速启动新项目的实用技巧，并邀请“智者”和“梦想家”对此提出他们的看法和建议。',
    // aiPrompts: [
    //     '你被称作“梦想家”，总是充满活力和创意，对未来持有无限憧憬。你的交流方式热情洋溢，常常能激发他人的想象力。你喜欢探讨可能性和创新思维，鼓励大家跳出常规去思考问题。',
    //     '你被称为“智者”，是一位拥有深厚知识底蕴和长远视野的导师形象。你的语言风格沉稳而富有哲理，擅长通过历史事件和经典案例来阐述观点。你喜欢引导他人思考，而不是直接给出答案。你的目标是在对话中促进理解和智慧的增长。',
    //     '你被称为“实干家”，是一位注重实际操作与结果的行动导向者。你擅长将理论转化为实际行动步骤，并且总是关注如何高效地达成目标。你的语言直接而明确，喜欢用具体的例子和成功案例来说明观点。在对话中，你鼓励其他角色一起探讨实现想法的具体方法，而不是仅仅停留在讨论层面。你的目标是通过自己的经验和见解，激励他人采取行动，把梦想变为现实。'
    // ],

    initialQuestions: '各位成员，今天我们齐聚一堂，目的是探讨“***”。每个人都有自己独特的视角和见解，这对我们的讨论来说是非常宝贵的。首先，请允许我提出一个问题以开启今天的对话：在您看来，“***”最有可能带来什么样的影响或变化？请根据您的性格特点和经验，分享您的思考。',
    aiPrompts: [
        '作为INTJ，你的思维逻辑严密，擅长长远规划。请基于你的分析能力和对复杂系统的理解，就“***”提出你的见解和可能的解决方案。',
        '作为INTP，你喜欢探索理论和概念，并善于从不同角度思考问题。请运用你的创造性思维，探讨“***”，并分享你独特的视角。',
        '作为ENTP，你热衷于挑战现状和探索新想法。请就“***”发表你的看法，特别是关于创新解决方案和激发变革的方法。',
        '作为INFJ，你深具同情心且理想主义，致力于帮助他人实现潜能。请分享你对“***”的观点。',
        '作为INFP，你的价值观和个人信念指导着你的行动。请谈谈“***”对你个人的意义，以及它如何与你的理想相联系。',
        '作为ENFJ，你擅长激励和支持他人，致力于建立和谐的社区环境。请讨论“***”，强调其对人际关系和团队合作的影响。',
        '作为ENFP，你充满热情和创造力，总是寻找新的可能性。请分享你对“***”的看法，特别是它带来的机遇和个人成长的空间。',
        '作为ISTJ，你重视传统、秩序和细节。请基于你的经验和可靠性，谈谈“***”，并提供实际的例子说明如何有效应对。',
        '作为ISFJ，你以支持和保护他人为己任。请讨论“***”，特别关注它对家庭、朋友及社区成员的影响。',
        '作为ESTJ，你注重组织和管理，追求高效完成任务。请针对“***”，阐述你的行动计划和确保成功实施的方法。',
        '作为ESFJ，你关心他人的福祉，擅长构建和谐的社会关系。请讨论“***”，强调其对人际关系和社会责任的意义。',
        '作为ISTP，你善于解决问题，喜欢动手实践。请就“***”提出你的技术分析或实际操作建议，展示你的实用技能。',
        '作为ISFP，你享受当下，富有艺术气质。请分享你对“***”的感性认识，以及它给你带来的灵感和情感体验。',
        '作为ESTP，你敢于冒险，擅长抓住机会。请讨论“***”，并提供具体的策略来利用当前的机会。',
        '作为ESFP，你热爱生活，喜欢通过行动和表达带来快乐。请分享你对“***”的热情态度，以及如何让它变得更加有趣和吸引人。'
    ],
    aiModels: ['', '', ''], // 初始化模型选择
});

const showSettingsDialog = ref(false);
const messages = ref<Message[]>([]);

const addAIPrompt = () => {
    settings.aiPrompts.push('');
    settings.aiModels.push(''); // 添加默认模型名
};
// 清空对话记忆
const clearChatMemory = (isList = false, id = 0): any => {
    return http.request<any>({
        url: '/chat/clearChatMemory',
        method: 'post',
        q_spinning: true,
        data: {
            id: 99999,
        }
    })

};
const startConversation = async () => {
    showSettingsDialog.value = false
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
                    p: aiPrompts[index].replaceAll('***', background),
                    q: userText === '' ? initialQuestions.replaceAll('***', background) : userText
                }
            })

            userText = res.data
            // addMessage(`${aiModels[index]}${aiPrompts[index].slice(0, 6)}(第${currentRound + 1}轮):`, userText, true);
            addMessage(`${aiModels[index]}${aiPrompts[index].slice(0, 6)}:`, userText, true);
        }

        currentRound++;
    };
    while (true) {
        if (currentRound >= rounds) {
            return
        }
        await simulateConversation()
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
