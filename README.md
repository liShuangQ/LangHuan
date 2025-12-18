# LangHuan(万卷琅嬛)

LangHuan 是一个基于 Kotlin + Spring AI 的智能对话与知识检索系统，集成了 RAG 技术，为个人和小团队提供知识管理和智能问答的一站式解决方案。

`琅嬛福地，藏天下典籍。`

## 功能特色

### 🤖 智能对话系统
- **个人知识空间**：为每个用户提供独立的知识库管理空间
- **对话记忆功能**：智能保存和管理对话上下文，提供连贯的交互体验
- **深度思考模式**：支持模型深度推理输出，提供更详细的分析过程
- **图片文档识别**：支持多模态模型，可识别图片和文档，提供更丰富的答案和体验
- **多专家模式对话**：支持多角色协作对话，模拟专家团队讨论
- **移动端适配**：响应式布局设计，自适应手机/平板等设备，移动端友好的界面交互

### 📚 RAG知识检索
- **智能文档处理**：自动提取文档图片并进行存储，提供图文并茂的答案
- **混合检索技术**：结合向量检索、BM25全文检索、分数融合和重排序
- **重排序机制**：支持ReRank模型，优化检索结果排序
- **多格式文档支持**：支持Md、Word、HTML、纯文本等多种文档格式
- **召回测试工具**：提供可视化的检索效果测试和优化工具

### 🗂️ 文件管理系统
- **文件组织管理**：支持文件分组、标签化管理
- **权限控制**：细粒度的文件访问权限控制，支持公开/私有设置
- **文件共享**：支持用户间文件组共享，便于协作
- **向量化管理**：支持单条向量的编辑、删除和优化
- **批量导入导出**：支持RAG文档的批量导入导出功能

### 👥 用户权限系统
- **角色权限管理**：接口级别，完整的RBAC权限控制体系
- **JWT认证**：安全的用户认证和授权机制
- **权限缓存优化**：高效的权限验证和缓存机制
- **多用户支持**：支持多用户并发使用，数据隔离

### 🔧 系统管理功能
- **提示词管理**：可配置的提示词模板和优化工具
- **模型配置**：支持多种OpenAI兼容模型的配置和切换
- **消息通知系统**：完整的系统消息推送和通知机制
- **使用统计仪表盘**：详细的系统使用情况统计和分析
- **MinIO对象存储**：集成对象存储，知识文件定期备份等功能

### 🐳 部署运维
- **轻量化部署**：适合个人和小团队快速部署使用
- **Docker容器化**：可选的完整的Docker部署方案
- **数据库备份**：自动化的数据备份和恢复机制
- **定时任务管理**：定时清理临时文件等维护任务
- **日志管理**：完善的系统日志记录和管理


## 部署

### 推荐使用
 [Docker部署方案](./langhuan_docker_dist/README.md)
### 基于源码部署：
 [前端本地部署](./langhuan-web/README.md) |  [后端本地部署](./langhuan-server/README.md) 

## 部分功能截图

![image.png](./readme/chat.png)
![image.png](./readme/chatmobile.png)
![image.png](./readme/dashboard.png)


