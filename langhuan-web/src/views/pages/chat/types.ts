export interface ChatWindow {
    id: string;
    title: string;
    date: string;
    active?: boolean;
}
export interface ChatSeedEmitMessageData {
    userMessage: string;
    accessory: File[];
}

export interface ChatSendParam {
    chatId: string;
    prompt: string;
    userMessage: string;
    isRag: boolean;
    isReRank: boolean;
    ragGroupId: string;
    isFunction: boolean;
    modelName: string;
}
export interface ChatSettings {
    modelName: string;
    promptTemplate: string;
    ragGroup: {
        id: string;
        name: string;
    } | null;
    isReRank: boolean;
    // 多专家模式相关字段
    // 纯前端编写专家流程
    isExpertMode: boolean;
    expertFileGroups: {
        id: string;
        name: string;
    }[]; // 文件组（多选）
    expertConversationRounds: number; // 对话轮数
}
export interface ChatOption extends Partial<ChatSeedEmitMessageData & ChatSendParam & ChatSettings>{
    fileGroupName?:string
    showUserMessage?:boolean
}
export interface Message {
    id: string;
    content: string;
    sender: "user" | "assistant";
    timestamp: string;
    loading?: boolean;
    rag?: any[];
    chatSettings?: ChatSettings | any;
    showUserMessage?: boolean; // 目前是否展示用户消息
}

export interface RagGroup {
    id: string;
    name: string;
}

export interface ChatFeedback {
    questionId: string;
    questionContent: string;
    answerContent: string;
    interaction: string;
    knowledgeBaseIds: string;
    suggestion: string;
    usePrompt: string;
    useModel: string;
    useRank: boolean;
    useFileGroupId: string;
    useFileGroupName: string;
}
