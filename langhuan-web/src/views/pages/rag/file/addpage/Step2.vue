<template>
    <div class="step-container">
        <div v-for="(item, index) in content" :key="index"
        >  {{ item }}  </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits(['next'])

const content = ref<string[]>([])

const setDocument = (value: string[]) => {
    content.value = value
}

defineExpose({ setDocument })
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>