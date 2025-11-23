<template>
  <div class="data-table">
    <!-- Search Form -->
    <el-form v-if="showSearch && searchFields.length" :inline="true" class="search-form">
      <el-form-item v-for="field in searchFields" :key="field.prop" :label="field.label">
        <el-input
          v-if="field.type === 'input'"
          v-model="searchForm[field.prop]"
          :placeholder="field.placeholder || `请输入${field.label}`"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-select
          v-else-if="field.type === 'select'"
          v-model="searchForm[field.prop]"
          :placeholder="field.placeholder || `请选择${field.label}`"
          clearable
        >
          <el-option
            v-for="opt in field.options"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-date-picker
          v-else-if="field.type === 'daterange'"
          v-model="searchForm[field.prop]"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
      </el-form-item>
    </el-form>

    <!-- Toolbar -->
    <div class="toolbar" v-if="$slots.toolbar">
      <slot name="toolbar"></slot>
    </div>

    <!-- Table -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="data"
      :border="border"
      :stripe="stripe"
      :height="height"
      :max-height="maxHeight"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column v-if="selection" type="selection" width="55" />
      <el-table-column v-if="index" type="index" label="序号" width="60" />

      <template v-for="col in columns" :key="col.prop">
        <el-table-column
          v-if="!col.slot"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :sortable="col.sortable"
          :formatter="col.formatter"
          :show-overflow-tooltip="col.showOverflowTooltip !== false"
        />
        <el-table-column
          v-else
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
        >
          <template #default="scope">
            <slot :name="col.slot" :row="scope.row" :index="scope.$index"></slot>
          </template>
        </el-table-column>
      </template>

      <el-table-column v-if="$slots.action" label="操作" :width="actionWidth" fixed="right">
        <template #default="scope">
          <slot name="action" :row="scope.row" :index="scope.$index"></slot>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <el-pagination
      v-if="showPagination"
      class="pagination"
      :current-page="pagination.page"
      :page-size="pagination.pageSize"
      :page-sizes="pageSizes"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  border: {
    type: Boolean,
    default: true
  },
  stripe: {
    type: Boolean,
    default: true
  },
  selection: {
    type: Boolean,
    default: false
  },
  index: {
    type: Boolean,
    default: false
  },
  height: {
    type: [String, Number],
    default: undefined
  },
  maxHeight: {
    type: [String, Number],
    default: undefined
  },
  showSearch: {
    type: Boolean,
    default: true
  },
  searchFields: {
    type: Array,
    default: () => []
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  pagination: {
    type: Object,
    default: () => ({
      page: 1,
      pageSize: 10,
      total: 0
    })
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  actionWidth: {
    type: [String, Number],
    default: 200
  }
})

const emit = defineEmits([
  'search',
  'reset',
  'selection-change',
  'sort-change',
  'page-change',
  'size-change'
])

const tableRef = ref()
const searchForm = reactive({})

// Initialize search form
props.searchFields.forEach((field) => {
  searchForm[field.prop] = field.default || ''
})

const handleSearch = () => {
  emit('search', { ...searchForm })
}

const handleReset = () => {
  props.searchFields.forEach((field) => {
    searchForm[field.prop] = field.default || ''
  })
  emit('reset')
  emit('search', { ...searchForm })
}

const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

const handleSortChange = ({ prop, order }) => {
  emit('sort-change', { prop, order })
}

const handleSizeChange = (size) => {
  emit('size-change', size)
}

const handleCurrentChange = (page) => {
  emit('page-change', page)
}

// Expose methods
defineExpose({
  clearSelection: () => tableRef.value?.clearSelection()
})
</script>

<style lang="scss" scoped>
.data-table {
  .search-form {
    margin-bottom: 15px;
  }

  .toolbar {
    margin-bottom: 15px;
  }

  .pagination {
    margin-top: 15px;
    justify-content: flex-end;
  }
}
</style>
