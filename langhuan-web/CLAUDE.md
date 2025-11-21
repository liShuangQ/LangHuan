# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在此代码仓库中工作时提供指导。

## 技术栈精通要求

您精通 TypeScript、Node.js、Vue.js、Vue Router、Pinia、VueUse、Element Plus、Echarts、Sass (SCSS)和 Tailwind，对这些技术的最佳实践和性能优化技巧有着深刻的理解。

**重要提醒：** 当完成任务时，无需启动服务器，我会自己来检查是否运行正常。

## 项目概述

**琅嬛福地 (LangHuan)** - "藏天下典籍" - 一个现代化的 Vue 3 知识管理应用程序，具备 AI 驱动的聊天功能。这是与 Spring Boot 后端集成的全栈应用程序的前端。

## 核心架构

### 技术栈
- **Vue 3.3.4** 组合式 API (`<script setup>`)
- **TypeScript 4.9.4** 严格模式
- **webpack 5**（非 Vite - 这在现代 Vue 项目中不常见）
- **Element Plus 2.9.11** UI 组件库
- **Pinia 2.0.36** 状态管理
- **Vue Router 4** 哈希路由
- **Tailwind CSS 3.3.3** + SCSS 样式
- **Axios 1.7.4** HTTP 请求

### 构建系统
- **自定义 webpack 5 配置**（非 Vite）
- 开发服务器运行在 **端口 9088**
- 生产构建输出到 `dist/langhuan/`
- 配置了 Vue、Vue Router 和 Element Plus 的自动导入
- 组件自动注册和 TypeScript 定义生成

### 关键目录结构
```
src/
├── components/
│   └── globalComponents/     # 增强版 Element Plus 组件
│       ├── ElementTableC/    # 自定义表格组件
│       └── ElementFormC/     # 自定义表单组件
├── layouts/                  # 布局组件（auth, pages, error）
├── views/                    # 页面组件（自动加载为路由）
├── store/                    # Pinia 状态存储（user, menu, aimodel）
├── api/                      # API 服务层
├── router/                   # Vue Router 配置
├── utils/                    # 工具函数
├── types/                    # TypeScript 定义
└── style/                    # 全局样式和 Element Plus 覆盖
```

## 重要模式与规范

### 代码风格与结构
- 编写简洁、可维护且技术准确的 TypeScript 代码
- 使用函数式和声明式编程模式；避免使用类
- 优先采用迭代和模块化，遵循 DRY 原则，避免代码重复
- 使用描述性的变量名，加上辅助动词（例如，isLoading、hasError）
- 系统地组织文件：每个文件应只包含相关的内容，例如导出的组件、子组件、辅助函数、静态内容和类型
- 使用 4 个空格作为缩进

### 命名约定
- 目录使用小写并用短横线分隔（例如，components/auth-wizard）
- 函数优先使用具名导出

### TypeScript 使用规范
- 所有代码均使用 TypeScript；优先使用接口而非类型，因其具有可扩展性和合并能力
- 避免使用枚举；使用映射代替，以获得更好的类型安全性和灵活性
- 使用带有 TypeScript 接口的函数组件

### 语法和格式规范
- 对于纯函数，请使用"function"关键字，以利用提升机制并提高清晰度
- 始终使用 Vue 组合式 API 的 script setup 风格
- 非最佳实践情况下，尽量减少使用 watch 或者 computed 属性
- 绘制页面时，在非特殊说明情况下，不要添加圆角、边框、背景色、阴影

### 组件架构
- **全局组件**: Element Plus 组件的自定义增强版本
  - `ElementTableC`: 带分页、选择和自定义功能的表格
  - `ElementFormC`: 带增强验证和布局的表单
- **基于布局的路由**: 页面从 `/views/` 目录自动加载
- **组合式 API**: 所有组件使用 `<script setup>` 语法

### 状态管理
- **Pinia 存储库** 在 `/store/` 目录中：
  - `user.ts`: 认证和用户配置文件
  - `menu.ts`: 导航菜单状态
  - `aimodel.ts`: AI 模型配置
- **基于令牌的身份验证** 自动头部注入
- **基于角色的访问控制**（管理员/用户区别）

