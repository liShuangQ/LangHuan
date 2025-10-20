<template>
    <div class="w-full h-full overflow-hidden">
        <div class="h-full flex items-center bg-white">
            <div class="px-6 flex items-center text-gray-800 font-bold border-r border-gray-200 whitespace-nowrap">
                <div v-if="BASE_PROJECT_LOGO_URL_BIG" class="w-full h-full">
                    <img :src="BASE_PROJECT_LOGO_URL_BIG" alt="logo" class="h-10"></img>
                </div>
                <div v-else>
                    {{ BASE_PROJECT_NAME }}
                </div>
            </div>
            <el-menu class="el-menu-horizontal-demo flex-1" mode="horizontal" :collapse="isCollapse"
                :unique-opened="true" ref="menuRef" @select="menuSelect" :default-active="getNowMenu('path') as string"
                :default-openeds="getNowMenu('faPath') as string[]" background-color="#ffffff" text-color="#333333"
                active-text-color="#0089fa">
                <!--      router-->
                <template v-for="(item) in menuData as any" :key="item.path">
                    <template v-if="!item.children || item.children === 0">
                        <el-menu-item :index="item.path">
                            <el-icon v-if="item.icon && item.icon !== ''">
                                <component :is="item.icon"></component>
                            </el-icon>
                            <div v-else style="width: 24px"></div>
                            <span>{{ item.title }}</span>
                        </el-menu-item>
                    </template>
                    <template v-else>
                        <sub-menu :key="item.path" :SubItem='item'></sub-menu>
                    </template>
                </template>
            </el-menu>
            <!-- <el-icon @click="isCollapse = !isCollapse" -->
            <!--          class="h-[40px] text-[24px] text-gray-600 cursor-pointer flex items-center justify-center w-[40px] flex-shrink-0 hover:text-gray-800"-->
            <!--     <Expand v-show="isCollapse"/>-->
            <!--     <Fold v-show="!isCollapse"/>-->
            <!-- </el-icon>-->
        </div>
    </div>
</template>

<script lang="ts">
import { PagesMenu } from '@/router/menu'
import SubMenu from "./SubMenu.vue";
import router from "@/router";
import menu from "@/store/menu";

export default defineComponent({
    components: {
        "sub-menu": SubMenu,
    },
    setup() {
        const BASE_PROJECT_NAME = computed(() => {
            return process.env.BASE_PROJECT_NAME as string
        })

        const BASE_PROJECT_LOGO_URL_BIG = computed(() => {
            return process.env.BASE_PROJECT_LOGO_URL_BIG as string;
        })


        const isCollapse = ref<boolean>(false)
        let menuRef = ref(null)
        const menuData = ref<PagesMenu[] | null>(null)
        const menuStore = menu()
        const flatMenu = menuStore.flatMenu
        menuData.value = menuStore.getMenu() as PagesMenu[]
        let getNowMenu = (type: 'faPath' | 'path'): string[] | string => {
            const menu: PagesMenu = flatMenu.find(e => e.path === router.currentRoute.value.path) as PagesMenu
            if (type === 'path') {
                menuStore.setHistoryAdd(menu)
            }
            try {
                return menu[type]
            } catch (err) {
                console.warn(err)
                return type === 'faPath' ? ("" as string).split(',') : "" as string
            }
        }
        const menuSelect = (index: string, indexPath: string[], item: any, routeResult: any) => {
            router.push({ path: index });
        }



        return {
            BASE_PROJECT_NAME, BASE_PROJECT_LOGO_URL_BIG, menuData, isCollapse, menuRef, menuSelect, getNowMenu
        }
    }
})

</script>

<style>
.el-menu-horizontal-demo {
    border-bottom: none;
}

.el-menu-horizontal-demo:not(.el-menu--collapse) {
    min-height: 40px;
    display: flex;
    align-items: center;
}

/* 横版菜单样式调整 */
.el-menu--horizontal .el-menu-item {
    height: 40px;
    line-height: 40px;
    border-bottom: 2px solid transparent;
    color: #333333;
}

.el-menu--horizontal .el-menu-item.is-active {
    border-bottom-color: #0089fa;
    color: #0089fa;
    font-weight: 500;
}

.el-menu--horizontal .el-menu-item:hover {
    background-color: #f5f5f5;
    color: #0089fa;
}

.el-menu--horizontal .el-sub-menu__title {
    height: 40px;
    line-height: 40px;
    border-bottom: 2px solid transparent;
    color: #333333;
}

.el-menu--horizontal .el-sub-menu.is-active>.el-sub-menu__title {
    border-bottom-color: #0089fa;
    color: #0089fa;
    font-weight: 500;
}

.el-menu--horizontal .el-sub-menu__title:hover {
    background-color: #f5f5f5;
    color: #0089fa;
}

/* 下拉菜单样式 */
.el-menu--horizontal .el-menu {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-menu--horizontal .el-menu .el-menu-item {
    height: 40px;
    line-height: 40px;
    background-color: #ffffff;
    color: #333333;
}

.el-menu--horizontal .el-menu .el-menu-item:hover {
    background-color: #f5f5f5;
    color: #0089fa;
}

.el-menu--horizontal .el-menu .el-menu-item.is-active {
    background-color: #f0f9ff;
    color: #0089fa;
    font-weight: 500;
}
</style>
