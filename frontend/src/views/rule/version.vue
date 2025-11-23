<template>
  <div class="rule-version">
    <!-- Header Card -->
    <el-card class="header-card">
      <el-form :inline="true">
        <el-form-item label="选择规则">
          <el-select
            v-model="selectedRuleId"
            placeholder="请选择规则"
            filterable
            style="width: 400px"
            @change="handleRuleChange"
          >
            <el-option
              v-for="rule in rules"
              :key="rule.id"
              :label="`${rule.ruleName} (${rule.ruleCode})`"
              :value="rule.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!selectedRuleId" @click="handleCreateSnapshot">
            <el-icon><Camera /></el-icon>
            创建快照
          </el-button>
          <el-button @click="handleBack">
            <el-icon><Back /></el-icon>
            返回规则列表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Version List Card -->
    <el-card class="table-card" v-if="selectedRuleId">
      <template #header>
        <div class="card-header">
          <span>版本历史</span>
          <el-tag v-if="currentRuleName" type="info">{{ currentRuleName }}</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="versions" border stripe style="width: 100%">
        <el-table-column prop="version" label="版本号" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary">v{{ row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isCurrent" label="当前版本" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isCurrent === 1" type="success">当前</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeLog" label="变更日志" min-width="250" show-overflow-tooltip />
        <el-table-column prop="createBy" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.isCurrent !== 1"
              type="warning"
              link
              @click="handleCompare(row)"
              :disabled="!currentVersion"
            >
              对比当前
            </el-button>
            <el-popconfirm
              v-if="row.isCurrent !== 1"
              title="确定要回滚到该版本吗？"
              @confirm="handleRollback(row)"
            >
              <template #reference>
                <el-button type="success" link>回滚</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-if="row.isCurrent !== 1"
              title="确定要删除该版本吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Empty State -->
    <el-card v-else class="empty-card">
      <el-empty description="请选择规则查看版本历史" />
    </el-card>

    <!-- View Version Dialog -->
    <el-dialog v-model="viewDialogVisible" title="版本详情" width="900px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="版本号">
          <el-tag type="primary">v{{ currentVersionData.version }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否当前版本">
          <el-tag :type="currentVersionData.isCurrent === 1 ? 'success' : 'info'">
            {{ currentVersionData.isCurrent === 1 ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{
          currentVersionData.createBy
        }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          currentVersionData.createTime
        }}</el-descriptions-item>
        <el-descriptions-item label="变更日志" :span="2">{{
          currentVersionData.changeLog || '无'
        }}</el-descriptions-item>
        <el-descriptions-item label="规则快照" :span="2">
          <pre>{{ formatJson(currentVersionData.ruleSnapshot) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentVersionData.isCurrent !== 1"
          type="warning"
          @click="handleRollback(currentVersionData)"
        >
          回滚到此版本
        </el-button>
      </template>
    </el-dialog>

    <!-- Compare Dialog -->
    <el-dialog v-model="compareDialogVisible" title="版本对比" width="1000px">
      <div class="compare-container">
        <div class="compare-header">
          <el-tag type="success">当前版本 (v{{ currentVersion?.version }})</el-tag>
          <el-icon><Right /></el-icon>
          <el-tag type="warning">对比版本 (v{{ compareVersion?.version }})</el-tag>
        </div>
        <el-divider />
        <div class="compare-content">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="version-block">
                <h4>当前版本</h4>
                <pre>{{ formatJson(currentVersion?.ruleSnapshot) }}</pre>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="version-block">
                <h4>对比版本</h4>
                <pre>{{ formatJson(compareVersion?.ruleSnapshot) }}</pre>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <template #footer>
        <el-button @click="compareDialogVisible = false">关闭</el-button>
        <el-button type="warning" @click="handleRollback(compareVersion)">
          回滚到 v{{ compareVersion?.version }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Create Snapshot Dialog -->
    <el-dialog
      v-model="snapshotDialogVisible"
      title="创建版本快照"
      width="500px"
      @close="handleSnapshotDialogClose"
    >
      <el-form
        ref="snapshotFormRef"
        :model="snapshotForm"
        :rules="snapshotRules"
        label-width="100px"
      >
        <el-form-item label="规则">
          <el-input v-model="currentRuleName" disabled />
        </el-form-item>
        <el-form-item label="变更日志" prop="description">
          <el-input
            v-model="snapshotForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入本次变更的说明，如：修复了XXX问题、新增了XXX功能等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="snapshotDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitSnapshot"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, Back, Right } from '@element-plus/icons-vue'
import {
  getVersionList,
  createVersionSnapshot,
  rollbackVersion,
  deleteVersion
} from '@/api/version'
import { getRuleList } from '@/api/rule'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const rules = ref([])
const versions = ref([])
const selectedRuleId = ref(null)
const viewDialogVisible = ref(false)
const compareDialogVisible = ref(false)
const snapshotDialogVisible = ref(false)
const currentVersionData = ref({})
const compareVersion = ref(null)
const snapshotFormRef = ref()

const snapshotForm = reactive({
  description: ''
})

const snapshotRules = {
  description: [{ required: true, message: '请输入变更日志', trigger: 'blur' }]
}

// Computed
const currentRuleName = computed(() => {
  const rule = rules.value.find((r) => r.id === selectedRuleId.value)
  return rule ? `${rule.ruleName} (${rule.ruleCode})` : ''
})

const currentVersion = computed(() => {
  return versions.value.find((v) => v.isCurrent === 1)
})

// Load rules
const loadRules = async () => {
  try {
    const res = await getRuleList({ page: 1, pageSize: 1000 })
    if (res.code === 200) {
      rules.value = res.data.records || []

      // Get rule from route query
      const ruleIdFromQuery = route.query.ruleId
      if (ruleIdFromQuery) {
        selectedRuleId.value = parseInt(ruleIdFromQuery)
        loadVersions()
      }
    }
  } catch (error) {
    console.error('Failed to load rules:', error)
  }
}

// Load versions
const loadVersions = async () => {
  if (!selectedRuleId.value) return
  loading.value = true
  try {
    const res = await getVersionList(selectedRuleId.value)
    if (res.code === 200) {
      versions.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载版本列表失败')
    console.error(error)
  }
  loading.value = false
}

onMounted(() => {
  loadRules()
})

// Rule change
const handleRuleChange = () => {
  loadVersions()
}

// View version
const handleView = (row) => {
  currentVersionData.value = row
  viewDialogVisible.value = true
}

// Compare versions
const handleCompare = (row) => {
  compareVersion.value = row
  compareDialogVisible.value = true
}

// Create snapshot
const handleCreateSnapshot = () => {
  snapshotForm.description = ''
  snapshotDialogVisible.value = true
}

// Submit snapshot
const handleSubmitSnapshot = async () => {
  try {
    await snapshotFormRef.value.validate()

    submitting.value = true
    const res = await createVersionSnapshot(selectedRuleId.value, snapshotForm.description)

    if (res.code === 200) {
      ElMessage.success('快照创建成功')
      snapshotDialogVisible.value = false
      loadVersions()
    } else {
      ElMessage.error(res.message || '快照创建失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Submit error:', error)
    }
  } finally {
    submitting.value = false
  }
}

// Rollback version
const handleRollback = async (row) => {
  try {
    const res = await rollbackVersion({
      ruleId: selectedRuleId.value,
      versionId: row.id
    })

    if (res.code === 200) {
      ElMessage.success('回滚成功')
      viewDialogVisible.value = false
      compareDialogVisible.value = false
      loadVersions()
    } else {
      ElMessage.error(res.message || '回滚失败')
    }
  } catch (error) {
    ElMessage.error('回滚失败')
    console.error(error)
  }
}

// Delete version
const handleDelete = async (row) => {
  try {
    const res = await deleteVersion(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadVersions()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

// Back to rule list
const handleBack = () => {
  router.push('/rule/list')
}

// Dialog close
const handleSnapshotDialogClose = () => {
  snapshotFormRef.value?.resetFields()
  snapshotForm.description = ''
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
.rule-version {
  padding: 20px;

  .header-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .empty-card {
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .compare-container {
    .compare-header {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 20px;
      font-size: 16px;
    }

    .compare-content {
      .version-block {
        h4 {
          margin: 0 0 10px 0;
          padding: 10px;
          background-color: #f5f7fa;
          border-radius: 4px;
        }

        pre {
          margin: 0;
          white-space: pre-wrap;
          word-break: break-all;
          max-height: 500px;
          overflow-y: auto;
          background-color: #f5f7fa;
          padding: 15px;
          border-radius: 4px;
          font-size: 13px;
        }
      }
    }
  }

  pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
    max-height: 400px;
    overflow-y: auto;
    background-color: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    font-size: 13px;
  }
}
</style>
