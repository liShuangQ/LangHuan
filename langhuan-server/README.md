# langhuan-server

## 项目概述

琅嬛福地是基于 Spring Boot 3.5.6 + Kotlin 1.9.25 的 AI 知识管理系统，提供 RAG（检索增强生成）功能、文档处理和对话 AI 特性。系统集成了多种 AI 模型，支持结合 BM25 和向量搜索的混合检索策略。

## 系统要求

### 基础环境
- **Java**: JDK 21（LTS版本）
- **操作系统**: Linux/macOS/Windows
- **内存**: 最低 8GB，推荐 16GB+
- **存储**: 最低 50GB 可用空间，推荐 100GB+

### 依赖服务
- **PostgreSQL**: 15+ (需要 pgvector 插件)
- **MinIO**: 对象存储服务
- **AI模型(包括嵌入模型)**: OpenAI兼容API或本地Ollama

## 快速部署指南

### 第一步：环境准备

#### 1.1 安装 JDK 21
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# macOS (使用 Homebrew)
brew install openjdk@21

# 验证安装
java -version
```

#### 1.2 安装 PostgreSQL + pgvector
```bash
# Ubuntu/Debian
sudo apt install postgresql-15 postgresql-contrib postgresql-15-pgvector

# macOS (使用 Homebrew)
brew install postgresql@15 pgvector

# 启动 PostgreSQL
sudo systemctl start postgresql  # Linux
brew services start postgresql@15  # macOS

# 或者使用 Docker
docker pull postgres pgvector/pgvector
docker run -it -d --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=xxxxxx pgvector/pgvector

```

#### 1.3 配置 PostgreSQL
```sql
-- 连接数据库
sudo -u postgres psql

-- 创建数据库和用户
CREATE DATABASE langhuan_db;
CREATE USER langhuan_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE langhuan_db TO langhuan_user;

-- 启用 pgvector 扩展
\c langhuan_db;
CREATE EXTENSION IF NOT EXISTS vector;
```

#### 1.4 安装和配置 MinIO
```bash
# 下载 MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio

# 创建数据和配置目录
mkdir -p ~/minio/data

# 启动 MinIO (端口 9000 API, 9001 Console)
./minio server ~/minio/data --console-address ":9001"

# 或者使用 Docker
docker pull minio/minio:RELEASE.2025-04-22T22-12-26Z
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minio \
  -e MINIO_ROOT_PASSWORD=your_password \
  -v ~/minio/data:/data \
  minio/minio:RELEASE.2025-04-22T22-12-26Z server /data --console-address ":9001"
```

### 第二步：项目部署

#### 2.1 克隆项目
```bash
git clone <repository-url>
cd langhuan-server
```

#### 2.2 配置应用参数
编辑 `src/main/resources/application.yml`：

```yaml
server:
  port: 9077

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/langhuan_db?serverTimezone=Asia/Shanghai&useTimezone=true
    username: langhuan_user
    password: your_password
    driver-class-name: org.postgresql.Driver

  ai:
    openai:
      base-url: https://dashscope.aliyuncs.com/compatible-mode  # 或你的模型API
      api-key: your-api-key
      chat:
        options:
          model: qwen-plus  # 或其他模型
      embedding:
        options:
          model: text-embedding-v3

minio:
  url: http://localhost:9000
  access-key: minio
  secret-key: your_password
  img-bucket-name: langhuan-img
  file-bucket-name: langhuan-file

# 其他配置保持默认...
```

#### 2.3 创建数据库表结构
```bash
# 执行 SQL 文件（按顺序执行）
src/main/resources/sql/ddl_1.sql
src/main/resources/sql/admin_dml_2.sql
src/main/resources/sql/api_log_ddl_3.sql
src/main/resources/sql/prompt_dml_4.sql
src/main/resources/sql/index_dlc.sql
```

#### 2.4 构建和启动应用
```bash
# 方式一：直接运行
./gradlew bootRun

# 方式二：构建后运行
./gradlew build -x test
java -jar build/libs/langhuan-server-0.0.1-SNAPSHOT.jar
```

#### 2.5 启动参数（推荐）
```bash
# 完整启动命令（包含必要的 JVM 参数）
java \
  --add-modules jdk.incubator.vector \
  --enable-native-access=ALL-UNNAMED \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar build/libs/langhuan-server-0.0.1-SNAPSHOT.jar
```

### 第三步：验证部署

#### 3.1 检查应用启动
```bash
# 检查应用是否启动成功
curl -X POST http://localhost:9077/user/search \
  -H "Content-Type: application/json" \
  -d '{}'

# 或查看日志
tail -f logs/langhuan.log
```

#### 3.2 验证 Web 界面
- **MinIO 控制台**: http://localhost:9001
  - 用户名: `minio`
  - 密码: `xxxxxx`

#### 3.3 数据库验证
```sql
-- 检查表是否创建成功
\dt

-- 检查向量表
SELECT * FROM vector_store_rag LIMIT 1;

-- 检查用户表
SELECT * FROM t_user;
```

