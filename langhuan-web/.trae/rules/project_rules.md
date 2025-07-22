您精通 TypeScript、Node.js、Vue.js、Vue Router、Pinia、VueUse、Element Plus、Echarts、Sass (SCSS)和 Tailwind，对这些技术的最佳实践和性能优化技巧有着深刻的理解。
# 代码风格与结构
- 编写简洁、可维护且技术准确的 TypeScript 代码。
- 使用函数式和声明式编程模式；避免使用类。
- 优先采用迭代和模块化，遵循 DRY 原则，避免代码重复。
- 使用描述性的变量名，加上辅助动词（例如，isLoading、hasError）。
- 系统地组织文件：每个文件应只包含相关的内容，例如导出的组件、子组件、辅助函数、静态内容和类型。
- 使用 4 个空格作为缩进。
# 命名约定
- 目录使用小写并用短横线分隔（例如，components/auth-wizard）。
- 函数优先使用具名导出。
# TypeScript 使用
- 所有代码均使用 TypeScript；优先使用接口而非类型，因其具有可扩展性和合并能力。
- 避免使用枚举；使用映射代替，以获得更好的类型安全性和灵活性。
- 使用带有 TypeScript 接口的函数组件。
# 语法和格式
- 对于纯函数，请使用“function”关键字，以利用提升机制并提高清晰度。
- 始终使用 Vue 组合式 API 的 script setup 风格。
# 用户界面和样式
- 使用 Element Plus、Sass (SCSS) 和 Tailwind 来构建组件和进行样式设计。
- 使用 Tailwind CSS 实现响应式设计；采用PC端优先的方法。
- 使用 Echarts 构建可视化图表信息。
# 性能优化
- 在适用的情况下利用 VueUse 函数来增强响应性和性能。
- 对非关键组件使用动态加载。
# 请求接口封装样例
- 引入 `import { http } from "@/plugins/axios";` 后
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
       // 操作逻辑...
    })
```