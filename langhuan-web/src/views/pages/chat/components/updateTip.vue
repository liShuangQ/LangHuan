<template>
    <el-dialog v-model="dialogVisible" title="更新内容" width="500px" center>
        <div class="update-tip-content">
            <div class="tip-icon">
                <el-icon :size="48" color="#409EFF">
                    <InfoFilled />
                </el-icon>
            </div>
            <div class="tip-text">
                <p
                    class="tip-description"
                    v-html="
                        upDateTip ||
                        '本次更新带来了全新的功能和体验优化，快来体验吧！'
                    "
                ></p>
            </div>
        </div>

        <template #footer>
            <div class="dialog-footer">
                <el-button type="primary" @click="handleConfirm"
                    >知道了</el-button
                >
            </div>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { InfoFilled } from "@element-plus/icons-vue";
import { upDateTip } from "../config";

interface Props {
    cacheKey?: string;
}

const props = withDefaults(defineProps<Props>(), {
    cacheKey: "update_tip_shown",
});

const dialogVisible = ref(false);

// 检查缓存，决定是否显示弹窗
function checkShouldShow(): boolean {
    const cached = localStorage.getItem(props.cacheKey);
    return !cached;
}

// 设置缓存
function setCacheFlag(): void {
    localStorage.setItem(props.cacheKey, "true");
}

// 处理确认
function handleConfirm(): void {
    setCacheFlag();
    dialogVisible.value = false;
}

// 组件挂载时检查是否需要显示
onMounted(() => {
    if (checkShouldShow()) {
        dialogVisible.value = true;
    }
});
</script>

<style lang="scss" scoped>
.update-tip-content {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 20px 0;

    .tip-icon {
        flex-shrink: 0;
    }

    .tip-text {
        flex: 1;

        .tip-title {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin: 0 0 8px 0;
        }

        .tip-description {
            font-size: 14px;
            color: #606266;
            line-height: 1.5;
            margin: 0;
        }
    }
}

.dialog-footer {
    display: flex;
    justify-content: center;
    align-items: center;
}

:deep(.el-dialog__header) {
    padding: 20px 20px 0 20px;
}

:deep(.el-dialog__body) {
    padding: 10px 20px;
}

:deep(.el-dialog__footer) {
    padding: 10px 20px 20px 20px;
}
</style>
