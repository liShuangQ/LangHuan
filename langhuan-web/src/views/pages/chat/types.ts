export interface ChatWindow {
    id: string;
    title: string;
    date: string;
    active?: boolean;
}

export interface Message {
    id: string;
    content: string;
    sender: 'user' | 'assistant';
    timestamp: string;
    loading?: boolean;
    rag?: any[];
}

export interface ChatMessage {
    id: string;
    p: string;
    q: string;
    isRag: boolean;
    groupId: string;
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
}