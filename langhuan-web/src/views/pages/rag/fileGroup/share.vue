<template>
    <div>
        <el-form
            ref="shareFormRef"
            :model="shareForm"
            :rules="shareFormRules"
            label-width="120px"
            class="share-form"
        >
            <el-form-item label="共享用户" prop="sharedUsers">
                <!-- multiple -->
                <el-select
                    v-model="shareForm.sharedUsers"
                    placeholder="请选择共享用户"
                    style="width: 100%"
                    clearable
                    filterable
                    remote
                    @change="userChangeMethod"
                    :remote-method="userRemoteMethod"
                >
                    <el-option
                        v-for="user in userOptions"
                        :key="user.value"
                        :label="user.label"
                        :value="user.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="权限配置">
                <div class="permission-grid">
                    <div class="permission-item">
                        <div class="permission-label">读取权限</div>
                        <el-switch
                            v-model="shareForm.canRead"
                            active-text="开启"
                            inactive-text="关闭"
                        />
                    </div>
                    <div class="permission-item">
                        <div class="permission-label">新增权限</div>
                        <el-switch
                            v-model="shareForm.canAdd"
                            active-text="开启"
                            inactive-text="关闭"
                        />
                    </div>
                    <div class="permission-item">
                        <div class="permission-label">更新权限</div>
                        <el-switch
                            v-model="shareForm.canUpdate"
                            active-text="开启"
                            inactive-text="关闭"
                        />
                    </div>
                    <div class="permission-item">
                        <div class="permission-label">删除权限</div>
                        <el-switch
                            v-model="shareForm.canDelete"
                            active-text="开启"
                            inactive-text="关闭"
                        />
                    </div>
                </div>
            </el-form-item>
        </el-form>

        <div class="form-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleConfirm">确定</el-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, FormInstance, FormRules } from "element-plus";
import { http } from "@/plugins/axios";
interface ShareForm {
    // sharedUsers: string[];
    sharedUsers: string;
    canRead: boolean;
    canAdd: boolean;
    canUpdate: boolean;
    canDelete: boolean;
}

interface UserOption {
    label: string;
    value: string;
}

interface Props {
    fileGroupId?: string | number;
    visible?: boolean;
    userStoreInfo?: any;
}

const props = withDefaults(defineProps<Props>(), {
    fileGroupId: "",
    visible: false,
    userStoreInfo: null,
});

const emit = defineEmits<{
    confirm: [data: ShareForm];
    cancel: [];
}>();

const shareFormRef = ref<FormInstance>();
const userOptions = ref<UserOption[]>([]);

const shareForm = reactive<ShareForm>({
    // sharedUsers: [],
    sharedUsers: "",
    canRead: true,
    canAdd: false,
    canUpdate: false,
    canDelete: false,
});

const shareFormRules: FormRules = {
    sharedUsers: [
        {
            required: true,
            message: "请选择至少一个共享用户",
            trigger: "change",
        },
    ],
};

const userRemoteMethod = (query: string) => {
    clearTimeout((window as any).remoteSearchTimer);
    (window as any).remoteSearchTimer = setTimeout(() => {
        http.request<any>({
            url: "/user/getUserPageList",
            method: "post",
            data: {
                name: query,
                pageNum: 1,
                pageSize: 20,
            },
        })
            .then((res) => {
                userOptions.value = res.data.records
                    .filter(
                        (user: any) =>
                            user.username !== props.userStoreInfo.user.username
                    )
                    .map((user: any) => ({
                        label: user.name,
                        value: user.username,
                    }));
            })
            .catch((err) => {
                console.log(err);
            });
    }, 500);
};

// 重置表单
const resetForm = (data: ShareForm | null) => {
    shareForm.sharedUsers = "";
    shareForm.canRead = false;
    shareForm.canAdd = false;
    shareForm.canUpdate = false;
    shareForm.canDelete = false;
};

// 设置表单数据
const setFormData = (data: Partial<ShareForm>) => {
    Object.assign(shareForm, data);
};

// 获取表单数据
const getFormData = (): ShareForm => {
    return { ...shareForm };
};
// 共享用户改变,获取用户当前的权限
const userChangeMethod = (value: string) => {
    http.request<any>({
        url: "/rag/file-group/shareList",
        method: "post",
        q_spinning: true,
        data: {
            fileGroupId: props.fileGroupId,
            sharedWith: value,
        },
    }).then((res) => {
        if (res.data.length > 0) {
            shareForm.canRead = res.data[0].canRead;
            shareForm.canAdd = res.data[0].canAdd;
            shareForm.canUpdate = res.data[0].canUpdate;
            shareForm.canDelete = res.data[0].canDelete;
        }else{
            shareForm.canRead = false;
            shareForm.canAdd = false;
            shareForm.canUpdate = false;
            shareForm.canDelete = false;
        }
    });
};

// 确定按钮处理
const handleConfirm = async () => {
    try {
        await shareFormRef.value?.validate();
        emit("confirm", getFormData());
    } catch (error) {
        console.error("表单验证失败:", error);
    }
};

// 取消按钮处理
const handleCancel = () => {
    emit("cancel");
};

// 暴露方法给父组件
defineExpose({
    resetForm,
    setFormData,
    getFormData,
});

onMounted(() => {
    nextTick(() => {});
});
</script>

<style scoped lang="scss">
.share-form {
    .permission-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;

        .permission-item {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            justify-content: space-between;
            padding: 12px;
            border: 1px solid #e4e7ed;
            border-radius: 6px;
            background-color: #fafafa;

            .permission-label {
                font-size: 14px;
                color: #606266;
                font-weight: 500;
            }
        }
    }
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;
}
</style>
