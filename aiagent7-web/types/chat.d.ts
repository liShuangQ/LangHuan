interface Message {
    id: number;
    text: string;
    recommend?: Array<String>,
    isUser: boolean;
}

interface Chat {
    id: number;
    messages: Message[];
    active: boolean;
}