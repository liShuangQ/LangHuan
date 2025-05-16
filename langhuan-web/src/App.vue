<template>
    <div>
        <!--        <el-config-provider :locale="'zhLocale'">-->
        <router-view v-slot="{ Component }">
            <keep-alive>
                <component :is="Component" />
            </keep-alive>
        </router-view>
        <!--        </el-config-provider>-->
    </div>
</template>
<script setup lang="ts">
</script>
<style lang="scss">
@import url("./style/app.scss");
</style>
