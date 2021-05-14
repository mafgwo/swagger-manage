<template>
  <div :class="{'has-logo':showLogo}">
    <logo v-if="showLogo" :title="swaggerIdNameMap[$store.state.settings.swaggerId]" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <div v-show="routeData.length > 0" style="padding: 5px">
        <el-input
          v-model="filterText"
          :clearable="true"
          prefix-icon="el-icon-search"
          size="mini"
          placeholder="搜索，支持路径、名称、描述"
        />
      </div>
      <el-menu
        class="menu-wrapper"
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :default-openeds="defaultOpeneds"
        :unique-opened="false"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        mode="vertical"
      >
        <div v-for="(item) in routeData" v-show="!item.hidden" :key="item.path">
          <!-- 只有一个子节点 -->
          <div v-if="hasOneChild(item.children)">
            <router-link :to="resolvePath(item, item.children[0].path)">
              <el-menu-item :index="resolvePath(item, item.children[0].path)">
                <span slot="title">{{ item.children[0].meta.title }}</span>
              </el-menu-item>
            </router-link>
          </div>
          <div v-else>
            <el-submenu :index="item.path">
              <template v-show="!item.hidden" slot="title">
                <i :class="item.icon"></i>
                <span slot="title">{{ item.meta && item.meta.title }}</span>
              </template>
              <div v-for="(child) in item.children" v-show="!child.hidden" :key="child.path">
                <div v-if="!child.children">
                  <router-link :to="child.path">
                    <el-menu-item :index="child.path">
                      {{ child.meta.title }}
                    </el-menu-item>
                  </router-link>
                </div>
                <div v-else class="nest-menu">
                  <el-submenu :index="child.path">
                    <template slot="title">{{ child.meta.title }}</template>
                    <router-link v-for="methodItem in child.children" :key="methodItem.id" :to="methodItem.path">
                      <el-menu-item :index="methodItem.path">
                        {{ methodItem.meta.title }}
                      </el-menu-item>
                    </router-link>
                  </el-submenu>
                </div>
              </div>
            </el-submenu>
          </div>
        </div>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import variables from '@/styles/variables.scss'
import path from 'path'
import { isExternal } from '@/utils/validate'
import FixiOSBug from './FixiOSBug'

export default {
  components: { Logo },
  mixins: [FixiOSBug],
  data() {
    return {
      swaggerIdNameMap: {
      },
      routes: '',
      filterText: '',
      colorMap: {
        'GET': '',
        'POST': '#53A053',
        'PUT': '#CA9846',
        'PATCH': '#CA9846',
        'DELETE': '#E25A50',
        'OPTIONS': '#1485BA',
        'HEAD': '#1485BA'
      },
      typeMap: {
        'GET': '',
        'POST': 'success',
        'PUT': 'info',
        'PATCH': 'info',
        'DELETE': 'danger',
        'OPTIONS': 'warning',
        'HEAD': 'warning'
      }
    }
  },
  computed: {
    ...mapGetters([
      'sidebar'
    ]),
    stateSwaggerId() {
      return this.$store.state.settings.swaggerId
    },
    routeData() {
      return this.$store.state.settings.routes
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    /**
     * 默认展开
     * @returns {[]}
     */
    defaultOpeneds() {
      // const indexArr = []
      // this.routes.forEach(route => {
      //   if (route.children && route.children.length > 0) {
      //     indexArr.push(route.path)
      //   }
      // })
      // return indexArr
      return []
    },
    showLogo() {
      return true
    },
    variables() {
      return variables
    },
    isCollapse() {
      return false
    }
  },
  watch: {
    filterText(searchText) {
      this.routeData.forEach(parent => {
        if (parent.meta && parent.meta.api) {
          const children = parent.children || []
          let findChildCount = 0
          for (let i = 0; i < children.length; i++) {
            const child = children[i]
            const meta = child.meta
            const find = (
              this.isMatch(meta.gatewayUrl, searchText) ||
              this.isMatch(meta.path, searchText) ||
              this.isMatch(meta.title, searchText) ||
              this.isMatch(meta.description, searchText) ||
              this.isMatch(meta.summary, searchText) ||
              this.isMatch(meta.module, searchText)
            )
            if (find) {
              findChildCount++
              child.hidden = false
            } else {
              child.hidden = true
            }
          }
          // 子节点都没找到，父节点也隐藏
          parent.hidden = findChildCount === 0
        }
      })
    },
    stateSwaggerId(val) {
      this.loadDocName(val)
    }
  },
  created() {
    this.loadDoc()
    const swaggerId = this.getUrlSwaggerId() || this.getSwaggerId()
    this.loadDocName(swaggerId)
  },
  methods: {
    isMatch(target, searchText) {
      if (searchText.length === 0) {
        return true
      }
      return target && target.toLowerCase().indexOf(searchText.toLowerCase()) > -1
    },
    loadDocName(val) {
      let swaggerName = this.swaggerIdNameMap[val]
      if (!swaggerName) {
        let uri = '/swagger/get/first';
        if (val) {
          uri = '/swagger/' + val;
        }
        this.get(uri, {}, resp => {
          const data = resp.data
          data && (this.swaggerInfo = data)
          data && (this.swaggerIdNameMap[data.id] = data.name)
        })
      }
    },
    getColor(method) {
      return this.colorMap[method] || ''
    },
    getType(method) {
      return this.typeMap[method] || ''
    },
    getMenuItemStyle(method) {
      const color = this.getColor(method);
      return color ? {color: this.getColor(method)} : {}
    },
    hasOneChild(children) {
      if (children && children.length === 1 && children[0].meta.showOne) {
        return true
      }
    },
    hasOneShowingChild(children = [], parent) {
      const showingChildren = children.filter(item => {
        if (item.hidden) {
          return false
        } else {
          // Temp set(will be used if only has one showing child)
          this.onlyOneChild = item
          return true
        }
      })

      // 首页
      if (showingChildren.length === 1 && showingChildren[0].meta.showOne) {
        return true
      }

      // When there is only one child router, the child router is displayed by default
      if (showingChildren.length === 1) {
        return false
      }

      // Show parent if there are no child router to display
      if (showingChildren.length === 0) {
        this.onlyOneChild = { ... parent, path: '', noShowingChildren: true }
        return true
      }

      return false
    },
    resolvePath(item, routePath) {
      if (isExternal(routePath)) {
        return routePath
      }
      const basePath = item.path
      if (isExternal(basePath)) {
        return basePath
      }
      return path.resolve(basePath, routePath)
    },
    resolveSubmenuPath(routePath) {
      return routePath
    }
  }
}
</script>
