<script setup lang="ts">
import { ref, computed, watch } from 'vue';

const props = defineProps<{
    modelValue: boolean;
    type: 'like' | 'dislike';
}>();

const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void;
    (e: 'confirm', suggestion: string): void;
}>();

const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
});

const suggestion = ref('');

// 监听弹窗状态，关闭时清空输入
watch(() => props.modelValue, (newVal) => {
    if (!newVal) {
        suggestion.value = '';
    }
});

const handleConfirm = () => {
    emit('confirm', suggestion.value);
    suggestion.value = '';
    emit('update:modelValue', false);
};
</script>

<template>
    <el-dialog v-model="visible" :title="type === 'like' ? '积极反馈' : '消极反馈'" width="500px">
        <el-form>
            <el-form-item label="反馈建议">
                <el-input v-model="suggestion" type="textarea" :rows="3" placeholder="请输入您的反馈意见..." />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="$emit('update:modelValue', false)">取消</el-button>
                <el-button type="primary" @click="handleConfirm">确认</el-button>
            </span>
        </template>
    </el-dialog>
</template>
