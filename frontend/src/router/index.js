import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/components/Layout/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/rule/list',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'rule/list',
        name: 'RuleList',
        component: () => import('@/views/rule/index.vue'),
        meta: { title: '规则列表' }
      },
      {
        path: 'rule/create',
        name: 'RuleCreate',
        component: () => import('@/views/rule/create.vue'),
        meta: { title: '创建规则' }
      },
      {
        path: 'rule/edit/:id',
        name: 'RuleEdit',
        component: () => import('@/views/rule/edit.vue'),
        meta: { title: '编辑规则' }
      },
      {
        path: 'rule/group',
        name: 'RuleGroup',
        component: () => import('@/views/rule/group.vue'),
        meta: { title: '规则组' }
      },
      {
        path: 'rule/version',
        name: 'RuleVersion',
        component: () => import('@/views/rule/version.vue'),
        meta: { title: '版本管理' }
      },
      {
        path: 'scene/list',
        name: 'SceneList',
        component: () => import('@/views/scene/index.vue'),
        meta: { title: '场景列表' }
      },
      {
        path: 'scene/metadata',
        name: 'SceneMetadata',
        component: () => import('@/views/scene/metadata.vue'),
        meta: { title: '元数据配置' }
      },
      {
        path: 'execution-log',
        name: 'ExecutionLog',
        component: () => import('@/views/log/index.vue'),
        meta: { title: '执行日志' }
      },
      {
        path: 'test',
        name: 'RuleTest',
        component: () => import('@/views/test/index.vue'),
        meta: { title: '规则测试' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 全局前置守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 规则引擎` : '规则引擎'

  // 获取token
  const token = localStorage.getItem('token')

  // 判断页面是否需要认证
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth !== false)

  if (requiresAuth && !token) {
    // 需要认证但未登录，跳转到登录页
    next({
      path: '/login',
      query: { redirect: to.fullPath } // 保存目标路由，登录后跳转
    })
  } else if (to.path === '/login' && token) {
    // 已登录用户访问登录页，重定向到首页
    next('/')
  } else {
    next()
  }
})

export default router
