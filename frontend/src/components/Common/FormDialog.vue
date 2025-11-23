<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-width="labelWidth"
      :disabled="loading"
    >
      <slot :form="formData"></slot>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '对话框'
  },
  width: {
    type: String,
    default: '600px'
  },
  labelWidth: {
    type: String,
    default: '100px'
  },
  formData: {
    type: Object,
    default: () => ({})
  },
  rules: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  confirmText: {
    type: String,
    default: '确定'
  }
})

const emit = defineEmits(['update:modelValue', 'submit', 'close'])

const formRef = ref()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleClose = () => {
  formRef.value?.resetFields()
  emit('close')
  visible.value = false
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    emit('submit', props.formData)
  } catch (error) {
    console.log('Form validation failed:', error)
  }
}

// Expose form methods
defineExpose({
  validate: () => formRef.value?.validate(),
  resetFields: () => formRef.value?.resetFields(),
  clearValidate: () => formRef.value?.clearValidate()
})
</script>
