# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

"琅嬛福地" (Langhuan Service) 是基于 Spring Boot 的 AI 知识管理系统，提供 RAG（检索增强生成）功能、文档处理和对话 AI 特性。系统集成了多种 AI 模型，支持结合 BM25 和向量搜索的混合检索策略。

## 技术栈

- **开发语言**: Kotlin 1.9.25 with Java 21
- **框架**: Spring Boot 3.5.6
- **数据库**: PostgreSQL + pgvector 向量扩展
- **ORM**: MyBatis Plus 3.5.7
- **AI 集成**: Spring AI 1.0.3 + OpenAI 兼容 API
- **存储**: MinIO 文件存储
- **构建工具**: Maven 3.9.11
- **安全**: Spring Security + JWT 认证

## 开发要求与规范

### 技术专长
您是 Kotlin 编程、Spring Boot、Spring 框架、PostgreSQL、Maven 及相关 Kotlin 技术领域的专家。同时熟练使用 Hutool、Lombok 工具库。

### 代码风格与结构
- 编写简洁、高效且文档完善的 Kotlin 代码，并搭配精准的 Spring Boot 示例
- 在代码中遵循 Spring Boot 的最佳实践和规范
- 创建 Web 服务时，采用 RESTful API 设计模式。**所有服务使用@PostMapping 注解**（即全部使用 POST 请求，接口名使用 `xx/search`、`xx/delete`、`xx/update`、`xx/add` 等方式来区分）
- 按照驼峰命名法（camelCase）使用具有描述性的方法名和变量名
- 构建 Spring Boot 应用程序的标准结构：控制器、服务、仓库、模型和配置
- **服务类（Service）直接编写实现，无需编写接口类**

### 注释要求
- **类和方法必须包含 KDoc 注释**
- 复杂逻辑代码块需要行内注释
- 公共 API 必须有清晰的使用说明
- 无需编写独立的功能使用文档

### Spring Boot 特定要求
- 使用 Spring Boot 启动器进行项目的快速搭建和依赖管理
- 正确使用注解（如@SpringBootApplication、@RestController、@Service）
- 有效利用 Spring Boot 的自动配置功能
- 使用@ControllerAdvice 和@ExceptionHandler 实现恰当的异常处理

### 命名规范
- **类名**: 帕斯卡命名法（PascalCase），如 `UserController`、`OrderService`
- **方法名和变量名**: 驼峰命名法（camelCase），如 `findUserById`、`isOrderValid`
- **常量**: 全大写命名法（ALL_CAPS），如 `MAX_RETRY_ATTEMPTS`、`DEFAULT_PAGE_SIZE`

### Kotlin 和 Spring Boot 的使用
- 运用 Spring Boot 3.x 的特性和最佳实践
- 使用 Bean 验证（如@Valid、自定义验证器）实现恰当的验证

### 配置与属性
- 使用 application.properties 或 application.yml 进行配置
- 利用 Spring Profiles 实现特定环境的配置
- 使用@ConfigurationProperties 实现类型安全的配置属性

### 依赖注入和控制反转（IoC）
- **为了提高可测试性，优先使用构造函数注入而非字段注入**
- 借助 Spring 的 IoC 容器管理 Bean 的生命周期

### 性能与可扩展性
- 使用 Spring Cache 抽象实现缓存策略
- 使用@Async 进行异步处理，实现非阻塞操作
- 实施恰当的数据库索引和查询优化

### 安全
- 使用 Spring Security 实现身份验证和授权
- 使用合适的密码编码方式（如 BCrypt）

### 日志记录与监控
- **使用 `companion object { private val log = LoggerFactory.getLogger(类名::class.java) }` 进行日志记录**
- 设定恰当的日志级别（ERROR、WARN、INFO、DEBUG）

### 数据访问与对象关系映射（ORM）
- **使用 mybatis、mybatis-plus、JdbcTemplate 进行数据库操作**
- 实现恰当的实体关系和级联操作

### 构建与部署
- 使用 Maven 进行依赖管理和构建过程
- 在 Spring Boot 应用程序设计中，遵循 SOLID 原则，保持高内聚和低耦合

## 开发命令

### 构建与运行
```bash
# 清理并编译
./mvnw clean compile

# 运行测试
./mvnw test

# 打包应用
./mvnw package

# 运行应用（开发环境）
./mvnw spring-boot:run

# 运行应用（生产环境）
java -jar target/langhuan-service-0.0.1-SNAPSHOT.jar
```
## 架构概览

### 核心包结构
```
com.langhuan/
├── config/          # 配置类（安全、AI、数据库等）
├── controller/      # REST API 端点
├── service/         # 业务逻辑服务
├── serviceai/       # AI 相关服务（聊天、RAG 等）
├── dao/            # 数据访问对象
├── model/          # 数据模型（domain、dto、pojo、vo）
├── utils/          # 工具类
├── functionTools/  # AI 函数调用工具
├── task/           # 定时任务
└── filter/         # 安全和请求过滤器
```

## 配置说明

### 应用配置
- 主配置文件: `src/main/resources/application.yml`
- 数据库: PostgreSQL + 时区配置
- AI 模型: 可配置的 OpenAI 兼容端点
- 文件存储: MinIO 桶分离配置

### 开发注意事项
- **所有 API 端点使用 POST 请求**，采用 RESTful 命名（如 `/user/add`、`/user/search`）
- 服务类直接实现逻辑，无需接口
- **优先使用构造函数注入而非字段注入**
- 使用 SLF4J 进行全面日志记录


## 代码风格指南总结
- 遵循 Kotlin 约定和 Spring Boot 最佳实践
- 公共 API 需要全面的文档说明
- 方法和变量使用 camelCase 命名
- 类名使用 PascalCase 命名
- 使用 companion object 进行日志记录和常量定义

## Task Master AI Instructions
**Import Task Master's development workflow commands and guidelines, treat as if import is in the main CLAUDE.md file.**
@./.taskmaster/CLAUDE.md
