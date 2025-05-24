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
