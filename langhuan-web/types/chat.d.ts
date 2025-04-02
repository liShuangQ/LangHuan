interface Message {
    id?: number;
    text: string;
    recommend?: Array<String>,
    metadata?: any,
    isUser: boolean;
    topInfo: string;
}

interface Chat {
    id: string;
    messages: Message[];
    active: boolean;
}