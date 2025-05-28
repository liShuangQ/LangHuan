
## 技术栈
框架: Spring Boot 3.4.1, Spring AI
数据库: PostgreSQL, MyBatis-Plus
AI 集成: OpenAI 模型，PGVector 向量存储
安全认证: JWT
工具库: Hutool, FastJSON, Lombok

## Java 编码规范
### 命名约定:
- 类名使用大驼峰命名法 (e.g., UserController)
- 方法和变量使用小驼峰命名法 (e.g., getUserById)
- 常量使用全大写字母和下划线 (e.g., MAX_PAGE_SIZE)
### 注释要求:
- 类和方法必须包含 Javadoc 注释
- 复杂逻辑代码块需要行注释
- 公共 API 必须有清晰的使用说明
### 代码风格:
- 使用 4 个空格缩进
- 遵循 DRY (Don't Repeat Yourself) 原则
- 保持方法简短且单一职责
### 控制器编写
- 控制器方法应简洁，主要处理请求和响应
- 使用 @RestController 注解
- 路径映射使用 @PostMapping 等注解（即全部使用post请求）

## 数据库操作规范
### MyBatis-Plus 使用:
- 优先使用 Wrapper 构造条件查询
- 避免编写复杂 SQL，使用 MyBatis-Plus 提供的方法
- 复杂查询使用 JdbcTemplate

### 事务管理:
- 使用 @Transactional 注解管理事务
- 明确事务传播行为和隔离级别

## 异常处理:
- 使用统一异常处理器处理全局异常
- 自定义业务异常类
- 提供清晰的错误信息

## 日志记录:
- 使用 SLF4J 进行日志记录
- 避免记录敏感信息
- 为关键业务流程添加日志