<template>
  <div :class="classObj" class="app-wrapper">
    <div v-if="device==='mobile'&&sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <sidebar id="leftPanel" class="sidebar-container" />
    <div id="rightPanel" class="main-container">
      <div id="resizeBar"></div>
      <div :class="{'fixed-header':fixedHeader}">
        <navbar />
        <tags-view v-if="showTagsView" />
      </div>
      <app-main />
    </div>
  </div>
</template>

<script>
import { Navbar, Sidebar, AppMain, TagsView } from './components'
import ResizeMixin from './mixin/ResizeHandler'
import variables from '@/styles/variables.scss'

const LEFT_WIDTH_KEY = 'swagger-manage-menuwidth'
const RESIZE_BAR_MARGIN_LEFT = `${-parseInt(variables.resizeBarWidth) / 2}px`
const OPACITY_1 = '1'
const OPACITY_0 = '0'
const MIN_WIDTH = 50
const MAX_WIDTH = 800

let leftPanel, rightPanel, resizeBar

export default {
  name: 'Layout',
  components: {
    Navbar,
    Sidebar,
    AppMain,
    TagsView
  },
  mixins: [ResizeMixin],
  computed: {
    sidebar() {
      return this.$store.state.app.sidebar
    },
    device() {
      return this.$store.state.app.device
    },
    fixedHeader() {
      return this.$store.state.settings.fixedHeader
    },
    showTagsView() {
      return this.$store.state.settings.showTagsView
    },
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile'
      }
    }
  },
  mounted() {
    leftPanel = document.getElementById('leftPanel')
    rightPanel = document.getElementById('rightPanel')
    resizeBar = document.getElementById('resizeBar')
    this.initDragAside()
    this.initLeftWidth()
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    },
    initDragAside() {
      const that = this
      let start, end, move, width
      resizeBar.onmousedown = function(e) {
        start = e.clientX
        document.onmousemove = function(e) {
          resizeBar.style.opacity = OPACITY_1
          end = e.clientX
          move = end - start
          if (end > MIN_WIDTH && end < MAX_WIDTH) {
            width = end
            resizeBar.style.marginLeft = `${move}px`
          }
        }
        document.onmouseup = function() {
          that.setLeftWidth(`${width}px`)
          document.onmousemove = null
          document.onmouseup = null
          resizeBar.releaseCapture && resizeBar.releaseCapture()
        }
        resizeBar.setCapture && resizeBar.setCapture()
        return false
      }
    },
    initLeftWidth() {
      const width = this.getAttr(LEFT_WIDTH_KEY)
      this.setLeftWidth(width)
    },
    setLeftWidth(width) {
      width = width || variables.sideBarWidth
      leftPanel.style.width = width
      rightPanel.style.marginLeft = width
      resizeBar.style.opacity = OPACITY_0
      resizeBar.style.marginLeft = RESIZE_BAR_MARGIN_LEFT
      this.setAttr(LEFT_WIDTH_KEY, width)
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "~@/styles/mixin.scss";
  @import "~@/styles/variables.scss";

  .app-wrapper {
    @include clearfix;
    position: relative;
    height: 100%;
    width: 100%;
    &.mobile.openSidebar{
      position: fixed;
      top: 0;
    }
  }
  .drawer-bg {
    background: #000;
    opacity: 0.3;
    width: 100%;
    top: 0;
    height: 100%;
    position: absolute;
    z-index: 999;
  }

  .fixed-header {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 9;
    width: calc(100% - #{$sideBarWidth});
    transition: width 0.28s;
  }

  .hideSidebar .fixed-header {
    width: calc(100% - 54px)
  }

  .mobile .fixed-header {
    width: 100%;
  }
  #resizeBar{
    position: absolute;
    width: $resizeBarWidth;
    border-left: 2px dashed #b2b3b4;
    opacity: 0;
    height: 100%;
    cursor: ew-resize;
    float: left;
    z-index: 9999;
  }
</style>
