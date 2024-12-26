# aiagent7-web

## 主要使用技术
- WebPack5.88.2 + TypeScript + Vue3
- TailWindCss + Sass + element-plus
- Axios + Pinia + vue-router@4

## 开发准备
- Node >= 16
- VsCode or WebStorm...
  - VsCode插件(Volar Eslint)
  - idea系列注意打开对应插件
- 包管理推荐 Pnpm

## 项目命令
- cd xxx
- pnpm install
- pnpm run serve (开发)
- pnpm run build（生产）
- pnpm run lint （eslint格式化）

## env说明
- TOKEN_KEY： 存储token的key
- AFTER_MENU：是否开启从用户获取获取菜单信息 'false' | 'true' （非 'true'，均从本地文件获取）
