<template>
  <div class="step-container">
    <el-form :model="form" label-width="120px">
      <el-form-item label="文件组">
        <el-select v-model="form.group" placeholder="请选择文件组">
          <el-option label="组A" value="groupA" />
          <el-option label="组B" value="groupB" />
        </el-select>
      </el-form-item>

      <el-form-item label="附加信息">
        <el-input
          v-model="form.additionalInfo"
          type="textarea"
          :rows="3"
          placeholder="请输入附加信息"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

const form = reactive({
  group: '',
  additionalInfo: ''
})
</script>

<style scoped>
.step-container {
  margin-top: 20px;
}
</style>