## 重点功能流程(可能更新不及时)
### 对话流程
```mermaid
flowchart TD
    Start([开始: chat方法调用]) --> Init{初始化}
    Init --> FileCheck{检查附件}

    %% 文件处理分支
    FileCheck -->|有附件| FileCategorize[FileUtil.categorizeFiles<br/>文件分类处理]
    FileCategorize --> ImageProcessing{有图片文件?}
    FileCategorize --> DocProcessing{有文档文件?}

    %% 图片处理流程
    ImageProcessing -->|有图片| ImageUnderstanding[ImgService.chat_imageUnderstanding<br/>图片理解处理]
    ImageUnderstanding --> ImageStore[存储图片到MinIO<br/>生成图片URL]
    ImageStore --> ExtractImageText[提取图片理解结果]

    %% 文档处理流程
    DocProcessing -->|有文档| DocumentRead[读取文档字节内容]
    DocumentRead --> TikaParse[TikaDocumentReader<br/>文档内容解析]
    TikaParse --> DocContentExtract[提取文档文本内容]

    %% 文件解读模式整合
    ExtractImageText --> BuildMemoryInput[构建记忆输入信息]
    DocContentExtract --> DocumentChat{文档内容是否为空?}
    DocumentChat -->|非空| DocumentUnderstand[toChat: 调用AI理解文档]
    DocumentChat -->|空| BuildMemoryInput
    DocumentUnderstand --> AddDocNames[添加文档名称到记忆输入]
    AddDocNames --> BuildMemoryInput

    %% 聊天记忆处理
    BuildMemoryInput --> AddUserMessage[添加用户消息到记忆输入]
    AddUserMessage --> StoreMemory[存储到ChatMemory]
    StoreMemory --> ReturnFileResult[返回文件解读结果<br/>ChatModelResult]
    ReturnFileResult --> End([结束])

    %% 无附件时的意图识别流程
    FileCheck -->|无附件| IntentionRecognition[ChatGeneralAssistanceService<br/>.chatIntentionClassifier<br/>意图识别]
    IntentionRecognition --> GetIntentionResult[获取意图识别结果]
    GetIntentionResult --> IntentionsCheck{意图类型判断}

    %% 普通聊天意图
    IntentionsCheck -->|chat| ToChat[toChat: 普通聊天处理]

    %% 添加到个人知识库意图
    IntentionsCheck -->|add_personal_knowledge_space| ProcessKnowledgeSpace[处理添加到个人知识库]

    %% 知识库处理流程
    ProcessKnowledgeSpace --> BuildThinking[构建模拟思考过程]
    BuildThinking --> ProcessTextDocument[ChatGeneralAssistanceService<br/>.documentSegmentation<br/>处理文本分段]
    ProcessTextDocument --> ProcessImageDocument{有图片文件?}
    ProcessImageDocument -->|有图片| ImgToText[ImgService.chat_imageUnderstandingToText<br/>图片转文本处理]
    ProcessImageDocument -->|无图片| SkipImageProcessing[跳过图片处理]

    %% 处理文档内容
    SkipImageProcessable --> ProcessDocFiles[处理文档文件]
    ImgToText --> ProcessDocFiles
    ProcessDocFiles --> SplitDocuments[使用配置拆分器<br/>拆分文档内容]
    SplitDocuments --> CollectDocuments[收集所有文档内容]
    CollectDocuments --> FilterDocuments[过滤空文档]
    FilterDocuments --> HasDocuments{是否有有效文档?}

    %% 知识库添加结果
    HasDocuments -->|有文档| AddToKnowledgeBase[RagService<br/>.addDocumentToMySpace<br/>添加到知识库]
    AddToKnowledgeBase --> AddSuccess{添加是否成功?}
    AddSuccess -->|成功| ReturnSuccess[返回成功消息]
    AddSuccess -->|失败| ReturnFail[返回失败消息]
    HasDocuments -->|无文档| ReturnNoContent[返回无内容消息]

    %% toChat处理流程
    ToChat --> CheckTools{启用工具函数?}
    CheckTools -->|启用| BuildTools[ToolCallbacks.from<br/>构建工具回调]
    CheckTools -->|未启用| EmptyTools[空工具数组]
    BuildTools --> CheckRag{启用RAG?}
    EmptyTools --> CheckRag
    CheckRag -->|启用RAG| RagChat[isRagChat: RAG聊天处理]
    CheckRag -->|未启用RAG| NoRagChat[noRagChat: 普通聊天处理]

    %% RAG聊天流程
    RagChat --> GetRAGPrompt[获取RAG提示词模板]
    GetRAGPrompt --> SearchDocuments[RagCallBackService<br/>.ragSearch<br/>搜索相关文档]
    SearchDocuments --> BuildRAGContext[构建RAG上下文<br/>拼接文档内容]
    BuildRAGContext --> ReplaceTemplate[替换提示词模板变量]
    ReplaceTemplate --> SendAIRequest_RAG[发送AI请求<br/>包含RAG上下文和工具]
    SendAIRequest_RAG --> GetAIResponse_RAG[获取AI响应]
    GetAIResponse_RAG --> BuildRAGResult[构建包含RAG文档的结果]
    BuildRAGResult --> End

    %% 普通聊天流程
    NoRagChat --> BuildNormalPrompt[构建普通提示词]
    BuildNormalPrompt --> SendAIRequest_Normal[发送AI请求<br/>不包含RAG上下文]
    SendAIRequest_Normal --> GetAIResponse_Normal[获取AI响应]
    GetAIResponse_Normal --> BuildNormalResult[构建普通聊天结果]
    BuildNormalResult --> End

    %% 异常处理
    CatchException[捕获异常] --> HandleException[处理异常<br/>构建错误响应]
    HandleException --> End

    %% 样式定义
    classDef serviceClass fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef utilClass fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef decisionClass fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef processClass fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef resultClass fill:#fce4ec,stroke:#880e4f,stroke-width:2px

    %% 应用样式
    class FileCategorize,ImageUnderstanding,TikaParse,ImgToText utilClass
    class ChatGeneralAssistanceService,RagService,RagCallBackService serviceClass
    class FileCheck,ImageProcessing,DocProcessing,DocumentChat,IntentionsCheck,CheckTools,CheckRag,HasDocuments,AddSuccess decisionClass
    class SearchDocuments,BuildRAGContext,SendAIRequest_RAG,SendAIRequest_Normal processClass
    class ReturnFileResult,ReturnSuccess,ReturnFail,ReturnNoContent,BuildRAGResult,BuildNormalResult resultClass
```
### 检索流程
```mermaid
flowchart TD
    A([开始: ragSearch调用]) --> B[并行执行双重检索]

    %% 向量检索分支
    B --> C[向量检索: embeddingCallBack]
    C --> D{是否有groupId过滤?}

    D -->|无groupId| E[全局向量搜索<br/>ragVectorStore.similaritySearch]
    D -->|有groupId| F[构建过滤表达式]

    F --> G{单个groupId?}
    G -->|是| H["过滤: fileId == '$fileId'"]
    G -->|否| I["过滤: groupId IN (id1, id2, ...)"]

    H --> J[带过滤条件的向量搜索]
    I --> J
    E --> K[向量检索结果]
    J --> K

    %% BM25检索分支
    B --> L[BM25全文检索: bm25CallBack]
    L --> M[Lucene全文索引搜索]
    M --> N[获取BM25搜索结果ID列表]
    N --> O[根据ID查询向量库]
    O --> P[构建包含BM25分数的文档]
    P --> Q[BM25检索结果]

    %% 分数融合
    K --> R[mergeDocumentsWithScores<br/>分数融合处理]
    Q --> R

    R --> S[构建文档映射表]
    S --> T[处理向量检索文档]
    T --> U[补充缺失的BM25分数]
    U --> V[处理BM25检索文档]
    V --> W[补充缺失的向量分数]
    W --> X[确保所有文档有完整分数]
    X --> Y[融合后的文档列表]

    %% 线性加权排序
    Y --> Z[rankLinearWeighting<br/>线性加权排序]

    Z --> AA[提取三种分数类型]
    AA --> BB[Spring AI向量分数]
    AA --> CC[BM25关键词分数]
    AA --> DD[手工排名分数]

    BB --> EE[Min-Max归一化处理]
    CC --> EE
    DD --> EE

    EE --> FF[计算加权综合得分]
    FF --> GG[更新元数据存储分数]
    GG --> HH[按综合得分降序排序]
    HH --> II[加权排序后的文档列表]

    %% 重排序分支
    II --> JJ{是否启用重排序?}

    JJ -->|启用| KK[重排序处理]
    KK --> LL[选择Top K文档]
    LL --> MM[调用AI重排序模型]
    MM --> NN[AI模型重排序]
    NN --> OO[最终重排序结果]

    JJ -->|未启用| PP[分数过滤处理]
    PP --> QQ[根据阈值过滤]
    QQ --> RR[最终过滤结果]

    %% 最终处理
    OO --> SS[限制结果数量]
    RR --> SS
    SS --> TT([结束: 返回搜索结果])

    %% 样式定义
    classDef searchMethod fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef fusionMethod fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef rankingMethod fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef dataFlow fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef decisionClass fill:#fce4ec,stroke:#c2185b,stroke-width:2px

    class C,L,R,Z,KK searchMethod
    class S,T,U,V,W,FF fusionMethod
    class EE,HH,NN rankingMethod
    class K,Q,Y,II,OO,RR dataFlow
    class D,G,JJ decisionClass
```

