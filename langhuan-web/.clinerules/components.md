# 优先考虑使用的组件

## 表单组件
- 使用方式参考：@src/views/pages/componentDemo/form
- 配置信息参考：@src/components/globalComponents/ElementFormC/form-component.d.ts
- 注意最佳实践是将配置信息放在一个和使用页面同目录的单独的文件中，方便管理
- 查询项目的类型 FormItemConfigs 中的 col 配置默认为5。
- 查询重置按钮等信息配置方式如下(重点<template>标签)：
```
<ElementFormC ref="formComRef" :formConfig="formConfig":formItemConfig="formItemConfig"
    @handle="formHandle">
    <template #handle-operate>
        <div class="float-right">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="default" :icon="RefreshLeft" @click="handleReset"></el-button>
        </div>
    </template>
</ElementFormC>
```


## 表格组件
- 使用方式参考：@src/views/pages/componentDemo/table
- 配置信息参考：@src/components/globalComponents/ElementTableC/table-component.d.ts
- 注意最佳实践是将配置信息放在一个和使用页面同目录的单独的文件中，方便管理
- 非特意说明指定宽度时，默认不设置表格每一项的宽度（类型 TableColumnConfig 中的 width 默认不设置）