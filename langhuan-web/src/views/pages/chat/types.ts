/**
 * 聊天相关类型定义文件
 * 定义聊天功能中使用的各种接口和类型
 */

/**
 * 聊天窗口接口
 */
export interface ChatWindow {
    id: string;              // 窗口ID
    title: string;           // 窗口标题
    date: string;            // 创建日期
    active?: boolean;        // 是否为当前激活窗口
    settingConfig?: ChatSettings | any;  // 窗口设置配置
}

/**
 * 聊天消息发送数据接口
 */
export interface ChatSeedEmitMessageData {
    userMessage: string;     // 用户消息内容
    accessory: File[];       // 附件文件列表
}

/**
 * 聊天发送参数接口
 */
export interface ChatSendParam {
    chatId: string;          // 聊天ID
    prompt: string;          // 系统提示词
    userMessage: string;     // 用户消息
    isRag: boolean;          // 是否启用RAG（检索增强生成）
    isReRank: boolean;       // 是否启用重排序
    ragGroupId: string;      // RAG文件组ID
    isFunction: boolean;     // 是否使用函数调用
    modelName: string;       // 使用的模型名称
}

/**
 * 聊天设置接口
 */
export interface ChatSettings {
    modelName: string;       // 模型名称
    promptTemplate: string;  // 提示词模板
    ragGroup: {              // 选中的RAG文件组
        id: string;
        name: string;
    } | null;
    isReRank: boolean;       // 是否启用重排序

    // 多专家模式相关字段 - 纯前端编写专家流程
    isExpertMode: boolean;   // 是否启用专家模式
    expertFileGroups: {      // 专家模式文件组（多选）
        id: string;
        name: string;
    }[];
    expertConversationRounds: number;  // 专家对话轮数
}

/**
 * 聊天选项接口 - 继承多个接口的部分属性
 */
export interface ChatOption extends Partial<ChatSeedEmitMessageData & ChatSendParam & ChatSettings>{
    fileGroupName?:string    // 文件组名称
    showUserMessage?:boolean // 是否显示用户消息
}

/**
 * 聊天消息接口
 */
export interface Message {
    id: string;              // 消息ID
    content: string;         // 消息内容
    sender: "user" | "assistant";  // 发送者类型
    timestamp: string;       // 时间戳
    loading?: boolean;       // 是否正在加载中
    rag?: any[];            // RAG相关文档列表
    chatSettings?: ChatSettings | any;  // 聊天设置信息
    showUserMessage?: boolean;  // 是否展示用户消息
}

/**
 * RAG文件组接口
 */
export interface RagGroup {
    id: string;              // 文件组ID
    name: string;            // 文件组名称
}

/**
 * 聊天反馈接口
 */
export interface ChatFeedback {
    questionId: string;          // 问题ID
    questionContent: string;     // 问题内容
    answerContent: string;       // 回答内容
    interaction: string;         // 交互类型（like/dislike）
    knowledgeBaseIds: string;    // 知识库ID列表
    suggestion: string;          // 用户建议
    usePrompt: string;           // 使用的提示词
    useModel: string;            // 使用的模型
    useRank: boolean;            // 是否使用了重排序
    useFileGroupId: string;      // 使用的文件组ID
    useFileGroupName: string;    // 使用的文件组名称
}