## 功能发展历程

### 核心功能建设期 (2024.12 - 2025.01)
- [x] 基础对话功能和记忆机制 (2024.12)
- [x] RAG检索问答系统 (2024.12)
- [x] 函数调用功能实现 (2024.12)
- [x] JWT用户认证授权 (2024.12)
- [x] 前端工程页面开发 (2024.12)
- [x] 用户角色权限体系 (2024.12)
- [x] 聊天记录管理 (2024.12)
- [x] OpenAI兼容模型接入 (2025.01)

### 功能扩展期 (2025.02 - 2025.03)
- [x] Spring AI框架升级优化 (2025.03)
- [x] 提示词配置管理 (2025.03)
- [x] 文件组和文件管理 (2025.03)
- [x] RAG召回测试工具 (2025.03)
- [x] Docker部署方案 (2025.03)

### 体验优化期 (2025.04 - 2025.05)
- [x] 提示词优化功能 (2025.03)
- [x] 文字向量化管理 (2025.04)
- [x] ReRank重排序机制 (2025.04)
- [x] Markdown格式支持 (2025.04)
- [x] 大文档处理优化 (2025.04)
- [x] 文档引用和反馈 (2025.04-05)
- [x] Spring AI 1.0正式版 (2025.05)
- [x] 权限控制完善 (2025.05)
- [x] RAG文件导出功能 (2025.05)

