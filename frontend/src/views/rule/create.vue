<template>
  <div class="rule-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建规则</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="ruleForm"
        :rules="formRules"
        label-width="120px"
        class="rule-form"
      >
        <!-- Basic Information -->
        <el-divider content-position="left">基本信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务场景" prop="sceneCode">
              <el-select
                v-model="ruleForm.sceneCode"
                placeholder="请选择业务场景"
                style="width: 100%"
                @change="handleSceneChange"
              >
                <el-option
                  v-for="scene in scenes"
                  :key="scene.sceneCode"
                  :label="scene.sceneName"
                  :value="scene.sceneCode"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则组" prop="groupId">
              <el-select
                v-model="ruleForm.groupId"
                placeholder="请选择规则组"
                style="width: 100%"
                clearable
              >
                <el-option
                  v-for="group in groups"
                  :key="group.id"
                  :label="group.groupName"
                  :value="group.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则编码" prop="ruleCode">
              <el-input v-model="ruleForm.ruleCode" placeholder="请输入规则编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则类型" prop="ruleType">
              <el-select
                v-model="ruleForm.ruleType"
                placeholder="请选择规则类型"
                style="width: 100%"
              >
                <el-option label="条件规则" value="CONDITION" />
                <el-option label="决策表" value="DECISION_TABLE" />
                <el-option label="脚本规则" value="SCRIPT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number
                v-model="ruleForm.priority"
                :min="1"
                :max="1000"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="规则描述">
          <el-input
            v-model="ruleForm.ruleDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入规则描述"
          />
        </el-form-item>

        <el-form-item label="是否启用">
          <el-switch v-model="ruleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效开始时间">
              <el-date-picker
                v-model="ruleForm.effectiveStartTime"
                type="datetime"
                placeholder="选择生效开始时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生效结束时间">
              <el-date-picker
                v-model="ruleForm.effectiveEndTime"
                type="datetime"
                placeholder="选择生效结束时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- Condition Configuration -->
        <el-divider content-position="left">条件配置</el-divider>

        <el-form-item label="触发条件">
          <div class="query-builder-container">
            <QueryBuilder v-model="ruleForm.conditions" :fields="metadata" />
          </div>
        </el-form-item>

        <!-- Action Configuration -->
        <el-divider content-position="left">动作配置</el-divider>

        <el-form-item label="执行动作">
          <div class="actions-container">
            <div v-for="(action, index) in ruleForm.actions" :key="index" class="action-item">
              <el-select v-model="action.actionType" placeholder="动作类型" style="width: 150px">
                <el-option label="返回数据" value="RETURN" />
                <el-option label="修改数据" value="MODIFY" />
                <el-option label="记录日志" value="LOG" />
                <el-option label="调用服务" value="CALL_SERVICE" />
                <el-option label="发送消息" value="SEND_MESSAGE" />
              </el-select>

              <el-input
                v-if="action.actionType === 'LOG'"
                v-model="action.message"
                placeholder="日志内容"
                style="flex: 1; margin-left: 10px"
              />

              <div v-else-if="action.actionType === 'RETURN'" style="flex: 1; margin-left: 10px">
                <el-input
                  v-model="action.paramsStr"
                  type="textarea"
                  :rows="2"
                  placeholder='返回数据，JSON格式，如：{"need_report": true, "disease_code": "HIV"}'
                />
              </div>

              <div
                v-else-if="action.actionType === 'CALL_SERVICE'"
                style="flex: 1; margin-left: 10px"
              >
                <el-input v-model="action.serviceName" placeholder="服务名称" />
              </div>

              <el-button
                type="danger"
                circle
                size="small"
                style="margin-left: 10px"
                @click="removeAction(index)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>

            <el-button type="primary" @click="addAction">
              <el-icon><Plus /></el-icon>
              添加动作
            </el-button>
          </div>
        </el-form-item>

        <!-- Form Actions -->
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Close } from '@element-plus/icons-vue'
import QueryBuilder from '@/components/QueryBuilder/index.vue'
import { createRule } from '@/api/rule'
import { getSceneList, getMetadataByScene } from '@/api/scene'
import { getGroupList } from '@/api/group'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const scenes = ref([])
const groups = ref([])
const metadata = ref([])