### 路由配置
- **基于哈希的路由** (`createWebHashHistory`)
- **自动生成路由** 从 `/views/` 目录
- **路由守卫** 用于身份验证和授权
- **布局系统** 嵌套路由结构

### 用户界面和样式
- 使用 Element Plus、Sass (SCSS) 和 Tailwind 来构建组件和进行样式设计
- 使用 Tailwind CSS 实现响应式设计；采用 PC 端优先的方法
- 使用 Echarts 构建可视化图表信息
- 非明确指定不要修改 Element Plus 的默认样式

### 性能优化
- 在适用的情况下利用 VueUse 函数来增强响应性和性能
- 对非关键组件使用动态加载

### HTTP 请求封装示例
引入 `import { http } from "@/plugins/axios";` 后：
```ts
http.request<any>({
    url: 'aa/aa', //接口地址
    method: 'post', //请求方法 post|get
    q_spinning: true, //是否开启蒙层，默认开启
    /**
     * q_contentType 影响请求头中的 Content-Type
     * form: application/x-www-form-urlencoded
     * json: application/json
     * 枚举为 "form" | "json"
     */
    q_contentType: 'form', //默认使用form
    data: {
        // 传递的数据...
    }
}).then((res) => {
    /**
     * res.code 200为响应成功
     * res.message 响应信息
     * res.data 响应数据
     */
    // 操作逻辑...
})
```

## 优先使用的组件(如果提示了明确使用组件编写)

### 表单组件 (ElementFormC)
- **使用方式参考**: `@src/views/pages/componentDemo/form`
- **配置信息参考**: `@src/components/globalComponents/ElementFormC/form-component.d.ts`
- **最佳实践**: 将配置信息放在一个和使用页面同目录的单独文件中，方便管理
- **查询项目的类型 FormItemConfigs 中的 col 配置默认为 5**
- **查询重置按钮等信息配置方式**（重点 `<template>` 标签）：
```vue
<ElementFormC ref="formComRef" :formConfig="formConfig" :formItemConfig="formItemConfig"
    @handle="formHandle">
    <template #handle-operate>
        <div class="float-right">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="default" :icon="RefreshLeft" @click="handleReset"></el-button>
        </div>
    </template>
</ElementFormC>
```

### 表格组件 (ElementTableC)
- **使用方式参考**: `@src/views/pages/componentDemo/table`
- **配置信息参考**: `@src/components/globalComponents/ElementTableC/table-component.d.ts`
- **最佳实践**: 将配置信息放在一个和使用页面同目录的单独文件中，方便管理
- **宽度设置**: 非特意说明指定宽度时，默认不设置表格每一项的宽度（类型 TableColumnConfig 中的 width 默认不设置）

## 配置说明

### 路径别名
- `@/` → `src/` 目录（在 webpack 和 TypeScript 中配置）

### 自动导入
- Vue: `ref`, `reactive`, `onMounted` 等
- Vue Router: `useRouter`, `useRoute`
- Element Plus: 自动导入的组件和工具

## 开发工作流程

1. **启动开发环境**: `npm run dev`（端口 9088）
2. **代码格式化**: `npm run lint`（ESLint 自动修复）
3. **构建分析**: `npm run analyze`（打包分析器）
4. **生产构建**: `npm run build`

## 关键架构决策

- **webpack 5 而非 Vite**: 可能是为了特定的构建优化需求
- **哈希路由**: 为了在不同服务器配置下的部署灵活性
- **自定义表格/表单组件**: Element Plus 的增强版本，具备额外功能
- **基于布局的路由加载**: 从文件结构自动生成路由
- **强 TypeScript 集成**: 为组件和导入自动生成类型

## 代码库工作指南

- **组件开发**: 遵循 `/components/globalComponents/` 中的现有模式
- **新页面**: 添加到 `/views/` 目录 - 路由会自动生成
- **API 集成**: 使用现有的 Axios 配置，参考“HTTP 请求封装示例”
- **状态管理**: 在 `/store/` 中创建新的存储，遵循 Pinia 模式
- **样式设计**: 使用 Tailwind 工具类结合 SCSS 进行组件特定样式
- **TypeScript**: 保持严格类型检查

## Task Master AI Instructions
**Import Task Master's development workflow commands and guidelines, treat as if import is in the main CLAUDE.md file.**
@./.taskmaster/CLAUDE.md
