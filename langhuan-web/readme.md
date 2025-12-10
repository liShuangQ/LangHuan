# langhuan-web

## 介绍

"琅嬛福地"，藏天下典籍

## 技术栈说明

- WebPack 5.88.2 + TypeScript + Vue 3
- TailWindCss + Sass + Element Plus
- Axios + Pinia + Vue Router @4

## 部署步骤

### 方式一：开发环境部署

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd langhuan-web
   ```

2. **安装依赖**
   ```bash
   # 推荐使用 pnpm
   pnpm install

   # 或者使用 npm
   npm install

   # 或者使用 yarn
   yarn install
   ```

3. **启动开发服务器**
   ```bash
   # 使用 pnpm
   pnpm run dev

   # 或者使用 npm
   npm run dev

   # 或者使用 yarn
   yarn dev
   ```

4. **访问应用**
   默认情况下，应用会在本地启动，访问地址通常是 `http://localhost:8080`（具体地址请参考控制台输出）

### 方式二：生产环境构建部署

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd langhuan-web
   ```

2. **安装依赖**
   ```bash
   pnpm install
   ```

3. **构建生产版本**
   ```bash
   # 使用 pnpm
   pnpm run build

   # 或者使用 npm
   npm run build

   # 或者使用 yarn
   yarn build
   ```

4. **部署构建产物**
   - 构建完成后，所有产物将生成在 `dist` 目录下
   - 可以将该目录下的所有文件部署到任何静态文件服务器（如 Nginx、Apache 等）

5. **使用 Nginx 部署示例**

   创建 Nginx 配置文件（如 `/etc/nginx/sites-available/langhuan-web`）：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com; # 替换为您的域名或IP

       root /path/to/langhuan-web/dist; # 替换为实际的dist目录路径
       index index.html;

       location / {
           try_files $uri $uri/ /index.html;
       }

       # 配置缓存策略
       location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
           expires 1y;
           add_header Cache-Control "public, immutable";
       }
   }
   ```

   启用配置：
   ```bash
   sudo ln -s /etc/nginx/sites-available/langhuan-web /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl reload nginx
   ```

## 项目配置

### 环境变量

项目通过 `.env` 文件配置环境变量，修改 `.env` 文件在项目根目录后重启启动或构建（具体看env文件内）：

```
BASE_URL=http://localhost:9077

# 其他配置项
```

### 自定义端口

如果需要修改开发服务器的端口，可以修改 `config/webpack.dev.js` 文件中的配置：

```javascript
devServer: {
    port: 8080, // 修改为您需要的端口
    // 其他配置...
}
```

## 常见问题

### 1. 安装依赖时出现权限错误

```bash
# 使用 --ignore-scripts 参数跳过脚本执行
npm install --ignore-scripts

# 或者更改 npm 默认目录权限
sudo chown -R $(whoami) ~/.npm
```

### 2. 构建失败：内存不足

增加 Node.js 内存限制：

```bash
# Linux/Mac
export NODE_OPTIONS="--max-old-space-size=4096"

# Windows
set NODE_OPTIONS=--max-old-space-size=4096

# 然后再次尝试构建
npm run build
```

### 3. 代理配置

如需在开发环境中代理 API 请求，可在 `config/webpack.dev.js` 中配置：

```javascript
devServer: {
    proxy: {
        '/api': {
            target: 'http://your-api-server.com',
            changeOrigin: true,
            pathRewrite: { '^/api': '' }
        }
    }
}
```
