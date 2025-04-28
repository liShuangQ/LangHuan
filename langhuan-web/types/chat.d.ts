interface Message {
    id?: number;
    text: string;
    recommend?: Array<String>,
    rag?: Array<any>,
    metadata?: any,
    isStart?: any,
    isUser: boolean;
    topInfo: string;
}

interface Chat {
    id: string;
    messages: Message[];
    active: boolean;
}