### 功能完善期 (2025.06 - 2025.08)
- [x] ReRank模型集成 (2025.06)
- [x] 深度思考模式 (2025.06)
- [x] 消息通知系统 (2025.06)
- [x] 权重计算优化 (2025.07)
- [x] 仪表盘统计功能 (2025.07)
- [x] 用户信息完善 (2025.07)
- [x] 文件组权限共享 (2025.07)
- [x] MinIO对象存储 (2025.08)
- [x] 多专家模式对话 (2025.08)
- [x] 个人知识空间 (2025.08)
- [x] 新增图片和文档的多模态识别 (2025.08)
- [x] 混合检索技术 (2025.10)
- [x] 移动端适配，更多的使用细节处理 (2025.12)

### 规划中功能
- [ ] 移除funcation tool代码，转为全面对MCP的支持，实现MCP热插拔
- [ ] 计划开发与之配合的MCP服务
- [ ] 提供针对模型等信息的配置页面，可自由更换模型api，rag配置，权重等
- [ ] 添加更多的拆分文档方式（在全文中划线拆分chunk）
- [ ] 不满意的回答过滤文档后重新回答
- [ ] 在对话中标记文档，记录被调用次数多并且满意的文档，区分高低价值 便于维护
- [ ] 支持调用更多的服务解析文档



## 开源协议

本项目采用 **Apache License 2.0** 许可协议，详情见 `LICENSE` 文件。使用前请务必遵守协议条款。

## 特别声明

1. 作者不对因使用本项目代码引发的任何法律风险或技术问题承担责任。
2. 本项目与作者任职单位（如有）无关，非职务作品，未利用雇主资源、技术文档或商业信息。

## 联系与支持

如有问题，请联系开发团队或提交 Issue。
