# 万卷琅嬛

## langhuan-service

### 项目描述

- "琅嬛福地"，藏天下典籍
- langhuan-service 是一个基于 Spring Boot 框架构建的服务端应用程序。该项目旨在提供与人工智能（AI）相关的服务，并集成了多种功能和依赖库以支持其业务需求。

### 部署
#### docker
- langhuan-service/src/main/resources/application.yml 配置文件中修改对应配置
- langhuan-web/config/webpack.dev.js 配置文件中修改proxy的对应配置
- 项目根目录执行 docker-compose up -d 启动服务
- 执行 docker-compose down 停止服务
#### 本地部署
- 本地部署请参考langhuan-service和langhuan-web的README.md

### 功能日志

- [x] 第一个对话例子 （20241211）
- [x] 基本实现RAG的问答，可添加向量库 （20241218）
- [x] 基本实现函数调用的实现 （20241223）
- [x] 添加JWT用户的认证和授权 （20241224）
- [x] 添加langhuan-web前端工程页面 （20241225）
- [x] 完善用户，角色，权限等 （20241226）
- [x] 实现聊天记录并优化页面 （20241231）
- [x] 改用openai兼容格式的模型 （20250102）
- [x] 添加简易斯坦福小镇的探索对话服务和页面 （20250211）
- [x] spring-ai升级1.0.0-M6版本，工具类等改动 （20250304）
- [x] 提示词可配置添加数据库存储提示词并提供配置页面 （20250305）
- [x] 文件组管理，文件管理，文件切分上传向量库 （20250315）
- [x] 第一轮优化全部的现有功能，包括校验 边界情况等（20250315）
- [x] 召回测试（20250316）
- [x] 添加docker部署方式，优化用户角色权限缓存（20250327）
- [ ] 后续单独使用要在细化用户处