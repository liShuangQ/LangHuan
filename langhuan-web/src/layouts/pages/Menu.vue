<template>
    <div class="w-full h-full overflow-hidden">
        <div class="w-[calc(100%-1px)] h-[40px] text-center leading-10 bg-[#001f37] text-white font-bold">
            {{ BASE_PROJECT_NAME }}
        </div>
        <el-menu
            style="height:calc(100% - 40px);"
            class="el-menu-vertical-demo"
            :collapse="isCollapse"
            :unique-opened="true"
            ref="menuRef"
            @select="menuSelect"
            :default-active="getNowMenu('path')"
            :default-openeds="getNowMenu('faPath')"
            background-color="#001624"
            text-color="#fff"
            active-text-color="#0089fa"
        >
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
            <el-icon @click="isCollapse = !isCollapse"
                     style="height: 40px;font-size: 24px;text-align: center;width: 100%;color: #fff;cursor: pointer"
            >
                <Expand v-show="isCollapse"/>
                <Fold v-show="!isCollapse"/>
            </el-icon>
        </el-menu>
    </div>
</template>

<script lang="ts">
import {PagesMenu} from '@/router/menu'
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

        const menuSelect = (index: string, indexPath: string[], item: object, routeResult: object) => {
            // console.log(index,indexPath,item,routeResult)
            router.push({path: index});
        }

        return {
            BASE_PROJECT_NAME, menuData, isCollapse, menuRef, menuSelect, getNowMenu
        }
    }
})

</script>

<style>
.el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 200px;
    min-height: 400px;
}
</style>