interface Message {
    id?: number;
    text: string;
    recommend?: Array<String>,
    isUser: boolean;
    topInfo: string;
}

interface Chat {
    id: string;
    messages: Message[];
    active: boolean;
}