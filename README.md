# 万卷琅嬛
## langhuan-service
### 项目描述
langhuan-service 是一个基于 Spring Boot 框架构建的服务端应用程序。该项目旨在提供与人工智能（AI）相关的服务，并集成了多种功能和依赖库以支持其业务需求。
### 主要特性
- Spring Boot 版本：使用了最新的 Spring Boot 3.4.1-SNAPSHOT 版本，确保了对最新特性和性能优化的支持。
- Java 版本：项目使用 Java 21，利用了最新的语言特性和改进。
### AI 集成：
- 使用了 spring-ai-ollama-spring-boot-starter、spring-ai-pgvector-store-spring-boot-starter 和 spring-ai-tika-document-reader 等依赖，表明项目可能涉及自然语言处理、文档解析等 AI 相关功能。
- 安全性：集成了 spring-boot-starter-security 以提供安全机制，保护应用免受潜在的安全威胁。
- 数据库支持：使用了 PostgreSQL 数据库，并通过 spring-boot-starter-jdbc 提供了 JDBC 支持。
- JWT 认证：引入了 java-jwt 和 jjwt 依赖，用于实现 JSON Web Token (JWT) 的生成和验证，确保用户认证和授权的安全性。
### 工具库：
- 使用了 hutool-all，这是一个功能丰富的 Java 工具包，简化了开发过程中的常见操作。
- 引入了 fastjson 用于高效的 JSON 处理。
- 使用了 mybatis-plus-spring-boot3-starter 来简化 MyBatis 的配置和使用。
- Lombok：通过 Lombok 插件减少了样板代码的编写，提高了开发效率。
### 项目结构
- groupId: com.langhuan
- artifactId: langhuan-service
### 构建工具
- 项目使用 Maven 进行构建和管理，并配置了相应的插件来支持编译和打包过程，包括 Lombok 注解处理器和 Spring Boot 插件。
## langhuan-web
### 项目描述
langhuan-service的前端页面。此项目是一个基于 Webpack 5、TypeScript 和 Vue 3 的前端开发项目，旨在提供一个高效、灵活且易于维护的开发环境。它集成了多种流行的工具和库，以支持现代 Web 应用程序的开发。
### 主要功能和技术栈
构建工具
- Webpack 5：用于模块打包。
- Babel：用于编译现代 JavaScript 代码。
- ESLint：用于代码风格检查和错误检测。
- PostCSS：用于编写更简洁的 CSS 代码。
- TailwindCSS：用于快速构建自定义样式的设计系统。
### 框架和库
- Vue 3：用于构建用户界面的渐进式 JavaScript 框架。
- TypeScript：用于静态类型检查，提高代码质量。
- Element Plus：一套为开发者、设计师和产品经理准备的基于 Vue 3 的组件库。
- Pinia：Vue 3 的状态管理库。
- Axios：用于发起 HTTP 请求。
- Dayjs：轻量级的日期处理库。
- Lodash：常用的工具函数库。
### 开发辅助工具
- cross-env：用于跨平台设置环境变量。
- dotenv：用于加载环境变量。
- webpack-bundle-analyzer：用于分析打包后的文件大小。
- unplugin-auto-import 和 unplugin-vue-components：用于自动导入常用模块和组件。
### 脚本命令
- npm run build：生产环境打包。
- npm run dev：启动开发服务器。
- npm run analyze：分析打包文件大小。
- npm run lint：执行代码格式化和修复。
### 依赖项
- 开发依赖 (devDependencies)
- 编译工具：@babel/core, @babel/preset-env, @babel/preset-typescript
- Webpack 插件：html-webpack-plugin, mini-css-extract-plugin, eslint-webpack-plugin 等
- 样式处理：css-loader, postcss-loader, sass, sass-loader
- 其他：eslint, typescript, vue-loader 等
### 生产依赖 (dependencies)
- UI 组件库：element-plus, @element-plus/icons-vue
- 工具库：lodash, qs, dayjs
- 状态管理：pinia
- HTTP 请求：axios
- Vue 相关：vue, vue-router, @vueuse/core
