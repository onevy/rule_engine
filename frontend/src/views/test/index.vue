<template>
  <div class="rule-test">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>测试配置</template>
          <el-form :model="testForm" label-width="100px">
            <el-form-item label="选择场景">
              <el-select
                v-model="testForm.sceneCode"
                placeholder="请选择场景"
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
            <el-form-item label="业务ID">
              <el-input v-model="testForm.businessId" placeholder="请输入业务ID（可选）" />
            </el-form-item>
            <el-form-item label="输入数据">
              <div style="margin-bottom: 10px">
                <el-button
                  type="success"
                  size="small"
                  :disabled="!testForm.sceneCode"
                  @click="loadMetadata"
                >
                  <el-icon><Download /></el-icon>
                  加载示例数据
                </el-button>
                <el-button size="small" @click="handleClearData">
                  <el-icon><Delete /></el-icon>
                  清空数据
                </el-button>
              </div>
              <el-input
                v-model="testForm.inputDataStr"
                type="textarea"
                :rows="12"
                placeholder="请输入JSON格式的测试数据，或点击上方按钮加载示例数据"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="testing" @click="handleTest">
                <el-icon><CircleCheck /></el-icon>
                执行测试
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>测试结果</template>
          <div v-if="!result" class="empty-result">
            <el-empty description="暂无测试结果" />
          </div>
          <div v-else>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="是否匹配">
                <el-tag :type="result.matched ? 'success' : 'info'">
                  {{ result.matched ? '是' : '否' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="追踪ID">{{ result.traceId }}</el-descriptions-item>
              <el-descriptions-item label="执行耗时"
                >{{ result.executionTime }} ms</el-descriptions-item
              >
              <el-descriptions-item label="命中规则">
                <el-tag
                  v-for="rule in result.matchedRuleCodes"
                  :key="rule"
                  style="margin-right: 5px"
                >
                  {{ rule }}
                </el-tag>
                <span v-if="!result.matchedRuleCodes?.length">无</span>
              </el-descriptions-item>
              <el-descriptions-item label="输出数据">
                <pre>{{ formatJson(result.outputData) }}</pre>
              </el-descriptions-item>
              <el-descriptions-item v-if="result.errorMessage" label="错误信息">
                <span style="color: #f56c6c">{{ result.errorMessage }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Delete, CircleCheck, Refresh } from '@element-plus/icons-vue'
import { executeRule } from '@/api/rule'
import { getSceneList } from '@/api/scene'

const scenes = ref([])
const testing = ref(false)
const result = ref(null)

const testForm = ref({
  sceneCode: '',
  businessId: '',
  inputDataStr: '{}'
})

onMounted(async () => {
  const res = await getSceneList()
  if (res.code === 200) {
    scenes.value = res.data
  }
})

const handleSceneChange = () => {
  // Clear data when scene changes
  testForm.value.inputDataStr = '{}'
  result.value = null
}

const loadMetadata = () => {
  // Load sample data based on scene
  if (!testForm.value.sceneCode) return

  const sampleData = getSampleDataByScene(testForm.value.sceneCode)
  if (sampleData) {
    testForm.value.inputDataStr = JSON.stringify(sampleData, null, 2)
    ElMessage.success('已加载示例数据')
  } else {
    ElMessage.warning('该场景暂无示例数据')
  }
}

const handleClearData = () => {
  testForm.value.inputDataStr = '{}'
  ElMessage.info('数据已清空')
}

// Sample data templates for different scenes
const getSampleDataByScene = (sceneCode) => {
  const sampleDataMap = {
    INFECTIOUS_DISEASE_REPORT: {
      // 传染病报卡场景示例数据
      diagnosisCode: 'B22.100',
      diagnosisName: '艾滋病病毒病伴有卡波西肉瘤',
      patientName: '张三',
      patientAge: 35,
      patientGender: 'M',
      identityCard: '110101198801010001',
      contactPhone: '13800138000',
      address: '北京市朝阳区XX街道XX号',
      labTest: {
        HIV_antibody: 'positive',
        CD4_count: 150,
        test_date: '2025-01-15'
      },
      visitType: 'OUTPATIENT',
      visitDate: '2025-01-15',
      department: '感染科'
    },
    PRESCRIPTION_AUDIT: {
      // 处方审核场景示例数据
      prescriptionId: 'RX20250115001',
      patientId: 'P10001',
      patientName: '李四',
      patientAge: 65,
      patientGender: 'F',
      patientWeight: 55,
      diagnosis: '高血压、糖尿病',
      isPregnant: false,
      isLactating: false,
      isPediatric: false,
      liverFunction: 'normal',
      kidneyFunction: 'mild_impairment',
      creatinineClearance: 45,
      allergyHistory: ['青霉素'],
      medications: [
        {
          drugCode: 'D001',
          drugName: '阿司匹林肠溶片',
          dosage: '100mg',
          frequency: 'qd',
          route: 'oral',
          duration: 30
        },
        {
          drugCode: 'D002',
          drugName: '二甲双胍片',
          dosage: '500mg',
          frequency: 'bid',
          route: 'oral',
          duration: 30
        }
      ],
      prescriptionDate: '2025-01-15',
      doctorName: '王医生'
    },
    MEDICAL_RECORD_QC: {
      // 病历质控场景示例数据
      recordId: 'MR20250115001',
      patientId: 'P10002',
      patientName: '赵六',
      admissionDate: '2025-01-10',
      dischargeDate: '2025-01-15',
      lengthOfStay: 5,
      department: '内科',
      wardNumber: '501',
      bedNumber: '12',
      mainDiagnosis: '肺炎',
      additionalDiagnosis: ['高血压', '糖尿病'],
      surgeries: [],
      chiefComplaint: '咳嗽、发热3天',
      presentIllness: '患者3天前无明显诱因出现咳嗽、发热...',
      physicalExamination: '体温38.5℃，血压130/85mmHg...',
      laboratoryTests: {
        bloodRoutine: {
          WBC: 12.5,
          neutrophil: 0.85
        },
        CRP: 45
      },
      imagingStudies: {
        chestXray: '右下肺炎症改变'
      },
      costs: {
        totalCost: 8500,
        medicineCost: 3200,
        examinationCost: 2100,
        treatmentCost: 1800,
        bedCost: 1400
      },
      medicineRatio: 0.376,
      isComplete: true,
      isSigned: true,
      recordQuality: 'good'
    }
  }

  return sampleDataMap[sceneCode] || null
}

const handleTest = async () => {
  if (!testForm.value.sceneCode) {
    ElMessage.warning('请选择场景')
    return
  }

  let inputData
  try {
    inputData = JSON.parse(testForm.value.inputDataStr)
  } catch (e) {
    ElMessage.error('输入数据JSON格式错误')
    return
  }

  testing.value = true
  try {
    const res = await executeRule({
      sceneCode: testForm.value.sceneCode,
      businessId: testForm.value.businessId,
      inputData
    })
    if (res.code === 200) {
      result.value = res.data
    } else {
      ElMessage.error(res.message || '测试失败')
    }
  } catch (error) {
    ElMessage.error('测试失败')
  }
  testing.value = false
}

const handleReset = () => {
  testForm.value = {
    sceneCode: '',
    businessId: '',
    inputDataStr: '{}'
  }
  result.value = null
}

const formatJson = (obj) => {
  try {
    return JSON.stringify(obj, null, 2)
  } catch {
    return obj
  }
}
</script>

<style lang="scss" scoped>
.empty-result {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
