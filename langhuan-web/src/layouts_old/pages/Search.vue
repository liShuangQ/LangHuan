<template>
    <div>
        <el-select class="ml-1 mb-0.5" style="width: 200px" size="small" v-model="value" filterable placeholder="请输入"
                   @change="change">
            <el-option
                v-for="item in options"
                :key="item.path"
                :label="item.title"
                :value="item.path"
            />
        </el-select>
    </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import menu from "@/store/menu";
import {PagesMenu} from "@/router/menu";
import router from "@/router";

const menuStore = menu()
const value = ref('')
// HACK: 通过特征/pages/筛选出真实的路由
const options: PagesMenu[] | null = toRaw(menuStore.flatMenu).filter(e => e.path.indexOf('/pages/') !== -1)
const change = (path: string): void => {
    router.push({path: path})
}

</script>
