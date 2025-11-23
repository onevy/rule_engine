<template>
  <div class="scene-metadata">
    <el-card class="header-card">
      <el-form :inline="true">
        <el-form-item label="选择场景">
          <el-select
            v-model="selectedScene"
            placeholder="请选择场景"
            style="width: 300px"
            @change="loadMetadata"
          >
            <el-option
              v-for="scene in scenes"
              :key="scene.sceneCode"
              :label="`${scene.sceneName} (${scene.sceneCode})`"
              :value="scene.sceneCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!selectedScene" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增字段
          </el-button>
          <el-button @click="handleBack">
            <el-icon><Back /></el-icon>
            返回场景列表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <span>字段元数据列表</span>
      </template>
      <el-table :data="metadata" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="fieldCode" label="字段编码" width="180" show-overflow-tooltip />
        <el-table-column prop="fieldName" label="字段名称" width="150" show-overflow-tooltip />
        <el-table-column prop="fieldType" label="字段类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getFieldTypeLabel(row.fieldType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column
          prop="operators"
          label="支持操作符"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="defaultValue" label="默认值" width="120" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定要删除该字段吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑字段' : '新增字段'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="metadataForm" :rules="formRules" label-width="120px">
        <el-form-item label="所属场景">
          <el-input v-model="selectedSceneName" disabled />
        </el-form-item>
        <el-form-item label="字段编码" prop="fieldCode">
          <el-input
            v-model="metadataForm.fieldCode"
            placeholder="请输入字段编码，如：diagnosis_code"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="字段名称" prop="fieldName">
          <el-input v-model="metadataForm.fieldName" placeholder="请输入字段名称，如：诊断编码" />
        </el-form-item>
        <el-form-item label="字段类型" prop="fieldType">
          <el-select
            v-model="metadataForm.fieldType"
            placeholder="请选择字段类型"
            style="width: 100%"
          >
            <el-option label="字符串" value="STRING" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="布尔值" value="BOOLEAN" />
            <el-option label="日期" value="DATE" />
            <el-option label="数组" value="ARRAY" />
            <el-option label="对象" value="OBJECT" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="metadataForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="诊断" value="DIAGNOSIS" />
            <el-option label="检验" value="LAB_TEST" />
            <el-option label="检查" value="EXAM" />
            <el-option label="手术" value="SURGERY" />
            <el-option label="药品" value="MEDICINE" />
            <el-option label="患者" value="PATIENT" />
            <el-option label="病历" value="MEDICAL_RECORD" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="支持操作符" prop="operators">
          <el-select
            v-model="metadataForm.operatorsList"
            multiple
            placeholder="请选择支持的操作符"
            style="width: 100%"
          >
            <el-option label="等于 (EQ)" value="EQ" />
            <el-option label="不等于 (NEQ)" value="NEQ" />
            <el-option label="大于 (GT)" value="GT" />
            <el-option label="大于等于 (GTE)" value="GTE" />
            <el-option label="小于 (LT)" value="LT" />
            <el-option label="小于等于 (LTE)" value="LTE" />
            <el-option label="包含 (IN)" value="IN" />
            <el-option label="不包含 (NOT_IN)" value="NOT_IN" />
            <el-option label="模糊匹配 (LIKE)" value="LIKE" />
            <el-option label="左匹配 (START_WITH)" value="START_WITH" />
            <el-option label="右匹配 (END_WITH)" value="END_WITH" />
            <el-option label="范围 (BETWEEN)" value="BETWEEN" />
            <el-option label="为空 (IS_NULL)" value="IS_NULL" />
            <el-option label="不为空 (IS_NOT_NULL)" value="IS_NOT_NULL" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认值">
          <el-input v-model="metadataForm.defaultValue" placeholder="请输入默认值（可选）" />
        </el-form-item>
        <el-form-item label="值域配置">
          <el-input
            v-model="metadataForm.valueRange"
            type="textarea"
            :rows="3"
            placeholder='值域配置（JSON格式），如：["选项1", "选项2"] 或 {"min": 0, "max": 100}'
          />
        </el-form-item>
        <el-form-item label="字段描述">
          <el-input
            v-model="metadataForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入字段描述"
          />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Back } from '@element-plus/icons-vue'
import {
  getSceneList,
  getSceneMetadata,
  createMetadata,
  updateMetadata,
  deleteMetadata
} from '@/api/scene'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const scenes = ref([])
const selectedScene = ref('')
const metadata = ref([])
const formDialogVisible = ref(false)
const isEdit = ref(false)
const currentMetadata = ref({})
const formRef = ref()

