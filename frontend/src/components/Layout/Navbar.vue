<template>
  <div class="navbar">
    <div class="left-menu">
      <el-icon class="hamburger" @click="toggleSidebar">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
    </div>

    <div class="right-menu">
      <el-tooltip content="刷新" placement="bottom">
        <el-icon class="icon-btn" @click="handleRefresh">
          <Refresh />
        </el-icon>
      </el-tooltip>

      <el-tooltip content="全屏" placement="bottom">
        <el-icon class="icon-btn" @click="toggleFullscreen">
          <FullScreen />
        </el-icon>
      </el-tooltip>

      <el-dropdown trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="30" src="" />
          <span class="user-name">{{ username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>
              <div style="text-align: center; color: #909399; font-size: 12px">
                {{ username }}
              </div>
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Fold, Expand, Refresh, FullScreen, ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle-sidebar'])
const router = useRouter()
const userStore = useUserStore()

// 获取用户名
const username = computed(() => userStore.username || '管理员')

const toggleSidebar = () => {
  emit('toggle-sidebar')
}

const handleRefresh = () => {
  window.location.reload()
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用store的logout方法清除认证信息
    userStore.logout()

    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;

  .left-menu {
    display: flex;
    align-items: center;

    .hamburger {
      font-size: 20px;
      cursor: pointer;

      &:hover {
        color: #409eff;
      }
    }
  }

  .right-menu {
    display: flex;
    align-items: center;

    .icon-btn {
      font-size: 18px;
      margin-right: 15px;
      cursor: pointer;

      &:hover {
        color: #409eff;
      }
    }

    .avatar-wrapper {
      display: flex;
      align-items: center;
      cursor: pointer;

      .user-name {
        margin: 0 5px 0 10px;
        font-size: 14px;
      }
    }
  }
}
</style>
