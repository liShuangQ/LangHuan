# langhuan-service

## 介绍

"琅嬛福地"，藏天下典籍

## 部署

### 基本环境

- 推荐使用 jdk21
- postgres数据库+pgvector+minio+模型
- 启动时添加虚拟机选项 --add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED

### 部署

#### ollama(可选)

Ollama 是一个基于 Go 语言开发的开源工具，能让用户在本地便捷地管理和运行如 Llama、Falcon、Qwen2 等多种大型语言模型，具有自动硬件加速、无需虚拟化、支持
API、多平台兼容等特性。

- 下载 ollama [https://ollama.com/](ollama)
- 通过 ollama run xxx 下载需要的模型 [https://ollama.com/library/](library)

```text
ollama run qwen2.5:3b (例子，使用其它模型在 https://ollama.com/library 中查询下载后修改application.yml中模型配置)
```

- 在application.yml中修改对应配置

#### one api(可选)

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
docker run -it -d --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres pgvector/pgvector
```

#### minio

- docker pull minio/minio:RELEASE.2025-04-22T22-12-26Z

```txt
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minio \
  -e MINIO_ROOT_PASSWORD=minio123456 \
  -v /etc/localtime:/etc/localtime:ro \
  -v /usr/share/zoneinfo/Asia/Shanghai:/etc/timezone:ro \
  minio/minio:RELEASE.2025-04-22T22-12-26Z server /data --console-address ":9001"

```

## 系统环境需求

### 技术栈配置

- **Java运行环境**: JDK 21（LTS版本）
- **Spring Boot**: 3.4.1-SNAPSHOT
- **数据库**: PostgreSQL with pgvector插件（向量数据库）
- **AI框架**: Spring AI 1.0.0
- **文件存储**: MinIO对象存储
- **构建工具**: Maven

### 核心功能模块

1. AI对话系统（多模型支持）
2. RAG知识库（BM25+向量混合检索）
3. 文件管理与文档处理
4. 用户权限管理（RBAC）
5. API日志与统计分析

## 硬件配置建议（并发量100）

### 最低配置

- **CPU**: 4核心 2.4GHz+
- **内存**: 8GB RAM
- **存储**: 100GB SSD
- **网络**: 100Mbps带宽

### 推荐配置

- **CPU**: 8核心 3.0GHz+
- **内存**: 16GB RAM
- **存储**: 200GB NVMe SSD
- **网络**: 1Gbps带宽

### 详细配置分析

**内存分配**:

- JVM堆内存: 4-6GB
- PostgreSQL: 2-3GB（向量索引优化）
- 系统预留: 6GB

**存储需求**:

- 应用程序: 500MB
- 日志文件: 10GB（30天保留）
- 数据库: 25GB（含向量数据）
- 文件存储: 50GB
- 系统增长预留: 30%

**数据库优化配置**:

```yaml
shared_buffers: 2GB
max_connections: 200
work_mem: 64MB
```

**连接池配置**:

```yaml
maximum-pool-size: 20
minimum-idle: 20
connection-timeout: 20000
```

**JVM参数**:

```bash
-Xms4g -Xmx6g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### 性能监控指标

- CPU使用率: <70%（峰值）
- 内存使用率: <80%
- 响应时间: <500ms（95th percentile）
- 数据库连接: <80%使用率

## 扩展性建议

### 水平扩展

- 支持多实例部署（需要会话共享）
- 数据库读写分离
- 添加Redis缓存层
- 负载均衡（Nginx/HAProxy）

### 垂直扩展

- 根据业务增长升级硬件配置
- 优化数据库索引和查询
- 调整JVM参数和连接池配置

该配置可稳定支持100并发用户，并预留扩展空间。如需更高并发，建议增加服务器实例或升级硬件配置。
