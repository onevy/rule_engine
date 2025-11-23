<template>
  <el-breadcrumb separator="/" class="breadcrumb-container">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
      <span v-if="index === breadcrumbs.length - 1" class="no-link">{{ item.title }}</span>
      <router-link v-else :to="item.path">{{ item.title }}</router-link>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const routeMap = {
  '/dashboard': '工作台',
  '/rule/list': '规则列表',
  '/rule/group': '规则组',
  '/rule/create': '创建规则',
  '/rule/edit': '编辑规则',
  '/scene/list': '场景列表',
  '/scene/metadata': '元数据配置',
  '/execution-log': '执行日志',
  '/test': '规则测试'
}

const breadcrumbs = computed(() => {
  const _matched = route.matched.filter((item) => item.meta && item.meta.title)
  const crumbs = []

  // Add home
  crumbs.push({
    path: '/dashboard',
    title: '首页'
  })

  // Get current route title
  const currentTitle = routeMap[route.path] || route.meta?.title || '未知页面'

  if (route.path !== '/dashboard') {
    // Add parent menu if exists
    if (route.path.startsWith('/rule/')) {
      crumbs.push({
        path: '/rule/list',
        title: '规则管理'
      })
    } else if (route.path.startsWith('/scene/')) {
      crumbs.push({
        path: '/scene/list',
        title: '场景管理'
      })
    }

    crumbs.push({
      path: route.path,
      title: currentTitle
    })
  }

  return crumbs
})
</script>

<style lang="scss" scoped>
.breadcrumb-container {
  margin-bottom: 15px;

  .no-link {
    color: #97a8be;
    cursor: text;
  }
}
</style>
