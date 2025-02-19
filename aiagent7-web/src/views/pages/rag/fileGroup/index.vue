<template>
    <div style="width: 100%;overflow: hidden;box-sizing: border-box;padding: 16px;">
        <el-button style="margin-bottom: 8px;" type="primary" @click="handleAdd">新增</el-button>

        <el-form :model="queryForm" ref="queryFormRef" label-width="100px">
            <el-row :gutter="20">
                <el-col :span="8">
                    <el-form-item label="文件组名称" prop="groupName">
                        <el-input v-model="queryForm.groupName" placeholder="请输入文件组名称"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="文件组类型" prop="groupType">
                        <el-input v-model="queryForm.groupType" placeholder="请输入文件组类型"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item>
                        <el-button type="primary" @click="onQuery">查询</el-button>
                        <el-button type="default" @click="resetQueryForm">重置</el-button>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <el-table border :data="tableData" style="width: 100%;height: 70vh;">
            <el-table-column prop="id" label="ID"></el-table-column>
            <el-table-column prop="groupName" label="文件组名称"></el-table-column>
            <el-table-column prop="groupType" label="文件组类型"></el-table-column>
            <el-table-column prop="groupDesc" label="文件组描述"></el-table-column>
            <el-table-column prop="createdBy" label="创建用户"></el-table-column>
            <el-table-column prop="createdAt" label="创建时间"></el-table-column>
            <el-table-column label="操作">
                <template #default="scope">
                    <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                    <el-button type="text" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination style="float: right;margin-top: 16px;" background layout="total, prev, pager, next" :total="total"
            :page-size="pageSize" :current-page="currentPage" @current-change="handlePageChange"></el-pagination>

        <el-dialog v-model="dialogVisible" title="文件组信息">
            <el-form :model="form" ref="formRef" label-width="120px">
                <el-form-item label="文件组名称" model="groupName">
                    <el-input v-model="form.groupName" placeholder="请输入文件组名称"></el-input>
                </el-form-item>
                <el-form-item label="文件组类型" model="groupType">
                    <el-input v-model="form.groupType" placeholder="请输入文件组类型"></el-input>
                </el-form-item>
                <el-form-item label="文件组描述" model="groupDesc">
                    <el-input v-model="form.groupDesc" placeholder="请输入文件组描述"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitForm">确定</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>
<script lang="ts">
export default {
    auto: true,
};
</script>
<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { http } from '@/plugins/axios';

const queryForm = reactive({
    groupName: '',
    groupType: '',
});

const tableData = ref([]);
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

const form = reactive({
    id: null,
    groupName: '',
    groupType: '',
    groupDesc: '',
});

const dialogVisible = ref(false);
const isAdd = ref(true);

const queryFormRef = ref();
const formRef = ref();

const onQuery = () => {
    fetchData();
};

const resetQueryForm = () => {
    queryFormRef.value.resetFields();
};

const fetchData = () => {
    http.request<any>({
        url: '/rag/file-group/query',
        method: 'post',
        q_spinning: true,
        q_contentType: 'form',
        data: {
            groupName: queryForm.groupName,
            groupType: queryForm.groupType,
            pageNum: currentPage.value,
            pageSize: pageSize.value,
        },
    }).then((res) => {
        tableData.value = res.data.records;
        total.value = res.data.total;
    });
};

const handleAdd = () => {
    isAdd.value = true;
    form.id = null;
    form.groupName = '';
    form.groupType = '';
    form.groupDesc = '';
    dialogVisible.value = true;
};

const handleEdit = (row: any) => {
    isAdd.value = false;
    form.id = row.id;
    form.groupName = row.groupName;
    form.groupType = row.groupType;
    form.groupDesc = row.groupDesc;
    dialogVisible.value = true;
};

const handleDelete = (id: number) => {
    ElMessageBox.confirm('此操作将永久删除该文件组, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
    }).then(() => {
        http.request<any>({
            url: '/rag/file-group/delete',
            method: 'post',
            q_spinning: true,
            q_contentType: 'form',
            data: { id },
        }).then((res) => {
            ElMessage.success('删除成功');
            fetchData();
        });
    });
};

const submitForm = () => {
    formRef.value.validate((valid: any) => {
        if (valid) {
            const url = isAdd.value ? '/rag/file-group/add' : '/rag/file-group/update';
            http.request<any>({
                url,
                method: 'post',
                q_spinning: true,
                q_contentType: 'json',
                data: form,
            }).then((res) => {
                ElMessage.success(isAdd.value ? '添加成功' : '更新成功');
                dialogVisible.value = false;
                fetchData();
            });
        }
    });
};

const handlePageChange = (page: number) => {
    currentPage.value = page;
    fetchData();
};

onMounted(() => {
    fetchData();
});
</script>

<style scoped>
.box-card {
    width: 100%;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>
