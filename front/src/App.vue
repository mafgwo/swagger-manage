<template>
  <div id="app">
    <el-backtop />
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
  created() {
    this.loadDoc()
    console.log('swaggerId:' + this.getUrlSwaggerId())
  }
}
</script>
