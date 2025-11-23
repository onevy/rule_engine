<template>
  <div class="scene-management">
    <!-- Search and Filter Area -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="场景名称">
          <el-input
            v-model="searchForm.sceneName"
            placeholder="请输入场景名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="场景编码">
          <el-input
            v-model="searchForm.sceneCode"
            placeholder="请输入场景编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table Card -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>场景列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新增场景
            </el-button>
          </div>
        </div>
      </template>

      <!-- Table -->
      <el-table v-loading="loading" :data="sceneList" border stripe style="width: 100%">
        <el-table-column prop="sceneCode" label="场景编码" width="200" show-overflow-tooltip />
        <el-table-column prop="sceneName" label="场景名称" min-width="200" show-overflow-tooltip />
        <el-table-column
          prop="adapterClass"
          label="适配器类"
          min-width="300"
          show-overflow-tooltip
        />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleMetadata(row)">元数据配置</el-button>
            <el-popconfirm title="确定要删除该场景吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="场景详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="场景编码">{{ currentScene.sceneCode }}</el-descriptions-item>
        <el-descriptions-item label="场景名称">{{ currentScene.sceneName }}</el-descriptions-item>
        <el-descriptions-item label="适配器类" :span="2">{{
          currentScene.adapterClass
        }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentScene.status === 1 ? 'success' : 'info'">
            {{ currentScene.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentScene.createTime }}</el-descriptions-item>
        <el-descriptions-item label="场景描述" :span="2">{{
          currentScene.description || '无'
        }}</el-descriptions-item>
        <el-descriptions-item label="输入模式" :span="2">
          <pre>{{ formatJson(currentScene.inputSchema) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="输出模式" :span="2">
          <pre>{{ formatJson(currentScene.outputSchema) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentScene)">编辑</el-button>
      </template>
    </el-dialog>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑场景' : '新增场景'"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="sceneForm" :rules="formRules" label-width="120px">
        <el-form-item label="场景编码" prop="sceneCode">
          <el-input v-model="sceneForm.sceneCode" placeholder="请输入场景编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="场景名称" prop="sceneName">
          <el-input v-model="sceneForm.sceneName" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="适配器类" prop="adapterClass">
          <el-input
            v-model="sceneForm.adapterClass"
            placeholder="请输入适配器类名，如：com.example.ruleengine.adapter.InfectiousDiseaseAdapter"
          />
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input
            v-model="sceneForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入场景描述"
          />
        </el-form-item>
        <el-form-item label="输入模式">
          <el-input
            v-model="sceneForm.inputSchemaStr"
            type="textarea"
            :rows="4"
            placeholder='输入数据结构（JSON格式），如：{"diagnosis": "string", "labResult": "object"}'
          />
        </el-form-item>
        <el-form-item label="输出模式">
          <el-input
            v-model="sceneForm.outputSchemaStr"
            type="textarea"
            :rows="4"
            placeholder='输出数据结构（JSON格式），如：{"needReport": "boolean", "diseaseCode": "string"}'
          />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="sceneForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getScenePage, createScene, updateScene, deleteScene, updateSceneStatus } from '@/api/scene'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const sceneList = ref([])
const viewDialogVisible = ref(false)
const formDialogVisible = ref(false)
const isEdit = ref(false)
const currentScene = ref({})
const formRef = ref()

const searchForm = reactive({
  sceneName: '',
  sceneCode: '',
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const sceneForm = reactive({
  sceneCode: '',
  sceneName: '',
  adapterClass: '',
  description: '',
  inputSchemaStr: '',
  outputSchemaStr: '',
  status: 1
})

const formRules = {
  sceneCode: [
    { required: true, message: '请输入场景编码', trigger: 'blur' },
    {
      pattern: /^[A-Z][A-Z0-9_]*$/,
      message: '编码必须以大写字母开头，只能包含大写字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  sceneName: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  adapterClass: [
    { required: true, message: '请输入适配器类', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9.]*$/, message: '请输入有效的Java类名', trigger: 'blur' }
  ]
}

// Load data
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    // Remove empty values
    Object.keys(params).forEach((key) => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })

    const res = await getScenePage(params)
    if (res.code === 200) {
      sceneList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载场景列表失败')
    console.error(error)
  }
  loading.value = false
}

onMounted(() => {
  loadData()
})

// Search
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// Reset
const handleReset = () => {
  searchForm.sceneName = ''
  searchForm.sceneCode = ''
  searchForm.status = null
  pagination.page = 1
  loadData()
}

// Pagination
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

// View scene
const handleView = (row) => {
  currentScene.value = row
  viewDialogVisible.value = true
}

// Create scene
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  formDialogVisible.value = true
}

// Edit scene
const handleEdit = (row) => {
  isEdit.value = true
  currentScene.value = row
  sceneForm.sceneCode = row.sceneCode
  sceneForm.sceneName = row.sceneName
  sceneForm.adapterClass = row.adapterClass
  sceneForm.description = row.description || ''
  sceneForm.inputSchemaStr = formatJson(row.inputSchema)
  sceneForm.outputSchemaStr = formatJson(row.outputSchema)
  sceneForm.status = row.status
  formDialogVisible.value = true
  viewDialogVisible.value = false
}

// Delete scene
const handleDelete = async (row) => {
  try {
    const res = await deleteScene(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

// Status change
const handleStatusChange = async (row) => {
  try {
    const res = await updateSceneStatus(row.id, row.status)
    if (res.code === 200) {
      ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
    } else {
      // Revert status
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error(res.message || '状态更新失败')
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
    console.error(error)
  }
}

// Go to metadata configuration
const handleMetadata = (row) => {
  router.push(`/scene/metadata?sceneCode=${row.sceneCode}`)
}

// Submit form
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // Parse JSON schema
    let inputSchema = null
    let outputSchema = null

    if (sceneForm.inputSchemaStr) {
      try {
        inputSchema = JSON.parse(sceneForm.inputSchemaStr)
      } catch (e) {
        ElMessage.error('输入模式格式错误，请检查JSON格式')
        return
      }
    }

    if (sceneForm.outputSchemaStr) {
      try {
        outputSchema = JSON.parse(sceneForm.outputSchemaStr)
      } catch (e) {
        ElMessage.error('输出模式格式错误，请检查JSON格式')
        return
      }
    }

    const data = {
      sceneCode: sceneForm.sceneCode,
      sceneName: sceneForm.sceneName,
      adapterClass: sceneForm.adapterClass,
      description: sceneForm.description,
      inputSchema: inputSchema ? JSON.stringify(inputSchema) : null,
      outputSchema: outputSchema ? JSON.stringify(outputSchema) : null,
      status: sceneForm.status
    }

    submitting.value = true
    let res
    if (isEdit.value) {
      res = await updateScene(currentScene.value.id, data)
    } else {
      res = await createScene(data)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
      formDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || (isEdit.value ? '编辑失败' : '创建失败'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Submit error:', error)
    }
  } finally {
    submitting.value = false
  }
}

// Dialog close
const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

// Reset form
const resetForm = () => {
  sceneForm.sceneCode = ''
  sceneForm.sceneName = ''
  sceneForm.adapterClass = ''
  sceneForm.description = ''
  sceneForm.inputSchemaStr = ''
  sceneForm.outputSchemaStr = ''
  sceneForm.status = 1
}

// Format JSON
const formatJson = (obj) => {
  if (!obj) return ''
  try {
    if (typeof obj === 'string') {
      return JSON.stringify(JSON.parse(obj), null, 2)
    }
    return JSON.stringify(obj, null, 2)
  } catch {
    return obj
  }
}
</script>

<style lang="scss" scoped>
.scene-management {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;

    .search-form {
      .el-form-item {
        margin-bottom: 0;
      }
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
    max-height: 300px;
    overflow-y: auto;
    background-color: #f5f7fa;
    padding: 10px;
    border-radius: 4px;
  }
}
</style>
