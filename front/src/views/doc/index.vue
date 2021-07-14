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
        <h3>请求参数typescript代码</h3>
        <pre class="normal-text">{{ trans2TsInterfaceStr(item.requestParameters, false) }}</pre>
        <h3>返回参数typescript代码</h3>
        <pre class="normal-text">{{ trans2TsInterfaceStr(item.responseParameters, true) }}</pre>
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
        swaggerId: undefined,
        path: undefined,
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
    },
    trans2TsInterfaceStr(params, isResp) {
      return this.trans2TsInterface(null, params, isResp, [])
    },
    trans2TsInterface(fieldType, params, isResp, allElementTypes) {
      if (params == null) {
        return ''
      }
      // 有就不重复建
      if (fieldType != null && allElementTypes.indexOf(fieldType) >= 0) {
        return ''
      }
      let str = fieldType != null ? '\n\n' : ''
      str += 'interface '
      str += this.getInterfaceName(fieldType, isResp)
      str += ' {'
      const subElements = []
      allElementTypes.push(fieldType)
      params.forEach(param => {
        const line = this.trans2TsFieldStr(param, isResp)
        str += line
        if ((param.type === 'object' || param.type === 'array') && param.elementType != null) {
          subElements.push(param)
        }
      })
      str += '\n}'
      subElements.forEach(param => {
        str += this.trans2TsInterface(param.elementType, param.refs, isResp, allElementTypes)
      })
      return str
    },
    trans2TsFieldStr(param, isResp) {
      if (param.type == null || param.name == null) {
        return ''
      }
      let tsType = param.type
      if (tsType === 'integer') {
        tsType = 'number'
      }
      if (param.type === 'object' || param.type === 'array') {
        tsType = this.getInterfaceName(param.elementType, isResp)
      }
      return '\n  /** ' +
        (!isResp ? (param.required ? '必填 ' : '非必填 ') : '') +
        (param.description ? param.description : '') + ' */\n  ' +
        param.name + '?: ' + tsType + (param.type === 'array' ? '[]' : '')
    },
    getInterfaceName(fieldType, isResp) {
      if (fieldType == null) {
        return (isResp ? 'Resp' : 'Req')
      }
      const index = fieldType.indexOf('«')
      if (index >= 0) {
        fieldType = fieldType.substr(0, index)
      } else {
        fieldType = fieldType.replace('对象', '')
      }
      if (fieldType.endsWith('VO')) {
        fieldType = fieldType.substr(0, fieldType.length - 2)
      } else if (fieldType.endsWith('DTO')) {
        fieldType = fieldType.substr(0, fieldType.length - 3)
      }
      return fieldType + (isResp ? 'Resp' : 'Req')
    }
  }
}
</script>
