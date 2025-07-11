---
mode: "edit"
---

## 项目技术栈

1. 前端框架：Vue 3.x (Composition API + TypeScript)
2. UI 组件库：ElementPlus
3. CSS 框架：TailwindCSS v3.x
4. 样式处理：Sass (SCSS)
5. 代码校验：ESLint + Prettier
6. 类型检查：TypeScript

## Vue 3 开发规范

推荐使用 Composition API，优先使用 `<script setup>` 语法糖：

```vue
<template>
    <div>{{ message }}</div>
</template>

<script setup lang="ts">
import { ref } from "vue";

const message = ref("Hello Vue 3!");
</script>
```

注意根据不同的功能块或样式块划分不同的组件

## 组件命名规范

全局组件：大驼峰命名，如 BaseButton.vue
局部组件：小驼峰命名，如 userCard.vue

## 请求封装样例

引入 `import { http } from "@/plugins/axios";` 后

```ts
http.request<any>({
    url: "aa/aa", //接口地址
    method: "post", //请求方法 post|get
    q_spinning: true, //是否开启蒙层，默认开启
    /**
     * q_contentType 影响请求头中的 Content-Type
     * form: application/x-www-form-urlencoded
     * json: application/json
     * 枚举为 "form" | "json"
     */
    q_contentType: "json",
    data: {
        // 传递的数据...
    },
}).then((res) => {
    // 操作逻辑...
});
```
