<template>
  <div class="app-container">
    <el-tabs v-model="active" type="card">
      <el-tab-pane name="info">
        <span slot="label"><i class="el-icon-document" /> 接口信息</span>
        <h2 v-if="item.summary && item.summary.length > 0">{{ item.summary }}</h2>
        <div>
          服务接口PATH：
          <el-input v-model="item.path" :readonly="true" size="mini" style="width: 700px;">
            <template slot="prepend">{{ item.method }}</template>
          </el-input>
        </div>
        <div v-if="item.gatewayUrl" style="margin-top: 5px;">
          网关接口地址：
          <el-input v-model="item.gatewayUrl" :readonly="true" size="mini" style="width: 700px;">
            <template slot="prepend">{{ item.method }}</template>
          </el-input>
        </div>
        <div class="consumes-produces">
          <span class="label">consumes:</span><span class="text">{{ (item.consumes || []).join(', ') }}</span>
        </div>
        <div class="consumes-produces">
          <span class="label">produces:</span><span class="text">{{ (item.produces || []).join(', ') }}</span>
        </div>
        <h3>接口描述</h3>
        <div class="api-description">{{ item.description }}</div>
        <div v-show="formatHeaders(item.requestParameters).length > 0">
          <h3>请求Headers</h3>
          <parameter-table :data="formatHeaders(item.requestParameters)" />
        </div>
        <h3>请求参数</h3>
        <parameter-table :data="formatParameters(item.requestParameters)" :show-in="true" />
        <h3>返回参数</h3>
        <parameter-table :data="formatParameters(item.responseParameters)" />
        <h3>返回示例</h3>
        <pre class="normal-text">{{ JSON.stringify(createResponseExample(item.responseParameters), null, 4) }}</pre>
      </el-tab-pane>
      <el-tab-pane name="debug">
        <span slot="label"><i class="el-icon-s-promotion" /> 调试接口</span>
        <docdebug :item="item" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<style>
  .doc-container {padding: 10px;}
  .api-description {color: #606266;font-size: 13px;}
  .consumes-produces {margin-top: 10px;}
  .consumes-produces span {margin-right: 10px;}
  .consumes-produces .label {}
  .consumes-produces .text {color: #606266;}

</style>
<script>
import Docdebug from '@/components/Docdebug'
import ParameterTable from '@/components/ParameterTable'

export default {
  components: { Docdebug, ParameterTable },
  data() {
    return {
      active: 'info',
      item: {
        method: '',
        consumes: [],
        produces: [],
        requestParameters: [],
        responseParameters: []
      },
      requestUrl: '',
      currentMethod: ''
    }
  },
  computed: {
    swaggerId: {
      get() {
        return this.getUrlSwaggerId() || this.getSwaggerId()
      },
      set(val) {
        this.setSwaggerId(val)
      }
    }
  },
  created() {
    const swaggerId = this.swaggerId
    const pathAndMethod = this.decodePathAndMethod()
    if (swaggerId && pathAndMethod.path && pathAndMethod.method) {
      const paramData = {
        path: pathAndMethod.path,
        method: pathAndMethod.method
      }
      this.get(`/doc/${this.swaggerId}/item`, paramData, (resp) => {
        this.item = resp.data
      })
    } else {
      this.tipError('未找到文档信息')
    }
  },
  methods: {
    formatParameters(params) {
      return params.filter(row => {
        return row.in !== 'header'
      })
    },
    formatHeaders(params) {
      return params.filter(row => {
        return row.in === 'header'
      })
    }
  }
}
</script>
