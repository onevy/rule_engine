<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar-container">
      <Sidebar :collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="header-container">
        <Navbar @toggle-sidebar="toggleSidebar" :is-collapse="isCollapse" />
      </el-header>
      <el-main class="main-container">
        <Breadcrumb />
        <div class="page-content">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import Breadcrumb from './Breadcrumb.vue'

const isCollapse = ref(false)

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;

  .sidebar-container {
    background-color: #304156;
    transition: width 0.3s;
    overflow: hidden;
  }

  .header-container {
    padding: 0;
    height: 50px;
    line-height: 50px;
    background: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  }

  .main-container {
    background-color: #f0f2f5;
    padding: 20px;
    overflow-y: auto;

    .page-content {
      background: #fff;
      padding: 20px;
      border-radius: 4px;
      min-height: calc(100vh - 150px);
    }
  }
}
</style>