const metadataForm = reactive({
  sceneCode: '',
  fieldCode: '',
  fieldName: '',
  fieldType: '',
  category: '',
  operatorsList: [],
  defaultValue: '',
  valueRange: '',
  description: ''
})

const formRules = {
  fieldCode: [
    { required: true, message: '请输入字段编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9_]*$/,
      message: '编码必须以小写字母开头，只能包含小写字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  fieldName: [{ required: true, message: '请输入字段名称', trigger: 'blur' }],
  fieldType: [{ required: true, message: '请选择字段类型', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  operators: [{ required: true, message: '请选择支持的操作符', trigger: 'change' }]
}

const selectedSceneName = computed(() => {
  const scene = scenes.value.find((s) => s.sceneCode === selectedScene.value)
  return scene ? `${scene.sceneName} (${scene.sceneCode})` : ''
})

// Load scenes
const loadScenes = async () => {
  try {
    const res = await getSceneList()
    if (res.code === 200) {
      scenes.value = res.data

      // Get scene from route query
      const sceneCodeFromQuery = route.query.sceneCode
      if (sceneCodeFromQuery && scenes.value.some((s) => s.sceneCode === sceneCodeFromQuery)) {
        selectedScene.value = sceneCodeFromQuery
      } else if (scenes.value.length > 0) {
        selectedScene.value = scenes.value[0].sceneCode
      }

      if (selectedScene.value) {
        loadMetadata()
      }
    }
  } catch (error) {
    console.error('Failed to load scenes:', error)
  }
}

// Load metadata
const loadMetadata = async () => {
  if (!selectedScene.value) return
  loading.value = true
  try {
    const res = await getSceneMetadata(selectedScene.value)
    if (res.code === 200) {
      metadata.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载元数据失败')
    console.error(error)
  }
  loading.value = false
}

onMounted(() => {
  loadScenes()
})

// Create metadata
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  metadataForm.sceneCode = selectedScene.value
  formDialogVisible.value = true
}

// Edit metadata
const handleEdit = (row) => {
  isEdit.value = true
  currentMetadata.value = row
  metadataForm.sceneCode = row.sceneCode
  metadataForm.fieldCode = row.fieldCode
  metadataForm.fieldName = row.fieldName
  metadataForm.fieldType = row.fieldType
  metadataForm.category = row.category
  metadataForm.operatorsList = row.operators ? row.operators.split(',') : []
  metadataForm.defaultValue = row.defaultValue || ''
  metadataForm.valueRange = row.valueRange || ''
  metadataForm.description = row.description || ''
  formDialogVisible.value = true
}

// Delete metadata
const handleDelete = async (row) => {
  try {
    const res = await deleteMetadata(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMetadata()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

// Submit form
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // Validate value range JSON
    if (metadataForm.valueRange) {
      try {
        JSON.parse(metadataForm.valueRange)
      } catch (e) {
        ElMessage.error('值域配置格式错误，请检查JSON格式')
        return
      }
    }

    const data = {
      sceneCode: metadataForm.sceneCode,
      fieldCode: metadataForm.fieldCode,
      fieldName: metadataForm.fieldName,
      fieldType: metadataForm.fieldType,
      category: metadataForm.category,
      operators: metadataForm.operatorsList.join(','),
      defaultValue: metadataForm.defaultValue,
      valueRange: metadataForm.valueRange,
      description: metadataForm.description
    }

    submitting.value = true
    let res
    if (isEdit.value) {
      res = await updateMetadata(currentMetadata.value.id, data)
    } else {
      res = await createMetadata(data)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
      formDialogVisible.value = false
      loadMetadata()
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
  metadataForm.sceneCode = ''
  metadataForm.fieldCode = ''
  metadataForm.fieldName = ''
  metadataForm.fieldType = ''
  metadataForm.category = ''
  metadataForm.operatorsList = []
  metadataForm.defaultValue = ''
  metadataForm.valueRange = ''
  metadataForm.description = ''
}

// Back to scene list
const handleBack = () => {
  router.push('/scene/list')
}

// Helper function
const getFieldTypeLabel = (type) => {
  const map = {
    STRING: '字符串',
    NUMBER: '数字',
    BOOLEAN: '布尔值',
    DATE: '日期',
    ARRAY: '数组',
    OBJECT: '对象'
  }
  return map[type] || type
}
</script>

<style lang="scss" scoped>
.scene-metadata {
  padding: 20px;

  .header-card {
    margin-bottom: 20px;
  }

  .table-card {
    margin-top: 20px;
  }
}
</style>
