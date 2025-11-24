<template>
  <div class="condition-group" :class="{ 'is-root': isRoot }">
    <div class="group-header">
      <div class="logic-switch">
        <el-radio-group v-model="localGroup.logic" size="small" @change="emitUpdate">
          <el-radio-button value="AND">并且</el-radio-button>
          <el-radio-button value="OR">或者</el-radio-button>
        </el-radio-group>
      </div>
      <div class="group-actions">
        <el-button type="primary" size="small" @click="addCondition">
          <el-icon><Plus /></el-icon>
          添加条件
        </el-button>
        <el-button
          v-if="depth < maxDepth"
          type="success"
          size="small"
          @click="addGroup"
        >
          <el-icon><FolderAdd /></el-icon>
          添加条件组
        </el-button>
        <el-button v-if="!isRoot" type="danger" size="small" @click="$emit('remove')">
          <el-icon><Delete /></el-icon>
          删除组
        </el-button>
      </div>
    </div>

    <div class="group-content">
      <template v-if="localGroup.conditions && localGroup.conditions.length > 0">
        <div
          v-for="(item, index) in localGroup.conditions"
          :key="index"
          class="condition-item-wrapper"
        >
          <!-- Nested Group -->
          <ConditionGroup
            v-if="item.logic"
            :group="item"
            :fields="fields"
            :is-root="false"
            :depth="depth + 1"
            :max-depth="maxDepth"
            @update="(newGroup) => updateCondition(index, newGroup)"
            @remove="removeCondition(index)"
          />
          <!-- Single Condition -->
          <ConditionItem
            v-else
            :condition="item"
            :fields="fields"
            @update="(newCondition) => updateCondition(index, newCondition)"
            @remove="removeCondition(index)"
          />
        </div>
      </template>
      <div v-else class="empty-tip">
        <el-empty description="暂无条件，请添加条件或条件组" :image-size="60" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Plus, FolderAdd, Delete } from '@element-plus/icons-vue'
import ConditionItem from './ConditionItem.vue'

const props = defineProps({
  group: {
    type: Object,
    default: () => ({
      logic: 'AND',
      conditions: []
    })
  },
  fields: {
    type: Array,
    default: () => []
  },
  isRoot: {
    type: Boolean,
    default: false
  },
  depth: {
    type: Number,
    default: 1
  },
  maxDepth: {
    type: Number,
    default: 2
  }
})

const emit = defineEmits(['update', 'remove'])

const localGroup = ref({ ...props.group })

watch(
  () => props.group,
  (newVal) => {
    localGroup.value = { ...newVal }
  },
  { deep: true }
)

const emitUpdate = () => {
  emit('update', { ...localGroup.value })
}

const addCondition = () => {
  if (!localGroup.value.conditions) {
    localGroup.value.conditions = []
  }
  localGroup.value.conditions.push({
    field: '',
    operator: 'EQ',
    value: '',
    valueType: 'STRING'
  })
  emitUpdate()
}

const addGroup = () => {
  if (!localGroup.value.conditions) {
    localGroup.value.conditions = []
  }
  localGroup.value.conditions.push({
    logic: 'AND',
    conditions: []
  })
  emitUpdate()
}

const updateCondition = (index, newCondition) => {
  localGroup.value.conditions[index] = newCondition
  emitUpdate()
}

const removeCondition = (index) => {
  localGroup.value.conditions.splice(index, 1)
  emitUpdate()
}
</script>

<style lang="scss" scoped>
.condition-group {
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;

  &.is-root {
    background-color: transparent;
    border: none;
    padding: 0;
  }

  .group-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px dashed #e4e7ed;

    .logic-switch {
      .el-radio-group {
        .el-radio-button {
          &:first-child {
            .el-radio-button__inner {
              border-radius: 4px 0 0 4px;
            }
          }
          &:last-child {
            .el-radio-button__inner {
              border-radius: 0 4px 4px 0;
            }
          }
        }
      }
    }

    .group-actions {
      display: flex;
      gap: 8px;
    }
  }

  .group-content {
    .condition-item-wrapper {
      margin-bottom: 10px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .empty-tip {
      padding: 20px 0;
    }
  }
}
</style>
