type FileType = "text" | "file";
type SplitFileMethod =
    | "FixedWindowTextSplitter"
    | "PatternTokenTextSplitter"
    | "OpenNLPSentenceSplitter"
    | "LlmTextSplitter";
type MethodData = {
    FixedWindowTextSplitter: {
        windowSize: number;
    };
    PatternTokenTextSplitter: {
        splitPattern: string;
    };
    OpenNLPSentenceSplitter: {};
    LlmTextSplitter: {
        windowSize: number;
        modelName: string;
    };
};
interface StepData {
    fileType: FileType; // 上传文件类型

    file: any; // file类型时使用
    text?: any; // text时使用

    splitFileMethod?: SplitFileMethod; // 切分的方法
    methodData?: MethodData; // 切分的参数
    previewADocument?: any; // 预览文档
    fineTuneData?: any; // 微调数据
}
