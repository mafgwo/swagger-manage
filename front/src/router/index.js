import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'
const _import = require('@/router/_import_' + process.env.NODE_ENV)

/**
 * Note: sub-menus only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menus
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menus
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/',
    name: 'Dashboard0',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: { title: '首页', component: 'dashboard/index', showOne: true, affix: true }
    }]
  },
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index')
      }
    ]
  },
  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  }
]

const createRouter = () => {
  const routes = getConstantRoutes()
  return new Router({
    // mode: 'history', // require service support
    scrollBehavior: () => ({ y: 0 }),
    routes: routes
  })
}

function getConstantRoutes() {
  return constantRoutes
}

const router = createRouter()
const allRouterNames = ['Dashboard0']

export function saveCurrentRoute(to) {
  const createRoute = (obj, parent) => {
    const meta = obj.meta
    const component = !obj.parent ? 'Layout' : meta.component
    const route = {
      name: obj.name,
      path: obj.path,
      component: component,
      meta: {
        title: meta.title,
        id: meta.id,
        parentTitle: meta.parentTitle,
        component: component
      }
    }
    if (parent) {
      parent.children = [route]
    }
    return route
  }
  const matched = to.matched
  const menus = []
  let route = null
  for (let i = 0; i < matched.length; i++) {
    route = createRoute(matched[i], route)
    menus.push(route)
  }
}

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export function initRoutes(menus) {
  // 重置路由
  // resetRouter()
  const routes = buildRouters(menus)
  // 动态添加路由 - 未添加过则动态添加路由
  const filterRouters = routes.filter(item => allRouterNames.indexOf(item.name) <= -1)
  router.addRoutes(filterRouters)
  for (const item of filterRouters) {
    allRouterNames.push(item.name)
  }
  return routes
}

export function buildRouters(menus) {
  return menus.filter(route => {
    if (route.component) {
      // Layout组件特殊处理
      if (route.component) {
        if (route.component === 'Layout') {
          route.component = Layout
        } else {
          route.component = _import(route.component)
        }
      }
    }
    if (route.children && route.children.length) {
      route.children = buildRouters(route.children)
    }
    return true
  })
}

export default router
