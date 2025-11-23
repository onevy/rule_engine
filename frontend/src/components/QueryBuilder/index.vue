<template>
  <div class="query-builder">
    <ConditionGroup
      :group="modelValue"
      :fields="fields"
      :is-root="true"
      @update="handleUpdate"
      @remove="handleRemoveRoot"
    />
  </div>
</template>

<script setup>
import { watch } from 'vue'
import ConditionGroup from './ConditionGroup.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({
      logic: 'AND',
      conditions: []
    })
  },
  fields: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

// Initialize default structure if empty
watch(
  () => props.modelValue,
  (val) => {
    if (!val || Object.keys(val).length === 0) {
      emit('update:modelValue', {
        logic: 'AND',
        conditions: []
      })
    }
  },
  { immediate: true }
)

const handleUpdate = (newGroup) => {
  emit('update:modelValue', newGroup)
}

const handleRemoveRoot = () => {
  // Cannot remove root group, reset it
  emit('update:modelValue', {
    logic: 'AND',
    conditions: []
  })
}
</script>

<style lang="scss" scoped>
.query-builder {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
