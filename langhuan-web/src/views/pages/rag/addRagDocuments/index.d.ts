type FileType = "text" | "file" | "html";
type SplitFileMethod =
    | "FixedWindowTextSplitter"
    | "PatternTokenTextSplitter"
    | "LlmTextSplitter";
type MethodData = {
    FixedWindowTextSplitter: {
        windowSize: number;
    };
    PatternTokenTextSplitter: {
        splitPattern: string;
    };
    LlmTextSplitter: {
        windowSize: number;
        modelName: string;
    };
};
interface StepData {
    fileType: FileType; // 上传文件类型

    file: any; // file类型时使用
    text?: string; // text时使用
    html?: string; // html时使用

    splitFileMethod?: SplitFileMethod; // 切分的方法
    methodData?: MethodData; // 切分的参数
    previewADocument?: string[]; // 预览文档
    fineTuneData?: string[]; // 微调数据
}
