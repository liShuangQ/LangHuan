# 万卷琅嬛

## LangHuan

### 项目描述

- "琅嬛福地"，藏天下典籍。

### 部署

#### docker

##### 构建部署

- langhuan-service/src/main/resources/application.yml 配置文件中修改对应配置
- langhuan-web/config/webpack.dev.js 配置文件中修改proxy的对应配置
- 项目根目录执行 docker compose up -d (docker-compose up -d) 启动服务（服务启动后会在项目根目录下添加postgres-data文件夹，用于保存数据库数据）
- 执行 docker compose down(docker-compose down) 停止服务
- 地址：服务器ip:9088 初始账号/密码： admin/123456

##### 打包部署

- langhuan-service/src/main/resources/application.yml 配置文件中修改对应配置
- langhuan-web/.env 修改BASE_URL为指向'service'(对应langhuan_docker_dist/langhuan-web/nginx.conf配置)
- 将langhuan-service打包成jar包后放置在langhuan_docker_dist/langhuan-service文件夹下（已有可忽略）
- 将langhuan-web打包langhuan文件夹后放置在langhuan_docker_dist/langhuan-web文件夹下，可删除stats.json（已有可忽略）
- 将langhuan-service/src/main/resources/sql文件夹复制在langhuan_docker_dist文件夹下（已有可忽略）
- 执行 docker compose up -d (docker-compose up -d) 启动服务（服务启动后会在目录下添加postgres-data文件夹，用于保存数据库数据）
- 地址：服务器ip:9088 初始账号/密码： admin/123456

#### 本地部署

- 本地部署请参考langhuan-service和langhuan-web的README.md

### 功能日志

- [x] 第一个对话例子，对话记忆 （20241211）
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
- [x] RAG召回测试（20250316）
- [x] 添加docker部署方式，优化用户角色权限缓存（20250327）
- [x] 修复页码BUG，添加提示词优化功能（20250331）
- [x] 添加单独的文字添加向量页面，修复会多次询问模型的bug，优化代码（20250401）
- [x] 优化模型和RAG的输出，添加更详细的召回信息，增加重排序（Re-ranking）召回测试中文档的点踩机制（20250402）
- [x] 对话框支持md格式（20250403）
- [x] 优化内存等处理方式(大文档拆分 向量) （20250410）
- [x] 对话窗口引用文档查看和点踩 （20250411）
- [x] 对话的反馈机制（20250428）
- [x] 升级springai 1.0.0-M7版本，文件管理中支持针对单条向量修改和删除，反馈中的向量片段修改（20250507）
- [x] 升级springai 1.0.0-M8版本，支持在添加文字中针对文件增加文本并且向量化，支持在召回中直接修改文本向量化（20250513）
- [x] 升级springai 1.0.0-RC1版本，支持对话记忆，聊天记录保存记忆等。（20250516）
- [ ] rag的ETL管道优化
- [ ] 添加量化指标，例如：共计提问次数xxx次，节约人工回答xxx次，节约成本时间xxx小时
- [ ] 完善rag，文件管理中可针对文件添加向量
- [ ] 文档中支持图片
- [ ] 补全权限机制
- [ ] 对话回答记录查找的向量文本，对话窗口做点赞点踩-针对对话本身
- [ ] 增加记录对话的点踩（存-踩的） 针对问题和回答，统计踩的问题（提示 要不要存入）
- [ ] 窗口和对话的记忆（暂时不持久化） -- 等待springai-1.0.0-M7版本，添加redis缓存 https://github.com/spring-projects/spring-ai/pull/2295
- [ ] 是否命中缓存
- [ ] 持续优化文档拆分模型，建立文档覆盖度机制
- [ ] 模仿MOE专家模式，分析问答 给出答案
- [ ] 针对问题调度工具简单处理