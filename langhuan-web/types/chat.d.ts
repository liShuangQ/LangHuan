interface Message {
    id?: number;
    time?: number;
    text: string;
    recommend?: Array<String>;
    rag?: Array<any>;
    metadata?: any;
    isStart?: any;
    isUser: boolean;
    useLlm?: string;
    topInfo: string;
}

interface ChatWindow {
    id: string;
    title: string;
    messages: Message[];
    active: boolean;

    // 用户信息
}