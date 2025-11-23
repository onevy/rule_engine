<template>
  <div class="execution-log">
    <DataTable
      :data="logs"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :search-fields="searchFields"
      @search="handleSearch"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    >
      <template #executionResult="{ row }">
        <el-tag :type="getResultType(row.executionResult)">
          {{ row.executionResult }}
        </el-tag>
      </template>
      <template #action="{ row }">
        <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
      </template>
    </DataTable>

    <el-dialog v-model="detailVisible" title="日志详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="追踪ID">{{ currentLog.traceId }}</el-descriptions-item>
        <el-descriptions-item label="场景编码">{{ currentLog.sceneCode }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ currentLog.businessId }}</el-descriptions-item>
        <el-descriptions-item label="执行结果">{{
          currentLog.executionResult
        }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ currentLog.executeTime }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ currentLog.executionTime }}</el-descriptions-item>
        <el-descriptions-item label="命中规则" :span="2">{{
          currentLog.hitRules
        }}</el-descriptions-item>
        <el-descriptions-item label="输入数据" :span="2">
          <pre>{{ formatJson(currentLog.inputData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="输出数据" :span="2">
          <pre>{{ formatJson(currentLog.outputData) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import DataTable from '@/components/Common/DataTable.vue'
import { getLogList } from '@/api/log'

const loading = ref(false)
const logs = ref([])
const detailVisible = ref(false)
const currentLog = ref({})

const columns = [
  { prop: 'traceId', label: '追踪ID', width: 200 },
  { prop: 'sceneCode', label: '场景编码', width: 180 },
  { prop: 'businessId', label: '业务ID', width: 150 },
  { prop: 'executionResult', label: '执行结果', width: 100, slot: 'executionResult' },
  { prop: 'hitRules', label: '命中规则' },
  { prop: 'executionTime', label: '耗时(ms)', width: 100 },
  { prop: 'executeTime', label: '执行时间', width: 180 }
]

const searchFields = [
  { prop: 'sceneCode', label: '场景', type: 'input' },
  { prop: 'traceId', label: '追踪ID', type: 'input' },
  {
    prop: 'executionResult',
    label: '结果',
    type: 'select',
    options: [
      { label: '命中', value: 'HIT' },
      { label: '未命中', value: 'MISS' },
      { label: '错误', value: 'ERROR' }
    ]
  }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const searchParams = ref({})

const loadData = async () => {
  loading.value = true
  const res = await getLogList({
    page: pagination.page,
    pageSize: pagination.pageSize,
    ...searchParams.value
  })
  if (res.code === 200) {
    logs.value = res.data.records
    pagination.total = res.data.total
  }
  loading.value = false
}

onMounted(() => {
  loadData()
})

const handleSearch = (params) => {
  searchParams.value = params
  pagination.page = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadData()
}

const handleDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

const getResultType = (result) => {
  const map = { HIT: 'success', MISS: 'info', ERROR: 'danger' }
  return map[result] || 'info'
}

const formatJson = (str) => {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}
</script>

<style lang="scss" scoped>
pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}
</style>
