<template>
    <div>
        <!-- Page Container -->
        <div class="flex items-center justify-center min-h-screen bg-white py-48">
            <div class="flex flex-col">
                <!-- Error Container -->
                <div class="flex flex-col items-center">
                    <div class="text-indigo-500 font-bold text-7xl">500</div>

                    <div
                        class="font-bold text-3xl xl:text-7xl lg:text-6xl md:text-5xl mt-10"
                    >
                        This page an error occurred
                    </div>

                    <div
                        class="text-gray-400 font-medium text-sm md:text-xl lg:text-2xl mt-8"
                    >
                        Please contact the administrator.
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts">
export default {
    auto: true,
    route: {meta: {title: "500页面"}},
};
</script>
<script setup lang="ts"></script>

<style scoped></style>
