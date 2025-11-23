<template>
  <div class="rule-list">
    <!-- Search and Filter Area -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="业务场景">
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
        <el-form-item label="规则名称">
          <el-input
            v-model="searchForm.ruleName"
            placeholder="请输入规则名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="规则编码">
          <el-input
            v-model="searchForm.ruleCode"
            placeholder="请输入规则编码"
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

    <!-- Toolbar -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>规则列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新增规则
            </el-button>
            <el-button type="success" @click="handleImport">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-dropdown v-if="selectedRows.length > 0" @command="handleBatchCommand">
              <el-button type="warning">
                批量操作 ({{ selectedRows.length }})
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="enable">批量启用</el-dropdown-item>
                  <el-dropdown-item command="disable">批量禁用</el-dropdown-item>
                  <el-dropdown-item command="export">导出选中</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>批量删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <!-- Table -->
      <el-table
        v-loading="loading"
        :data="ruleList"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="ruleCode" label="规则编码" width="150" show-overflow-tooltip />
        <el-table-column prop="ruleName" label="规则名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sceneName" label="业务场景" width="150" />
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getRuleTypeTag(row.ruleType)">
              {{ getRuleTypeName(row.ruleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center" />
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
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleCopy(row)">复制</el-button>
            <el-popconfirm title="确定要删除该规则吗？" @confirm="handleDelete(row)">
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
    <el-dialog v-model="viewDialogVisible" title="规则详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="规则编码">{{ currentRule.ruleCode }}</el-descriptions-item>
        <el-descriptions-item label="规则名称">{{ currentRule.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="业务场景">{{ currentRule.sceneName }}</el-descriptions-item>
        <el-descriptions-item label="规则类型">{{
          getRuleTypeName(currentRule.ruleType)
        }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ currentRule.priority }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentRule.version }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRule.status === 1 ? 'success' : 'info'">
            {{ currentRule.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRule.createTime }}</el-descriptions-item>
        <el-descriptions-item label="规则描述" :span="2">{{
          currentRule.ruleDesc || '无'
        }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentRule)">编辑</el-button>
      </template>
    </el-dialog>

    <!-- Import Dialog -->
    <el-dialog v-model="importDialogVisible" title="导入规则" width="600px">
      <el-form label-width="120px">
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".json"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              选择JSON文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">只能上传 JSON 文件</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="冲突处理">
          <el-radio-group v-model="importForm.conflictStrategy">
            <el-radio label="SKIP">跳过已存在的规则</el-radio>
            <el-radio label="OVERWRITE">覆盖已存在的规则</el-radio>
            <el-radio label="RENAME">重命名后导入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="导入后启用">
          <el-switch v-model="importForm.enableAfterImport" />
        </el-form-item>
      </el-form>

      <!-- Import Result -->
      <div v-if="importResult" class="import-result">
        <el-divider>导入结果</el-divider>
        <el-descriptions :column="4" border>
          <el-descriptions-item label="总数">{{ importResult.total }}</el-descriptions-item>
          <el-descriptions-item label="成功">
            <span class="success-text">{{ importResult.successCount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="跳过">
            <span class="warning-text">{{ importResult.skipCount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败">
            <span class="danger-text">{{ importResult.failCount }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-table
          v-if="importResult.details && importResult.details.length > 0"
          :data="importResult.details"
          size="small"
          max-height="200"
          style="margin-top: 10px"
        >
          <el-table-column prop="ruleCode" label="规则编码" width="150" />
          <el-table-column prop="ruleName" label="规则名称" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getImportStatusType(row.status)" size="small">
                {{ getImportStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="消息" show-overflow-tooltip />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="closeImportDialog">关闭</el-button>
        <el-button type="primary" :loading="importing" :disabled="!importFile" @click="doImport">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download, Upload, ArrowDown } from '@element-plus/icons-vue'
import {
  getRuleList,
  deleteRule,
  updateRuleStatus,
  copyRule,
  exportRules,
  importRules,
  batchDeleteRules,
  batchUpdateRuleStatus
} from '@/api/rule'
import { getSceneList } from '@/api/scene'

const router = useRouter()
const loading = ref(false)
const ruleList = ref([])
const scenes = ref([])
const viewDialogVisible = ref(false)
const currentRule = ref({})
const selectedRows = ref([])

// Import related
const importDialogVisible = ref(false)
const importing = ref(false)
const importFile = ref(null)
const importResult = ref(null)
const uploadRef = ref(null)
const importForm = reactive({
  conflictStrategy: 'SKIP',
  enableAfterImport: false
})

const searchForm = reactive({
  sceneCode: '',
  ruleName: '',
  ruleCode: '',
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

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

// Load rule list
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

    const res = await getRuleList(params)
    if (res.code === 200) {
      // 根据 sceneCode 映射 sceneName
      const records = res.data.records || []
      ruleList.value = records.map((rule) => {
        const scene = scenes.value.find((s) => s.sceneCode === rule.sceneCode)
        return {
          ...rule,
          sceneName: scene ? scene.sceneName : rule.sceneCode
        }
      })
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载规则列表失败')
    console.error(error)
  }
  loading.value = false
}

onMounted(async () => {
  await loadScenes()
  loadData()
})

// Selection change
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// Search
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// Reset
const handleReset = () => {
  searchForm.sceneCode = ''
  searchForm.ruleName = ''
  searchForm.ruleCode = ''
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

// Create rule
const handleCreate = () => {
  router.push('/rule/create')
}

// View rule
const handleView = (row) => {
  currentRule.value = row
  viewDialogVisible.value = true
}

// Edit rule
const handleEdit = (row) => {
  router.push(`/rule/edit/${row.id}`)
  viewDialogVisible.value = false
}

// Copy rule
const handleCopy = async (row) => {
  try {
    const res = await copyRule(row.id)
    if (res.code === 200) {
      ElMessage.success('复制成功')
      loadData()
    } else {
      ElMessage.error(res.message || '复制失败')
    }
  } catch (error) {
    ElMessage.error('复制失败')
    console.error(error)
  }
}

// Delete rule
const handleDelete = async (row) => {
  try {
    const res = await deleteRule(row.id)
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
    const res = await updateRuleStatus(row.id, row.status)
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

// Export
const handleExport = async () => {
  try {
    loading.value = true
    const res = await exportRules(null, searchForm.sceneCode || null)
    if (res.code === 200) {
      const data = res.data
      if (!data || data.length === 0) {
        ElMessage.warning('没有可导出的规则')
        return
      }
      // Download as JSON file
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `rules_export_${new Date().toISOString().slice(0, 10)}.json`
      link.click()
      URL.revokeObjectURL(url)
      ElMessage.success(`成功导出 ${data.length} 条规则`)
    } else {
      ElMessage.error(res.message || '导出失败')
    }
  } catch (error) {
    ElMessage.error('导出失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// Export selected
const handleExportSelected = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要导出的规则')
    return
  }
  try {
    loading.value = true
    const ruleIds = selectedRows.value.map((r) => r.id)
    const res = await exportRules(ruleIds, null)
    if (res.code === 200) {
      const data = res.data
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `rules_selected_${new Date().toISOString().slice(0, 10)}.json`
      link.click()
      URL.revokeObjectURL(url)
      ElMessage.success(`成功导出 ${data.length} 条规则`)
    } else {
      ElMessage.error(res.message || '导出失败')
    }
  } catch (error) {
    ElMessage.error('导出失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// Import
const handleImport = () => {
  importDialogVisible.value = true
  importFile.value = null
  importResult.value = null
  importForm.conflictStrategy = 'SKIP'
  importForm.enableAfterImport = false
}

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const doImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  try {
    importing.value = true
    // Read file content
    const content = await importFile.value.text()
    const rules = JSON.parse(content)

    if (!Array.isArray(rules)) {
      ElMessage.error('文件格式错误，需要JSON数组格式')
      return
    }

    const res = await importRules({
      rules,
      conflictStrategy: importForm.conflictStrategy,
      enableAfterImport: importForm.enableAfterImport
    })

    if (res.code === 200) {
      importResult.value = res.data
      if (res.data.successCount > 0) {
        ElMessage.success(`成功导入 ${res.data.successCount} 条规则`)
        loadData()
      } else if (res.data.skipCount > 0) {
        ElMessage.warning('所有规则已跳过')
      } else {
        ElMessage.error('导入失败')
      }
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (error) {
    if (error instanceof SyntaxError) {
      ElMessage.error('文件格式错误，请检查JSON格式')
    } else {
      ElMessage.error('导入失败')
    }
    console.error(error)
  } finally {
    importing.value = false
  }
}

const closeImportDialog = () => {
  importDialogVisible.value = false
  importFile.value = null
  importResult.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// Batch operations
const handleBatchCommand = async (command) => {
  const ruleIds = selectedRows.value.map((r) => r.id)

  switch (command) {
    case 'enable':
      await handleBatchEnable(ruleIds)
      break
    case 'disable':
      await handleBatchDisable(ruleIds)
      break
    case 'export':
      await handleExportSelected()
      break
    case 'delete':
      await handleBatchDelete(ruleIds)
      break
  }
}

const handleBatchEnable = async (ruleIds) => {
  try {
    await ElMessageBox.confirm(`确定要启用选中的 ${ruleIds.length} 条规则吗？`, '批量启用', {
      type: 'warning'
    })
    const res = await batchUpdateRuleStatus(ruleIds, 1)
    if (res.code === 200) {
      ElMessage.success('批量启用成功')
      loadData()
    } else {
      ElMessage.error(res.message || '批量启用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量启用失败')
      console.error(error)
    }
  }
}

const handleBatchDisable = async (ruleIds) => {
  try {
    await ElMessageBox.confirm(`确定要禁用选中的 ${ruleIds.length} 条规则吗？`, '批量禁用', {
      type: 'warning'
    })
    const res = await batchUpdateRuleStatus(ruleIds, 0)
    if (res.code === 200) {
      ElMessage.success('批量禁用成功')
      loadData()
    } else {
      ElMessage.error(res.message || '批量禁用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁用失败')
      console.error(error)
    }
  }
}

const handleBatchDelete = async (ruleIds) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${ruleIds.length} 条规则吗？此操作不可恢复！`,
      '批量删除',
      {
        type: 'error'
      }
    )
    const res = await batchDeleteRules(ruleIds)
    if (res.code === 200) {
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      loadData()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error(error)
    }
  }
}

// Helper functions
const getRuleTypeTag = (type) => {
  const map = {
    CONDITION: 'primary',
    DECISION_TABLE: 'success',
    SCRIPT: 'warning'
  }
  return map[type] || 'info'
}

const getRuleTypeName = (type) => {
  const map = {
    CONDITION: '条件规则',
    DECISION_TABLE: '决策表',
    SCRIPT: '脚本规则'
  }
  return map[type] || type
}

const getImportStatusType = (status) => {
  const map = {
    SUCCESS: 'success',
    SKIP: 'warning',
    FAIL: 'danger'
  }
  return map[status] || 'info'
}

const getImportStatusText = (status) => {
  const map = {
    SUCCESS: '成功',
    SKIP: '跳过',
    FAIL: '失败'
  }
  return map[status] || status
}
</script>

<style lang="scss" scoped>
.rule-list {
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

  .import-result {
    .success-text {
      color: #67c23a;
      font-weight: bold;
    }
    .warning-text {
      color: #e6a23c;
      font-weight: bold;
    }
    .danger-text {
      color: #f56c6c;
      font-weight: bold;
    }
  }
}
</style>
