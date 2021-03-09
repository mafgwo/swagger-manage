import { initRoutes } from '@/router'
import Vue from 'vue'

const docPage = 'doc/index'
const dashboardPage = 'dashboard/index'
const settingPage = 'setting/index'
let name_id = 0

Object.assign(Vue.prototype, {
  initDoc(swaggerId, callback) {
    this.$store.state.settings.swaggerId = swaggerId
    this.setSwaggerId(swaggerId)
    this.get(`/doc/get/${swaggerId}`, {}, resp => {
      this.initMenu(resp.data, callback)
    })
  },
  syncDoc(swaggerId, callback, errorCallback) {
    this.get(`/doc/sync/${swaggerId}`, {}, resp => {
      const data = resp.data
      this.initDoc(data.id, callback)
    }, errorCallback)
  },
  initMenu(docInfo, callback) {
    const docModules = docInfo.docModules || []
    const menus = [{
      path: '/',
      name: 'Dashboard0',
      component: 'Layout',
      redirect: '/dashboard',
      children: [{
        path: 'dashboard',
        name: 'Dashboard',
        component: dashboardPage,
        meta: { title: '首页', component: dashboardPage, showOne: true, affix: true }
      }]
    }
    // , {
    //   path: `/${docInfo.swaggerId}/setting`,
    //   component: 'Layout',
    //   children: [{
    //     path: 'list',
    //     name: 'Setting',
    //     component: settingPage,
    //     meta: { title: '全局配置', component: settingPage, showOne: true }
    //   }]
    // }
    ]
    docModules.forEach((row, index) => {
      const parent = {
        path: `module_${docInfo.swaggerId}_${index}`,
        name: `Module_${docInfo.swaggerId}_${index}`,
        component: 'Layout',
        meta: { title: row.module, api: true }
      }
      const children = []
      const store = {}
      row.items.forEach(item => {
        item.swaggerId = docInfo.swaggerId
        const key = item.summary + item.path
        let values = store[key]
        if (!values) {
          values = []
          store[key] = values
        }
        values.push(item)
      })
      for (const key in store) {
        const values = store[key]
        const item = values[0]
        const child = {
          path: `/swagger/${item.swaggerId}/` + this.encodePathAndMethod(item.path, item.method),
          name: `Doc_${docInfo.swaggerId}_${name_id++}`,
          component: docPage,
          meta: { title: item.summary, description: item.description, swaggerId: item.swaggerId, path: item.path, method: item.method, module: item.module }
        }
        if (values.length > 1) {
          const child_children = []
          values.forEach(child_item => {
            child_children.push({
              path: `/swagger/${child_item.swaggerId}/` + this.encodePathAndMethod(child_item.path, child_item.method),
              name: `Doc_${docInfo.swaggerId}_${name_id++}`,
              component: docPage,
              meta: { title: child_item.method, parentTitle: item.summary, swaggerId: child_item.swaggerId, path: child_item.path, method: child_item.method }
            })
          })
          child.children = child_children
        }
        children.push(child)
      }
      parent.children = children
      menus.push(parent)
    })
    const routes = initRoutes(menus)
    this.$store.state.settings.routes = routes
    callback && callback.call(this, routes)
  }
})