const ruleForm = reactive({
  sceneCode: '',
  groupId: null,
  ruleCode: '',
  ruleName: '',
  ruleType: 'CONDITION',
  priority: 100,
  ruleDesc: '',
  status: 1,
  effectiveStartTime: null,
  effectiveEndTime: null,
  conditions: {
    logic: 'AND',
    conditions: []
  },
  actions: []
})

const formRules = {
  sceneCode: [{ required: true, message: '请选择业务场景', trigger: 'change' }],
  ruleCode: [
    { required: true, message: '请输入规则编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
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

// Load groups
const loadGroups = async () => {
  try {
    const res = await getGroupList()
    if (res.code === 200) {
      groups.value = res.data
    }
  } catch (error) {
    console.error('Failed to load groups:', error)
  }
}

// Handle scene change - load metadata
const handleSceneChange = async () => {
  if (!ruleForm.sceneCode) {
    metadata.value = []
    return
  }

  try {
    const res = await getMetadataByScene(ruleForm.sceneCode)
    if (res.code === 200) {
      metadata.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load metadata:', error)
    metadata.value = []
  }
}

onMounted(() => {
  loadScenes()
  loadGroups()
})

// Add action
const addAction = () => {
  ruleForm.actions.push({
    actionType: 'RETURN',
    paramsStr: '',
    message: '',
    serviceName: ''
  })
}

// Remove action
const removeAction = (index) => {
  ruleForm.actions.splice(index, 1)
}

// 递归展平嵌套的条件组，并映射字段名为后端格式
// 使用共享计数器对象确保每个条件组获得唯一的 groupId
const flattenConditions = (conditionGroup, counter = { value: 0 }) => {
  const result = []
  if (!conditionGroup) return result

  // 获取当前组的 logic 和 conditions
  const logic = conditionGroup.logic || 'AND'
  const conditions = conditionGroup.conditions

  if (!conditions || !Array.isArray(conditions)) return result

  // 为当前组分配一个 groupId
  counter.value++
  const currentGroupId = counter.value

  conditions.forEach((item) => {
    if (item.logic && item.conditions) {
      // 嵌套的条件组，递归处理，共享计数器
      const nestedConditions = flattenConditions(item, counter)
      result.push(...nestedConditions)
    } else if (item.field) {
      // 单个条件，映射字段名为后端格式
      result.push({
        fieldCode: item.field,
        operator: item.operator,
        fieldValue: item.value,
        valueType: item.valueType || 'STRING',
        groupId: currentGroupId,
        groupLogic: logic // 保存当前组的逻辑关系
      })
    }
  })
  return result
}

// Submit form
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // Process actions
    const actions = ruleForm.actions.map((action) => {
      const result = {
        actionType: action.actionType
      }

      if (action.actionType === 'RETURN' && action.paramsStr) {
        try {
          // 验证 JSON 格式
          JSON.parse(action.paramsStr)
          // 后端 actionParams 是 String 类型，直接传 JSON 字符串
          result.actionParams = action.paramsStr
        } catch (e) {
          ElMessage.error('返回数据格式错误，请检查JSON格式')
          throw e
        }
      } else if (action.actionType === 'LOG') {
        result.message = action.message
      } else if (action.actionType === 'CALL_SERVICE') {
        result.serviceName = action.serviceName
      }

      return result
    })

    // 展平嵌套条件并映射字段名为后端格式
    const conditions = flattenConditions(ruleForm.conditions)

    const data = {
      ...ruleForm,
      conditions,
      actions
    }

    submitting.value = true
    const res = await createRule(data)

    if (res.code === 200) {
      ElMessage.success('创建成功')
      router.push('/rule/list')
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Submit error:', error)
    }
  } finally {
    submitting.value = false
  }
}

// Reset form
const handleReset = () => {
  formRef.value?.resetFields()
  ruleForm.conditions = {
    logic: 'AND',
    conditions: []
  }
  ruleForm.actions = []
  ruleForm.effectiveStartTime = null
  ruleForm.effectiveEndTime = null
  metadata.value = []
}

// Go back
const goBack = () => {
  router.push('/rule/list')
}
</script>

<style lang="scss" scoped>
.rule-create {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .rule-form {
    max-width: 1200px;

    .query-builder-container {
      width: 100%;
      min-height: 200px;
    }

    .actions-container {
      width: 100%;

      .action-item {
        display: flex;
        align-items: flex-start;
        margin-bottom: 10px;
        padding: 10px;
        background-color: #f5f7fa;
        border-radius: 4px;
      }
    }
  }
}
</style>
