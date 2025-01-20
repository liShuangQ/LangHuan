# spring-boot-ai-chat-ollama

## 介绍

本项目主要基于springai+ollama实现本地模型对话，对话记忆，RAG搜索，工具调用等功能。
也可通过设置 支持openai api的使用。


- 基础专业问答-RAG实现专业问答-构建专家库
- 文件审批（查重）-工具调用-通过算法实现
- 录入九天能力清单（专家库的一种）
- 重点 定位上九天   解决九天不能用的一部分



## 待做

- [ ] 实验RAG问题识别 给出建议 根据建议调用接口(根据建议强匹配) 返回结果
- [ ] 完善RAG文件组 文件通用组(在代码中默认已选定一组即可)  文件上传
- [ ] 优化Tool工具组 工具通用组(在代码中默认已选定一组即可) 工具     设计函数确认执行方式 设计支持强匹配方式
- [ ] 添加设计接口调用工具（计划：rag中取出 url 参数格式等，ai分析出入参和url等，结果直接返回(需要等待springai的优化)）
- [ ] 如果使用rag方式：接口组（使用rag文件组），添加接口，接口管理工具
- [ ] 添加工具链编排机制 编排页面 配合页面设计一个list，实现接口零代码链条调用输出
- [ ] 完善日志机制，记录token等
- [ ] 完善用户权限机制 如限制token数，关联rag和tool组等
- [ ] 图像识别-截图表单录入

## 部署

- 推荐使用 jdk21
- 下载 ollama [https://ollama.com/](ollama)
- 下载模型和嵌入模型

```text
ollama run qwen2.5:3b (默认配置，使用其它模型在 https://ollama.com/library 中查询下载后修改application.yml中模型配置)
ollama pull mxbai-embed-large（默认配置） 或 ollama pull mofanke/dmeta-embedding-zh（需要在application.yml中单独配置）
执行 ollama list 检查是否下载成功
```

- 下载 postgres pgvector/pgvector

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

## HELP（Getting Started）

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.1-SNAPSHOT/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.1-SNAPSHOT/maven-plugin/build-image.html)
* [Ollama](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.1-SNAPSHOT/reference/web/servlet.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.