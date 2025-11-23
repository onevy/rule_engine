<template>
  <div class="condition-item">
    <div class="condition-content">
      <!-- Field Selection -->
      <el-select
        v-model="localCondition.field"
        placeholder="选择字段"
        filterable
        class="field-select"
        @change="handleFieldChange"
      >
        <el-option-group
          v-for="category in groupedFields"
          :key="category.label"
          :label="category.label"
        >
          <el-option
            v-for="field in category.fields"
            :key="field.fieldCode"
            :label="field.fieldName"
            :value="field.fieldCode"
          />
        </el-option-group>
      </el-select>

      <!-- Operator Selection -->
      <el-select
        v-model="localCondition.operator"
        placeholder="操作符"
        class="operator-select"
        @change="handleOperatorChange"
      >
        <el-option
          v-for="op in availableOperators"
          :key="op.value"
          :label="op.label"
          :value="op.value"
        />
      </el-select>

      <!-- Value Input -->
      <div class="value-input">
        <!-- Multiple Values (IN, NOT_IN) -->
        <el-select
          v-if="isMultipleValue"
          v-model="localCondition.value"
          multiple
          filterable
          allow-create
          default-first-option
          :placeholder="getValuePlaceholder"
          @change="emitUpdate"
        >
          <el-option
            v-for="opt in fieldOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>

        <!-- Range Value (BETWEEN) -->
        <template v-else-if="localCondition.operator === 'BETWEEN'">
          <el-input
            v-model="rangeValue.min"
            :placeholder="'最小值'"
            style="width: 100px"
            @input="handleRangeChange"
          />
          <span class="range-separator">至</span>
          <el-input
            v-model="rangeValue.max"
            :placeholder="'最大值'"
            style="width: 100px"
            @input="handleRangeChange"
          />
        </template>

        <!-- Boolean Value -->
        <el-select
          v-else-if="currentFieldType === 'BOOLEAN'"
          v-model="localCondition.value"
          placeholder="请选择"
          @change="emitUpdate"
        >
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
        </el-select>

        <!-- Number Value -->
        <el-input-number
          v-else-if="currentFieldType === 'NUMBER'"
          v-model="localCondition.value"
          :placeholder="getValuePlaceholder"
          controls-position="right"
          @change="emitUpdate"
        />

        <!-- Date Value -->
        <el-date-picker
          v-else-if="currentFieldType === 'DATE'"
          v-model="localCondition.value"
          type="date"
          :placeholder="getValuePlaceholder"
          value-format="YYYY-MM-DD"
          @change="emitUpdate"
        />

        <!-- Select from Options -->
        <el-select
          v-else-if="fieldOptions.length > 0"
          v-model="localCondition.value"
          filterable
          :placeholder="getValuePlaceholder"
          @change="emitUpdate"
        >
          <el-option
            v-for="opt in fieldOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>

        <!-- Default Text Input -->
        <el-input
          v-else
          v-model="localCondition.value"
          :placeholder="getValuePlaceholder"
          @input="emitUpdate"
        />
      </div>
    </div>

    <div class="condition-actions">
      <el-button type="danger" circle size="small" @click="$emit('remove')">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  condition: {
    type: Object,
    default: () => ({
      field: '',
      operator: 'EQ',
      value: '',
      valueType: 'STRING'
    })
  },
  fields: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update', 'remove'])

const localCondition = ref({ ...props.condition })
const rangeValue = ref({ min: '', max: '' })

// Watch for external changes
watch(
  () => props.condition,
  (newVal) => {
    localCondition.value = { ...newVal }
    if (newVal.operator === 'BETWEEN' && Array.isArray(newVal.value)) {
      rangeValue.value = {
        min: newVal.value[0] || '',
        max: newVal.value[1] || ''
      }
    }
  },
  { deep: true }
)

// Group fields by category
const groupedFields = computed(() => {
  const groups = {}
  props.fields.forEach((field) => {
    const category = field.fieldCategory || '其他'
    if (!groups[category]) {
      groups[category] = []
    }
    groups[category].push(field)
  })
  return Object.keys(groups).map((key) => ({
    label: key,
    fields: groups[key]
  }))
})

// Get current field object
const currentField = computed(() => {
  return props.fields.find((f) => f.fieldCode === localCondition.value.field)
})

