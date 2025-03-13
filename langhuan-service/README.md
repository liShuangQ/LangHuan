# spring-boot-ai-chat-ollama

## 介绍

本项目主要基于springai+oneapi+ollama实现本地模型对话，对话记忆，RAG搜索，工具调用等功能。

## 部署

### 基本环境

- 推荐使用 jdk21
- postgres数据库 （下方介绍使用docker的方式。也可自己准备，需要RAG则需要pgvector插件）
- oneapi
- ollama

### 部署

#### ollama

Ollama 是一个基于 Go 语言开发的开源工具，能让用户在本地便捷地管理和运行如 Llama、Falcon、Qwen2 等多种大型语言模型，具有自动硬件加速、无需虚拟化、支持
API、多平台兼容等特性。

- 下载 ollama [https://ollama.com/](ollama)
- 通过 ollama run xxx 下载需要的模型 [https://ollama.com/library/](library)

```text
ollama run qwen2.5:3b (例子，使用其它模型在 https://ollama.com/library 中查询下载后修改application.yml中模型配置)
```

- 在application.yml中修改spring.ai.ollama配置

#### one api

是一个开源的接口管理与分发系统，支持如 OpenAI、Google PaLM 2、百度文心一言等多种大模型平台。通过统一接口访问不同大模型服务，可用于二次分发管理
key，仅单可执行文件，已打包好 Docker 镜像，能一键部署，开箱即用。

`推荐使用作为模型管理工具，也可直接使用兼容openai api的模型（针对qwen等兼容更多的模型）`

```text
# 使用 SQLite 的部署命令：
docker run --name one-api -d --restart always -p 3000:3000 -e TZ=Asia/Shanghai -v /home/ubuntu/data/one-api:/data justsong/one-api
# 使用 MySQL 的部署命令，在上面的基础上添加 `-e SQL_DSN="root:123456@tcp(localhost:3306)/oneapi"`，请自行修改数据库连接参数，不清楚如何修改请参见下面环境变量一节。
# 例如：
docker run --name one-api -d --restart always -p 3000:3000 -e SQL_DSN="root:123456@tcp(localhost:3306)/oneapi" -e TZ=Asia/Shanghai -v /home/ubuntu/data/one-api:/data justsong/one-api

# 默认管理员账号密码：root 123456
```
安装后，在oneapi中配置ollama的模型渠道，具体配置方式见官网。

#### 嵌入模型和向量数据库（RAG使用）

- 下载嵌入模型（ollama方式。如果使用云端模型直接在application填入对应地址即可）

```text
ollama pull mxbai-embed-large（默认配置） 或 ollama pull mofanke/dmeta-embedding-zh（需要在application.yml中单独配置）
执行 ollama list 检查是否下载成功
```

- 下载 postgres pgvector/pgvector （如果你有数据库则跳过这步，如没有pgvector则需要添加此插件）

```text
docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres pgvector/pgvector
```

- 执行创建向量库sql

```text
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store
(
    id        uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content   text,
    metadata  json,
    embedding vector(1536)
    );

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
ALTER TABLE vector_store ADD COLUMN embedding float[];
ALTER TABLE vector_store
ALTER COLUMN embedding TYPE vector USING embedding::vector;
```

## 待做

- [x] 提示词可配置 （20250305添加数据库存储提示词并提供配置页面）
- [ ] 实验RAG问题识别 给出建议 根据建议调用接口(根据建议强匹配) 返回结果
- [ ] 完善RAG文件组 文件通用组(在代码中默认已选定一组即可)  文件上传
- [ ] 优化Tool工具组 工具通用组(在代码中默认已选定一组即可) 工具 设计函数确认执行方式 设计支持强匹配方式
- [ ] 添加设计接口调用工具（计划：rag中取出 url 参数格式等，ai分析出入参和url等，结果直接返回(需要等待springai的优化)）
- [ ] 如果使用rag方式：接口组（使用rag文件组），添加接口，接口管理工具
- [ ] 添加工具链编排机制 编排页面 配合页面设计一个list，实现接口零代码链条调用输出
- [ ] 完善日志机制，记录token等
- [ ] 完善用户权限机制 如限制token数，关联rag和tool组等
- [ ] 图像识别-截图表单录入
