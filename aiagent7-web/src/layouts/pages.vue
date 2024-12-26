<template>
    <div class="common-layout h-screen bg-gray-100">
        <el-container>
            <el-aside :style="{display:fullScreenValue?'none':'block',width:'auto'}"
                      class="h-screen">
                <Menu></Menu>
            </el-aside>
            <el-container>
                <el-header :style="{display:fullScreenValue?'none':'block',height: 'max-content',padding:'0'}">
                    <div class="h-[40px] flex justify-between items-center bg-white mb-2">
                        <Breadcrumb></Breadcrumb>
                        <BarUtil @pageFullScreen="pageFullScreen" @refreshPage="refreshPage"></BarUtil>
                    </div>
                    <div class="flex items-center justify-between ml-1">
                        <div>
                            <HistoryTags></HistoryTags>
                        </div>
                        <div class="flex items-center justify-between mr-4">
                            <SearchMenu></SearchMenu>
                            <MenuUtil></MenuUtil>
                        </div>
                    </div>
                </el-header>
                <el-main style="padding: 8px;overflow: hidden;height: calc(100vh - 75px)">
                    <router-view
                        v-slot="{ Component }"
                        class="h-full w-full bg-white overflow-auto"
                    >
                        <Transition appear enter-active-class="animate__animated animate__fadeIn">
                            <component
                                :is="Component" v-if="refreshPageValue"></component>
                        </Transition>
                    </router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>
<script lang="ts">
export default {
    route: {
        meta: {
            auth: true,
        },
    },
};
</script>
<script lang="ts" setup>
import Menu from './pages/Menu.vue'
import HistoryTags from './pages/HistoryTags.vue'
import SearchMenu from './pages/Search.vue'
import MenuUtil from './pages/MenuUtil.vue'
import Breadcrumb from './pages/Breadcrumb.vue'
import BarUtil from './pages/BarUtil.vue'
import {ElMessage} from "element-plus";

let refreshPageValue = ref<boolean>(true);
const refreshPage = () => {
    refreshPageValue.value = false;
    nextTick(() => {
        refreshPageValue.value = true;
    });
    return;
};

let fullScreenValue = ref<boolean>(false)
const setFullFun = (e: any) => {
    if (e.keyCode === 27) {
        fullScreenValue.value = false;
        document.removeEventListener("keyup", setFullFun);
    }
};
const pageFullScreen = () => {
    fullScreenValue.value = true;
    ElMessage('按下ESC键退出')
    document.addEventListener("keyup", setFullFun);
};

</script>
<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 200px;
    min-height: 400px;
}

</style>
