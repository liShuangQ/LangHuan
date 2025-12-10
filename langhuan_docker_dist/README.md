# LangHuan Docker 部署指南

## 项目架构

LangHuan 是一个可基于 Docker Compose 的多服务应用，包含以下组件：

- **Web 服务** (nginx): 前端静态文件服务和反向代理
- **App 服务** (Spring Boot): 后端应用服务
- **Database 服务** (PostgreSQL + pgvector): 数据库服务，支持向量存储
- **MinIO 服务**: 对象存储服务

## 服务端口配置

| 服务  | 内部端口  | 外部端口  | 说明                        |
| ----- | --------- | --------- | --------------------------- |
| web   | 9088      | 9088      | 前端服务                    |
| app   | 9077      | 9077      | 后端 API 服务               |
| db    | 5432      | 5432      | PostgreSQL 数据库           |
| minio | 9000/9001 | 9000/9001 | MinIO 对象存储 (API/控制台) |

## 快速部署

### 1. 环境要求

- Docker Engine 20.10+
- Docker Compose 2.0+
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

### 2. 部署步骤

#### 启动所有服务

```bash
# 进入部署目录
cd langhuan_docker_dist

# 启动所有服务
docker-compose up -d
```

#### 查看服务状态

```bash
# 查看所有容器状态
docker-compose ps

# 查看服务日志
docker-compose logs -f [service_name]
```

### 3. 访问应用

- **前端应用**: http://localhost:9088
- **后端 API**: http://localhost:9077
- **MinIO 控制台**: http://localhost:9001
  - 用户名: `minio`
  - 密码: `xxxxxx`

## 服务管理

### 重启特定服务

使用提供的重启脚本：

```bash
# 重启后端和前端服务
./restart_services.sh
```

或手动重启：

```bash
# 重启后端服务
docker-compose stop app
docker-compose rm -f app
docker-compose up -d --no-deps --build app

# 重启前端服务
docker-compose stop web
docker-compose rm -f web
docker-compose up -d --no-deps --build web
```

### 停止所有服务

```bash
docker-compose down
```

### 完全清理（包括数据卷）

```bash
docker-compose down -v
docker system prune -f
```

### 建议的日常维护命令

- 清理悬空镜像
docker image prune -f

- 清理停止的容器（可选）
docker container prune -f

- 清理无用网络（可选）
docker network prune -f

- 清理所有未使用的资源（镜像、容器、网络、构建缓存）
docker system prune -f


## 数据持久化

项目使用以下数据卷进行数据持久化：

- `./postgres-data`: PostgreSQL 数据目录
- `./minio-data`: MinIO 数据目录
- `./logs`: 应用日志目录
- `./sql`: 数据库初始化脚本目录

## 数据库备份

### 自动备份配置

1. 修改备份脚本中的路径配置：
   ```bash
   vim backup_db/start.sh
   ```

2. 设置定时任务：
   ```bash
   crontab -e
   # 添加以下行（例如每天凌晨2点执行备份）
   0 2 * * * /path/to/langhuan_docker_dist/backup_db/start.sh
   ```

### 手动备份

```bash
# 执行备份脚本
./backup_db/start.sh
```

备份文件将保存在配置的备份目录中，自动清理30天前的备份文件。

## 环境变量配置

### 数据库配置

- `POSTGRES_USER`: postgres
- `POSTGRES_PASSWORD`: xxxxxx
- `SPRING_DATASOURCE_URL`: jdbc:postgresql://db:5432/postgres?serverTimezone=Asia/Shanghai&useTimezone=true

### MinIO 配置

- `MINIO_ROOT_USER`: minio
- `MINIO_ROOT_PASSWORD`: xxxxxx

## 故障排除

### 常见问题

1. **端口冲突**
   ```bash
   # 检查端口占用
   lsof -i :9088
   lsof -i :9077
   lsof -i :5432
   lsof -i :9000
   lsof -i :9001
   ```

2. **容器启动失败**
   ```bash
   # 查看详细日志
   docker-compose logs [service_name]
   ```

3. **数据库连接失败**
   ```bash
   # 检查数据库容器状态
   docker-compose ps db
   # 查看数据库日志
   docker-compose logs db
   ```

4. **磁盘空间不足**
   ```bash
   # 清理未使用的 Docker 资源
   docker system prune -f
   docker volume prune -f
   ```

### 重建服务

如果遇到严重问题，可以重建特定服务：

```bash
# 重建并启动特定服务
docker-compose up -d --build [service_name]

# 重建所有服务
docker-compose up -d --build
```

## 生产环境建议

1. **安全配置**
   - 修改默认密码
   - 配置防火墙规则
   - 使用 HTTPS

2. **性能优化**
   - 调整 JVM 参数
   - 配置数据库连接池
   - 设置适当的资源限制

3. **监控和日志**
   - 配置日志轮转
   - 设置监控告警
   - 定期备份数据

4. **高可用性**
   - 配置数据库主从复制
   - 使用负载均衡
   - 设置健康检查

## 更新部署

当有新版本时：

1. 停止服务：`docker-compose down`
2. 更新镜像文件
3. 重新构建：`docker-compose up -d --build`
4. 验证服务状态：`docker-compose ps`

---

如有问题，请查看日志文件或联系技术支持。