// Get current field type
const currentFieldType = computed(() => {
  return currentField.value?.valueType || 'STRING'
})

// Get field options
const fieldOptions = computed(() => {
  if (currentField.value?.options) {
    return currentField.value.options
  }
  return []
})

// All operators
const allOperators = [
  { value: 'EQ', label: '等于', types: ['STRING', 'NUMBER', 'BOOLEAN', 'DATE'] },
  { value: 'NEQ', label: '不等于', types: ['STRING', 'NUMBER', 'BOOLEAN', 'DATE'] },
  { value: 'GT', label: '大于', types: ['NUMBER', 'DATE'] },
  { value: 'GTE', label: '大于等于', types: ['NUMBER', 'DATE'] },
  { value: 'LT', label: '小于', types: ['NUMBER', 'DATE'] },
  { value: 'LTE', label: '小于等于', types: ['NUMBER', 'DATE'] },
  { value: 'IN', label: '包含', types: ['STRING', 'NUMBER', 'ARRAY'] },
  { value: 'NOT_IN', label: '不包含', types: ['STRING', 'NUMBER', 'ARRAY'] },
  { value: 'LIKE', label: '模糊匹配', types: ['STRING'] },
  { value: 'LEFT_MATCH', label: '左匹配', types: ['STRING'] },
  { value: 'BETWEEN', label: '范围', types: ['NUMBER', 'DATE'] },
  {
    value: 'IS_NULL',
    label: '为空',
    types: ['STRING', 'NUMBER', 'BOOLEAN', 'DATE', 'ARRAY', 'OBJECT']
  },
  {
    value: 'IS_NOT_NULL',
    label: '不为空',
    types: ['STRING', 'NUMBER', 'BOOLEAN', 'DATE', 'ARRAY', 'OBJECT']
  }
]

// Available operators based on field type
const availableOperators = computed(() => {
  const type = currentFieldType.value
  return allOperators.filter((op) => op.types.includes(type))
})

// Check if operator requires multiple values
const isMultipleValue = computed(() => {
  return ['IN', 'NOT_IN'].includes(localCondition.value.operator)
})

// Get value placeholder
const getValuePlaceholder = computed(() => {
  const op = localCondition.value.operator
  if (op === 'IS_NULL' || op === 'IS_NOT_NULL') {
    return ''
  }
  return '请输入值'
})

const emitUpdate = () => {
  emit('update', { ...localCondition.value })
}

const handleFieldChange = () => {
  // Reset operator and value when field changes
  const type = currentFieldType.value
  const validOperators = allOperators.filter((op) => op.types.includes(type))

  if (!validOperators.find((op) => op.value === localCondition.value.operator)) {
    localCondition.value.operator = validOperators[0]?.value || 'EQ'
  }

  // Reset value
  if (isMultipleValue.value) {
    localCondition.value.value = []
  } else {
    localCondition.value.value = ''
  }

  localCondition.value.valueType = type
  emitUpdate()
}

const handleOperatorChange = () => {
  // Reset value when operator changes
  if (isMultipleValue.value) {
    if (!Array.isArray(localCondition.value.value)) {
      localCondition.value.value = []
    }
  } else if (localCondition.value.operator === 'BETWEEN') {
    localCondition.value.value = ['', '']
    rangeValue.value = { min: '', max: '' }
  } else {
    if (Array.isArray(localCondition.value.value)) {
      localCondition.value.value = ''
    }
  }
  emitUpdate()
}

const handleRangeChange = () => {
  localCondition.value.value = [rangeValue.value.min, rangeValue.value.max]
  emitUpdate()
}
</script>

<style lang="scss" scoped>
.condition-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background-color: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 4px;

  .condition-content {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;

    .field-select {
      width: 180px;
    }

    .operator-select {
      width: 120px;
    }

    .value-input {
      flex: 1;
      min-width: 150px;
      display: flex;
      align-items: center;
      gap: 5px;

      .el-select,
      .el-input,
      .el-input-number,
      .el-date-picker {
        width: 100%;
      }

      .range-separator {
        color: #909399;
        padding: 0 5px;
      }
    }
  }

  .condition-actions {
    flex-shrink: 0;
  }
}
</style>
