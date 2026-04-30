import { createRouter, createWebHashHistory } from 'vue-router'

import { useAuthStore } from '../stores/auth'
import AuthView from '../views/AuthView.vue'
import DashboardView from '../views/DashboardView.vue'
import DecisionListPage from '../views/DecisionListPage.vue'
import DecisionDetailPage from '../views/DecisionDetailPage.vue'
import DecisionFormPage from '../views/DecisionFormPage.vue'
import TimelinePage from '../views/TimelinePage.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/decisions'
    },
    {
      path: '/login',
      name: 'login',
      component: AuthView,
      meta: { guestOnly: true }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true }
    },
    {
      path: '/decisions',
      name: 'decisions',
      component: DecisionListPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/decisions/new',
      name: 'decision-create',
      component: DecisionFormPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/decisions/:id',
      name: 'decision-detail',
      component: DecisionDetailPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/timeline',
      name: 'timeline',
      component: TimelinePage,
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (authStore.token && !authStore.user) {
    try {
      await authStore.fetchProfile()
    } catch (error) {
      authStore.logout()
    }
  }

  if (to.meta.requiresAuth && !authStore.token) {
    return '/login'
  }

  if (to.meta.guestOnly && authStore.token) {
    return '/decisions'
  }

  return true
})

export default router
