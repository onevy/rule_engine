<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>规则总数</template>
          <div class="stat-value">{{ stats.ruleCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>场景数量</template>
          <div class="stat-value">{{ stats.sceneCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>今日执行</template>
          <div class="stat-value">{{ stats.todayExecution }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>命中率</template>
          <div class="stat-value">{{ stats.hitRate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" style="margin-top: 20px">
      <template #header>最近执行记录</template>
      <el-table :data="recentLogs" style="width: 100%">
        <el-table-column prop="traceId" label="追踪ID" width="200" />
        <el-table-column prop="sceneCode" label="场景" width="180" />
        <el-table-column prop="executionResult" label="结果" width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.executionResult === 'HIT'
                  ? 'success'
                  : row.executionResult === 'ERROR'
                    ? 'danger'
                    : 'info'
              "
            >
              {{ row.executionResult }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionTime" label="耗时(ms)" width="100" />
        <el-table-column prop="executeTime" label="执行时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const stats = ref({
  ruleCount: 0,
  sceneCount: 0,
  todayExecution: 0,
  hitRate: 0
})

const recentLogs = ref([])

onMounted(() => {
  // TODO: Load dashboard data from API
  stats.value = {
    ruleCount: 25,
    sceneCount: 3,
    todayExecution: 1234,
    hitRate: 85.5
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-card {
    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #409eff;
      text-align: center;
      padding: 20px 0;
    }
  }
}
</style>
