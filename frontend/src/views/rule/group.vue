<template>
  <div class="rule-group">
    <!-- Search Area -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="所属场景">
          <el-select
            v-model="searchForm.sceneCode"
            placeholder="请选择场景"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="scene in scenes"
              :key="scene.sceneCode"
              :label="scene.sceneName"
              :value="scene.sceneCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规则组名称">
          <el-input
            v-model="searchForm.groupName"
            placeholder="请输入规则组名称"
            clearable
            style="width: 200px"
          />
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
          <span>规则组列表</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增规则组
          </el-button>
        </div>
      </template>
      <el-table :data="groups" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="groupCode" label="规则组编码" width="180" show-overflow-tooltip />
        <el-table-column
          prop="groupName"
          label="规则组名称"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="sceneCode" label="所属场景" width="150" />
        <el-table-column prop="executionMode" label="执行模式" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.executionMode === 'ALL' ? 'success' : 'warning'">
              {{ row.executionMode === 'ALL' ? '全部执行' : '首次命中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" align="center" />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定要删除该规则组吗？" @confirm="handleDelete(row)">
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
    <el-dialog v-model="viewDialogVisible" title="规则组详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="规则组编码">{{ currentGroup.groupCode }}</el-descriptions-item>
        <el-descriptions-item label="规则组名称">{{ currentGroup.groupName }}</el-descriptions-item>
        <el-descriptions-item label="所属场景">{{ currentGroup.sceneCode }}</el-descriptions-item>
        <el-descriptions-item label="执行模式">
          <el-tag :type="currentGroup.executionMode === 'ALL' ? 'success' : 'warning'">
            {{ currentGroup.executionMode === 'ALL' ? '全部执行' : '首次命中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">{{ currentGroup.priority }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentGroup.status === 1 ? 'success' : 'info'">
            {{ currentGroup.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{
          currentGroup.createTime
        }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{
          currentGroup.description || '无'
        }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentGroup)">编辑</el-button>
      </template>
    </el-dialog>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑规则组' : '新增规则组'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="groupForm" :rules="formRules" label-width="120px">
        <el-form-item label="所属场景" prop="sceneCode">
          <el-select
            v-model="groupForm.sceneCode"
            placeholder="请选择场景"
            style="width: 100%"
            :disabled="isEdit"
          >
            <el-option
              v-for="scene in scenes"
              :key="scene.sceneCode"
              :label="`${scene.sceneName} (${scene.sceneCode})`"
              :value="scene.sceneCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规则组编码" prop="groupCode">
          <el-input
            v-model="groupForm.groupCode"
            placeholder="请输入规则组编码，如：REPORT_GROUP_01"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="规则组名称" prop="groupName">
          <el-input v-model="groupForm.groupName" placeholder="请输入规则组名称" />
        </el-form-item>
        <el-form-item label="执行模式" prop="executionMode">
          <el-radio-group v-model="groupForm.executionMode">
            <el-radio value="ALL">全部执行</el-radio>
            <el-radio value="FIRST">首次命中</el-radio>
          </el-radio-group>
          <div class="form-tip">
            <div>• 全部执行：执行组内所有匹配的规则</div>
            <div>• 首次命中：执行第一个匹配的规则后停止</div>
          </div>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="groupForm.priority" :min="1" :max="1000" style="width: 100%" />
          <div class="form-tip">数值越大，优先级越高</div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="groupForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入规则组描述"
          />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="groupForm.status" :active-value="1" :inactive-value="0" />
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
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getGroupPage, createGroup, updateGroup, deleteGroup, updateGroupStatus } from '@/api/group'
import { getSceneList } from '@/api/scene'

const loading = ref(false)
const submitting = ref(false)
const groups = ref([])
const scenes = ref([])
const viewDialogVisible = ref(false)
const formDialogVisible = ref(false)
const isEdit = ref(false)
const currentGroup = ref({})
const formRef = ref()

const searchForm = reactive({
  sceneCode: '',
  groupName: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const groupForm = reactive({
  sceneCode: '',
  groupCode: '',
  groupName: '',
  executionMode: 'ALL',
  priority: 100,
  description: '',
  status: 1
})

const formRules = {
  sceneCode: [{ required: true, message: '请选择场景', trigger: 'change' }],
  groupCode: [
    { required: true, message: '请输入规则组编码', trigger: 'blur' },
    {
      pattern: /^[A-Z][A-Z0-9_]*$/,
      message: '编码必须以大写字母开头，只能包含大写字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  groupName: [{ required: true, message: '请输入规则组名称', trigger: 'blur' }],
  executionMode: [{ required: true, message: '请选择执行模式', trigger: 'change' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }]
}

// Load scenes
const loadScenes = async () => {
  try {
    const res = await getSceneList()
    if (res.code === 200) {
      scenes.value = res.data
    }
  } catch (error) {
    console.error('Failed to load scenes:', error)
  }
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

    const res = await getGroupPage(params)
    if (res.code === 200) {
      groups.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载规则组列表失败')
    console.error(error)
  }
  loading.value = false
}

onMounted(() => {
  loadScenes()
  loadData()
})

// Search
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// Reset
const handleReset = () => {
  searchForm.sceneCode = ''
  searchForm.groupName = ''
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

// View
const handleView = (row) => {
  currentGroup.value = row
  viewDialogVisible.value = true
}

// Create
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  formDialogVisible.value = true
}

// Edit
const handleEdit = (row) => {
  isEdit.value = true
  currentGroup.value = row
  groupForm.sceneCode = row.sceneCode
  groupForm.groupCode = row.groupCode
  groupForm.groupName = row.groupName
  groupForm.executionMode = row.executionMode
  groupForm.priority = row.priority
  groupForm.description = row.description || ''
  groupForm.status = row.status
  formDialogVisible.value = true
  viewDialogVisible.value = false
}

// Delete
const handleDelete = async (row) => {
  try {
    const res = await deleteGroup(row.id)
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
    const res = await updateGroupStatus(row.id, row.status)
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

// Submit form
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    const data = {
      sceneCode: groupForm.sceneCode,
      groupCode: groupForm.groupCode,
      groupName: groupForm.groupName,
      executionMode: groupForm.executionMode,
      priority: groupForm.priority,
      description: groupForm.description,
      status: groupForm.status
    }

    submitting.value = true
    let res
    if (isEdit.value) {
      res = await updateGroup(currentGroup.value.id, data)
    } else {
      res = await createGroup(data)
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
  groupForm.sceneCode = ''
  groupForm.groupCode = ''
  groupForm.groupName = ''
  groupForm.executionMode = 'ALL'
  groupForm.priority = 100
  groupForm.description = ''
  groupForm.status = 1
}
</script>

<style lang="scss" scoped>
.rule-group {
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
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
    line-height: 1.6;
  }
}
</style>
