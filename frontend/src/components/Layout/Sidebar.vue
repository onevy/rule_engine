<template>
  <div class="sidebar">
    <div class="logo">
      <img src="@/assets/images/logo.png" alt="Logo" v-if="!collapse" />
      <span v-if="!collapse">规则引擎</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="collapse"
      :unique-opened="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>工作台</template>
      </el-menu-item>

      <el-sub-menu index="rule-management">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>规则管理</span>
        </template>
        <el-menu-item index="/rule/list">规则列表</el-menu-item>
        <el-menu-item index="/rule/group">规则组</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="scene-management">
        <template #title>
          <el-icon><Grid /></el-icon>
          <span>场景管理</span>
        </template>
        <el-menu-item index="/scene/list">场景列表</el-menu-item>
        <el-menu-item index="/scene/metadata">元数据配置</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/execution-log">
        <el-icon><Tickets /></el-icon>
        <template #title>执行日志</template>
      </el-menu-item>

      <el-menu-item index="/test">
        <el-icon><VideoPlay /></el-icon>
        <template #title>规则测试</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Odometer, Document, Grid, Tickets, VideoPlay } from '@element-plus/icons-vue'

defineProps({
  collapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()

const activeMenu = computed(() => {
  return route.path
})
</script>

<style lang="scss" scoped>
.sidebar {
  height: 100%;

  .logo {
    height: 50px;
    line-height: 50px;
    text-align: center;
    background-color: #2b2f3a;
    color: #fff;
    font-size: 16px;
    font-weight: bold;
    overflow: hidden;

    img {
      width: 32px;
      height: 32px;
      vertical-align: middle;
      margin-right: 8px;
    }
  }

  .el-menu {
    border-right: none;
    height: calc(100% - 50px);
  }
}
</style>
