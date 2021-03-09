<template>
  <section class="app-main">
    <transition name="fade-transform" mode="out-in">
      <div v-if="$route.name">
        <keep-alive>
          <router-view :key="key" />
        </keep-alive>
      </div>
      <div v-else>
        <router-view :key="key" />
      </div>
    </transition>
  </section>
</template>

<script>
export default {
  name: 'AppMain',
  computed: {
    key() {
      return this.$route.fullPath
    }
  }
}
</script>

<style scoped>
.app-main {
  /*50 = navbar  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
}
.fixed-header+.app-main {
  padding-top: 50px;
}
</style>
