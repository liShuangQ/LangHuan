# spring-boot-ai-chat-ollama

## 介绍

本项目基于springai+ollama实现本地模型对话，对话记忆，RAG搜索等功能。

## 部署

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