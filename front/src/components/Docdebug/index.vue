<template>
  <div class="doc-debug">
    <el-row :gutter="20">
      <el-col :span="24-rightSpanSize">
        <el-input v-model="url" class="request-url">
          <span slot="prepend">
            服务接口 {{ currentMethod }}
          </span>
          <el-button slot="append" :loading="sendLoading" style="width: 100px" @click="send"> 发 送 </el-button>
        </el-input>
        <el-input v-if="gatewayUrl" v-model="gatewayUrl" class="request-url" style="margin-top: 5px;">
          <span slot="prepend">
            网关接口 {{ currentMethod }}
          </span>
          <el-button slot="append" :loading="sendLoading" style="width: 100px" @click="sendGateway"> 发 送 </el-button>
        </el-input>
        <el-input v-if="gatewayUrl" v-model="authorization" placeholder="网关token,用户登录成功后的token" class="request-url" style="margin-top: 5px;">
          <span slot="prepend">
            网关token
          </span>
          <span v-if="currentItem && currentItem.method === 'GET'" slot="append">
            自动签名：
            <el-radio-group v-model="signFlag">
              <el-radio :label="1">是</el-radio>
              <el-radio :label="0">否</el-radio>
            </el-radio-group>
          </span>
        </el-input>
        <div v-show="requestInfo.pathData.length > 0" class="path-param">
          <el-table
            :data="requestInfo.pathData"
            border
            :header-cell-style="cellStyle"
            :cell-style="cellStyle"
          >
            <el-table-column
              prop="name"
              label="Path参数"
              width="300"
            />
            <el-table-column label="参数值">
              <template slot-scope="scope">
                <el-form :model="scope.row" size="mini">
                  <el-form-item label-width="0">
                    <div v-if="scope.row.type === 'enum'">
                      <el-select v-model="scope.row.example">
                        <el-option v-for="val in scope.row.enums" :key="val" :value="val" :label="val" />
                      </el-select>
                    </div>
                    <div v-else>
                      <el-input v-model="scope.row.example" />
                    </div>
                  </el-form-item>
                </el-form>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-collapse v-model="collapseActive" accordion style="margin-top: 10px;">
          <el-collapse-item title="Headers" name="header">
            <span slot="title" class="result-header-label">
              <span>Headers <span class="param-count">({{ requestInfo.headerData.length + requestInfo.globalHeaderData.length }})</span></span>
            </span>
            <div>
              <h4>全局Headers</h4>
              <el-table
                :data="requestInfo.globalHeaderData"
                border
                :header-cell-style="cellStyleSmall()"
                :cell-style="cellStyleSmall()"
              >
                <el-table-column label="Name" prop="configKey" width="300px" />
                <el-table-column label="Value" prop="configValue">
                  <template slot-scope="scope">
                    <el-form :model="scope.row" size="mini">
                      <el-form-item label-width="0">
                        <el-input v-model="scope.row.configValue" />
                      </el-form-item>
                    </el-form>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-show="requestInfo.headerData.length > 0">
              <h4>接口Headers</h4>
              <el-table
                :data="requestInfo.headerData"
                border
                :header-cell-style="cellStyle"
                :cell-style="cellStyle"
              >
                <el-table-column label="Name" prop="name" width="300px" />
                <el-table-column label="Value">
                  <template slot-scope="scope">
                    <el-form :model="scope.row" size="mini">
                      <el-form-item label-width="0">
                        <el-input v-model="scope.row.example" />
                      </el-form-item>
                    </el-form>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-collapse-item>
        </el-collapse>
        <el-tabs v-model="requestInfo.requestActive" type="card" style="margin-top: 10px">
          <el-tab-pane label="Body" name="body">
            <span slot="label" class="result-header-label">
              <el-badge :is-dot="hasBody()" type="danger">
                <span>Body</span>
              </el-badge>
            </span>
            <el-radio-group v-model="requestInfo.postActive" size="small" style="margin-bottom: 20px;">
              <el-radio-button label="text" class="json-badge">Text</el-radio-button>
              <el-radio-button label="form">x-www-form-urlencoded <span class="param-count">({{ requestInfo.formData.length }})</span></el-radio-button>
              <el-radio-button label="multipart">multipart <span class="param-count">({{ requestInfo.multipartData.length }})</span></el-radio-button>
            </el-radio-group>
            <div v-show="showBody('text')">
              <el-radio-group v-model="contentType" style="margin-bottom: 10px;">
                <el-radio label="application/json;charset=UTF-8">JSON</el-radio>
                <el-radio label="text/plain">Text</el-radio>
                <el-radio label="application/xml">XML</el-radio>
                <el-radio label="text/html">HTML</el-radio>
                <el-radio label="application/x-javascript">JavaScript</el-radio>
              </el-radio-group>
              <el-form>
                <el-form-item label-width="0">
                  <el-input v-model="requestInfo.jsonBody" type="textarea" :autosize="{ minRows: 2, maxRows: 100}" />
                </el-form-item>
              </el-form>
            </div>
            <div v-show="showBody('form')">
              <el-table
                :data="requestInfo.formData"
                border
                :header-cell-style="cellStyle"
                :cell-style="cellStyle"
              >
                <el-table-column
                  prop="name"
                  label="参数名"
                  width="300"
                />
                <el-table-column label="参数值">
                  <template slot-scope="scope">
                    <el-form :model="scope.row" size="mini">
                      <el-form-item label-width="0">
                        <div v-if="scope.row.type === 'enum'">
                          <el-select v-model="scope.row.example">
                            <el-option v-for="val in scope.row.enums" :key="val" :value="val" :label="val" />
                          </el-select>
                        </div>
                        <div v-else>
                          <el-input v-model="scope.row.example" />
                        </div>
                      </el-form-item>
                    </el-form>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-show="showBody('multipart')">
              <el-upload
                action=""
                :multiple="true"
                :auto-upload="false"
                style="width: 500px;margin-bottom: 10px"
                :on-remove="(file, fileList) => onSelectMultiFile(file, fileList)"
                :on-change="(file, fileList) => onSelectMultiFile(file, fileList)"
              >
                <el-button slot="trigger" type="primary" size="mini">上传多个文件</el-button>
              </el-upload>
              <el-table
                v-show="showBody('multipart')"
                :data="requestInfo.multipartData"
                border
                :header-cell-style="cellStyle"
                :cell-style="cellStyle"
              >
                <el-table-column
                  prop="name"
                  label="参数名"
                  width="300"
                />
                <el-table-column label="参数值">
                  <template slot-scope="scope">
                    <el-form :model="scope.row" size="mini">
                      <el-form-item label-width="0" style="margin-bottom: 0">
                        <el-upload
                          v-if="scope.row.type === 'file' || scope.row.elementType === 'file'"
                          action=""
                          :multiple="true"
                          :auto-upload="false"
                          :on-change="(file, fileList) => onSelectFile(file, fileList, scope.row)"
                          :on-remove="(file, fileList) => onSelectFile(file, fileList, scope.row)"
                        >
                          <el-button slot="trigger" class="choose-file" type="primary">选择文件</el-button>
                        </el-upload>
                        <div v-else-if="scope.row.type === 'enum'">
                          <el-select v-model="scope.row.example">
                            <el-option v-for="val in scope.row.enums" :key="val" :value="val" :label="val" />
                          </el-select>
                        </div>
                        <div v-else>
                          <el-input v-model="scope.row.example" />
                        </div>
                      </el-form-item>
                    </el-form>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          <el-tab-pane label="Query" name="query">
            <span slot="label" class="result-header-label">
              <span>Query <span class="param-count">({{ requestInfo.queryData.length }})</span></span>
            </span>
            <el-table
              :data="requestInfo.queryData"
              border
              :header-cell-style="cellStyle"
              :cell-style="cellStyle"
            >
              <el-table-column
                prop="name"
                label="参数名"
                width="300"
              />
              <el-table-column label="参数值">
                <template slot-scope="scope">
                  <el-form :model="scope.row" size="mini">
                    <el-form-item label-width="0">
                      <div v-if="scope.row.type === 'enum'">
                        <el-select v-model="scope.row.example">
                          <el-option v-for="val in scope.row.enums" :key="val" :value="val" :label="val" />
                        </el-select>
                      </div>
                      <div v-else>
                        <el-input v-model="scope.row.example" />
                      </div>
                    </el-form-item>
                  </el-form>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
        <div style="margin-top: 10px;">
          <el-button type="primary" size="small" @click="saveCase">保存用例</el-button>
          <div style="margin-top: 10px;">
            <el-tag
              v-for="(caseItem, index) in cases"
              :key="index"
              closable
              style="margin-right: 10px;"
              @close="handleCloseCase(caseItem)"
            >
              <a @click="useCase(caseItem)">{{ caseItem.name }}</a>
            </el-tag>
          </div>
        </div>
      </el-col>
      <el-col :span="rightSpanSize" style="border-left: 1px #E4E7ED solid;">
        <div class="result-status">
          Status: <el-tag :type="result.status >= 200 && result.status < 300 ? 'success' : 'danger'">{{ result.status }}</el-tag>
        </div>
        <el-tabs v-model="resultActive" type="card">
          <el-tab-pane label="返回结果" name="body">
            <el-image v-if="result.bodyIsImage" :src="result.content" :preview-src-list="[result.content]" :autosize="{ minRows: 2, maxRows: 200}" />
            <el-input v-if="!result.bodyIsImage" v-model="result.content" type="textarea" :readonly="true" :autosize="{ minRows: 2, maxRows: 200}" />
          </el-tab-pane>
          <el-tab-pane label="Headers" name="headers">
            <span slot="label" class="result-header-label">
              <span>Headers <span class="param-count">({{ result.headerData.length }})</span></span>
            </span>
            <el-table
              :data="result.headerData"
              :header-cell-style="cellStyle"
              :cell-style="cellStyle"
            >
              <el-table-column label="Name" prop="name" />
              <el-table-column label="Value" prop="value" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>
<style>
  .cell .choose-file {padding: 5px;}
  .doc-debug .cell .el-form-item {margin-bottom: 0;}
  .result-header-label {font-size: 14px;}
  .result-header-label .el-badge__content.is-fixed {top: 10px;right: 0; }
  .json-badge .el-badge__content.is-fixed {top: 0;right: -3px; }
  .param-count {color: #909399;}
  .el-radio-group .el-badge {vertical-align: baseline;}
  .el-radio-group .is-active .param-count {color: #fff;}
  .result-status {margin-bottom: 12px; font-size: 13px;color: #606266;}
  .path-param {margin-top: 5px;}
</style>
<script>

import md5 from 'js-md5'
require('fast-text-encoding')
const xmlFormatter = require('xml-formatter')
export default {
  name: 'Docdebug',
  props: {
    item: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      signFlag: 0,
      rightSpanSize: 0,
      currentItem: null,
      itemMap: null,
      currentMethod: 'GET',
      cellStyle: { paddingTop: '5px', paddingBottom: '5px' },
      requestUrl: '',
      requestGatewayUrl: '',
      // 网关请求时需要
      authorization: '',
      contentType: 'application/json;charset=UTF-8',
      collapseActive: '',
      requestInfo: {
        requestActive: 'body',
        postActive: 'form',
        jsonBody: '',
        formData: [],
        multipartData: [],
        queryData: [],
        headerData: [],
        globalHeaderData: [],
        pathData: []
      },
      uploadFiles: [],
      fieldTypes: [
        { type: 'text', label: '文本' },
        { type: 'file', label: '文件' }
      ],
      resultActive: 'result',
      sendLoading: false,
      result: {
        bodyIsImage: false,
        headerData: [],
        content: '',
        status: 0
      },
      // 用例
      cases: []
    }
  },
  computed: {
    url: {
      get() {
        let url = this.requestUrl
        this.requestInfo.pathData.forEach(row => {
          url = url.replace(new RegExp(`{${row.name}}`), row.example || `{${row.name}}`)
        })
        return url
      },
      set(val) {
        this.requestUrl = val
      }
    },
    gatewayUrl: {
      get() {
        let url = this.requestGatewayUrl
        if (!url) {
          return null
        }
        this.requestInfo.pathData.forEach(row => {
          url = url.replace(new RegExp(`{${row.name}}`), row.example || `{${row.name}}`)
        })
        return url
      },
      set(val) {
        this.requestGatewayUrl = val
      }
    }
  },
  watch: {
    item(newVal) {
      this.loadItem(newVal)
    }
  },
  methods: {
    loadItem(item) {
      this.currentItem = item
      this.currentMethod = item.method
      this.requestUrl = this.bindUrl(item)
      this.requestGatewayUrl = item.gatewayUrl
      this.bindRequestParam(item)
      this.loadGlobalHeaders(resp => {
        this.requestInfo.globalHeaderData = resp.data
      })
      this.loadCases(item)
    },
    loadCases(item) {
      if (!item.swaggerId) {
        return
      }
      this.get('/doc-cases', { swaggerId: item.swaggerId, path: item.path, method: item.method }, (resp) => {
        this.cases = resp.data
      })
    },
    useCase(caseObj) {
      const requestInfo = JSON.parse(caseObj.content)
      this.authorization = requestInfo.authorization
      this.signFlag = requestInfo.signFlag || 0
      this.requestInfo.requestActive = requestInfo.requestActive
      this.requestInfo.postActive = requestInfo.postActive
      this.requestInfo.jsonBody = requestInfo.jsonBody
      this.replaceFieldValue(this.requestInfo.formData, 'name', 'example', requestInfo.formData)
      this.replaceFieldValue(this.requestInfo.multipartData, 'name', 'example', requestInfo.multipartData)
      this.replaceFieldValue(this.requestInfo.queryData, 'name', 'example', requestInfo.queryData)
      this.replaceFieldValue(this.requestInfo.headerData, 'name', 'example', requestInfo.headerData)
      this.replaceFieldValue(this.requestInfo.globalHeaderData, 'configKey', 'configValue', requestInfo.globalHeaderData)
      this.replaceFieldValue(this.requestInfo.pathData, 'name', 'example', requestInfo.pathData)
      this.$message({
        type: 'info',
        message: '已使用用例' + caseObj.name
      })
    },
    replaceFieldValue(items, keyProp, valueProp, replaceMap) {
      if (!replaceMap) {
        return
      }
      if (!items) {
        return
      }
      items.forEach(item => {
        item[valueProp] = replaceMap[item[keyProp]]
      })
    },
    // 构造所有的参数与值
    getParamMap() {
      return {
        authorization: this.authorization,
        signFlag: this.signFlag,
        requestActive: this.requestInfo.requestActive,
        postActive: this.requestInfo.postActive,
        jsonBody: this.requestInfo.jsonBody,
        formData: this.paramToMap(this.requestInfo.formData, 'name', 'example'),
        multipartData: this.paramToMap(this.requestInfo.multipartData, 'name', 'example'),
        queryData: this.paramToMap(this.requestInfo.queryData, 'name', 'example'),
        headerData: this.paramToMap(this.requestInfo.headerData, 'name', 'example'),
        globalHeaderData: this.paramToMap(this.requestInfo.globalHeaderData, 'configKey', 'configValue'),
        pathData: this.paramToMap(this.requestInfo.pathData, 'name', 'example')
      }
    },
    paramToMap(arr, keyProp, valueProp) {
      const obj = {}
      arr.forEach(item => {
        const key = item[keyProp]
        obj[key] = item[valueProp]
      })
      return obj
    },
    handleCloseCase(caseItem) {
      // 删除二次确认
      this.$confirm('确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        this.request('DELETE', '/doc-cases/' + caseItem.id, {}, {}, false, false, false, () => {
          this.cases.splice(this.cases.indexOf(caseItem), 1)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消保存用例'
        })
      })
    },
    saveCase() {
      this.$prompt('请输入用例名称', '保存用例', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputErrorMessage: '用例名必须填写'
      }).then(({ value }) => {
        if (!value) {
          this.$message({
            type: 'error',
            message: '用例名必须填写'
          })
          return
        }
        this.post('/doc-cases', {
          name: value,
          swaggerId: this.currentItem.swaggerId,
          path: this.currentItem.path,
          method: this.currentItem.method,
          content: JSON.stringify(this.getParamMap())
        }, (resp) => {
          this.cases.unshift(resp.data)
          this.$message({
            type: 'success',
            message: '保存用例成功'
          })
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消保存用例'
        })
      })
    },
    bindUrl(item) {
      return item.requestUrl + item.path
    },
    bindRequestParam(item) {
      const queryData = []
      const formData = []
      const multipartData = []
      const headerData = []
      const pathData = []
      const isQueryStringMethod = this.isQueryStringMethod(item)
      const requestParameters = item.requestParameters
      // 是否是上传文件请求
      const uploadRequest = item.uploadRequest
      requestParameters.forEach(row => {
        const propIn = row.in
        if (propIn === 'path') {
          pathData.push(row)
        } else if (propIn === 'header') {
          headerData.push(row)
        } else {
          if (isQueryStringMethod) {
            queryData.push(row)
          } else {
            if (uploadRequest || propIn === 'formData') {
              multipartData.push(row)
            } else {
              formData.push(row)
            }
          }
        }
      })
      this.requestInfo.headerData = headerData
      this.requestInfo.queryData = queryData
      this.requestInfo.formData = formData
      this.requestInfo.multipartData = multipartData
      this.requestInfo.pathData = pathData
      const bodyParams = requestParameters.filter(row => row.in === 'body')
      const hasBody = bodyParams.length > 0
      if (hasBody) {
        let jsonObj = ''
        if (bodyParams.length === 1 && bodyParams[0].name == null && bodyParams[0].type.startsWith('array(')) {
          const bodyItem = bodyParams[0].type.indexOf('string') > -1 ? 'test1' : 0
          jsonObj = [bodyItem]
        } else {
          const arrayBody = bodyParams.filter(row => row.parentType === 'array').length === bodyParams.length
          jsonObj = this.createResponseExample(bodyParams)
          if (arrayBody) {
            jsonObj = [jsonObj]
          }
        }
        this.requestInfo.jsonBody = JSON.stringify(jsonObj, null, 4)
      }
      this.requestInfo.requestActive = isQueryStringMethod ? 'query' : 'body'
      if (this.requestInfo.multipartData.length > 0) {
        this.requestInfo.postActive = 'multipart'
      } else {
        let active = ''
        if (hasBody) {
          active = 'text'
        } else if (this.requestInfo.formData.length > 0) {
          active = 'form'
        }
        this.requestInfo.postActive = active
        if (!this.requestInfo.postActive) {
          this.requestInfo.requestActive = 'query'
        }
      }
    },
    isQueryStringMethod(item) {
      return ['get', 'head'].indexOf(item.method.toLowerCase()) > -1
    },
    hasBody() {
      return this.requestInfo.jsonBody.length > 0 || this.requestInfo.formData.length > 0 || this.requestInfo.multipartData.length > 0
    },
    showBody(active) {
      return this.requestInfo.postActive === active
    },
    onSelectFile(f, fileList, row) {
      const files = []
      fileList.forEach(file => {
        const rawFile = file.raw
        files.push(rawFile)
      })
      row.__file__ = { name: row.name, files: files }
    },
    onSelectMultiFile(file, fileList) {
      const files = []
      fileList.forEach(file => {
        const rawFile = file.raw
        files.push(rawFile)
      })
      this.uploadFiles = files
    },
    send() {
      const item = this.currentItem
      const headers = this.buildRequestHeaders()
      let data = this.getParamObj(this.requestInfo.queryData)
      let isJson = false
      let isForm = false
      let isMultipart = false
      // 如果请求body
      switch (this.requestInfo.postActive) {
        case 'text':
          headers['Content-Type'] = this.contentType
          data = this.requestInfo.jsonBody
          if (this.contentType.indexOf('json') > -1) {
            isJson = true
          }
          break
        case 'form':
          isForm = true
          data = this.getParamObj(this.requestInfo.formData)
          break
        case 'multipart':
          isMultipart = this.requestInfo.multipartData.length > 0 || this.uploadFiles.length > 0
          data = this.getParamObj(this.requestInfo.multipartData)
          break
        default:
      }
      this.sendLoading = true
      this.request(item.method, '/doc/proxy', data, headers, isJson, isForm, isMultipart, this.doProxyResponse)
    },
    sendGateway() {
      const item = this.currentItem
      const headers = this.buildRequestHeaders()
      headers['target-url'] = this.gatewayUrl
      if (this.authorization) {
        headers['Authorization'] = 'Bearer ' + this.authorization
      }
      let data = this.getParamObj(this.requestInfo.queryData)
      let isJson = false
      let isForm = false
      let isMultipart = false
      // 如果请求body
      switch (this.requestInfo.postActive) {
        case 'text':
          headers['Content-Type'] = this.contentType
          data = this.requestInfo.jsonBody
          if (this.contentType.indexOf('json') > -1) {
            isJson = true
          }
          break
        case 'form':
          isForm = true
          data = this.getParamObj(this.requestInfo.formData)
          break
        case 'multipart':
          isMultipart = this.requestInfo.multipartData.length > 0 || this.uploadFiles.length > 0
          data = this.getParamObj(this.requestInfo.multipartData)
          break
        default:
      }
      this.sendLoading = true
      // 如果需要自动签名并且当前method是GET则自动签名
      if (item.method === 'GET' && this.signFlag && this.gatewayUrl) {
        const time = new Date().getTime()
        let uri = this.gatewayUrl.split('//')[1]
        uri = uri.substr(uri.indexOf('/'))
        const paramStr = this.queryStr()
        const signStr = `${uri}${this.authorization}${time}${paramStr}`
        const sign = md5(signStr)
        headers['x-zg-time'] = time
        headers['x-zg-sign'] = sign
      }
      this.request(item.method, '/doc/proxy', data, headers, isJson, isForm, isMultipart, this.doProxyResponse)
    },
    queryStr() {
      return this.requestInfo.queryData.filter(it => it.example).sort((n1, n2) => n1.name > n2.name ? 1 : -1).map(it => `${it.name}=${it.example}`).join('&')
    },
    buildRequestHeaders() {
      const headers = {}
      this.requestInfo.headerData.forEach(row => {
        headers[row.name] = row.example || ''
      })
      this.requestInfo.globalHeaderData.forEach(row => {
        headers[row.configKey] = row.configValue
      })
      headers['target-url'] = this.url
      return headers
    },
    getParamObj(array) {
      const data = {}
      array.forEach(row => {
        // 处理文件上传
        const fileConfig = row.__file__
        if (fileConfig) {
          const fileConfigs = this.getFileConfigs(data)
          fileConfigs.push(fileConfig)
        } else if (row.example !== null && row.example !== '') {
          data[row.name] = row.example
        }
      })
      // 全局上传
      if (this.uploadFiles.length > 0) {
        const fileConfigs = this.getFileConfigs(data)
        fileConfigs.push({ name: 'file', files: this.uploadFiles })
      }
      return data
    },
    getFileConfigs(data) {
      let fileConfigs = data['__files__']
      if (!fileConfigs) {
        fileConfigs = []
        data['__files__'] = fileConfigs
      }
      return fileConfigs
    },
    doProxyResponse(error, response) {
      this.sendLoading = false
      if (error) {
        this.$message.error(error)
        return
      }
      this.buildResultHeaders(response)
      this.buildResultStatus(error, response)
      this.buildResultContent(error, response)
    },
    buildResultStatus(error, response) {
      if (!error) {
        const statusCode = response.headers['target-statuscode']
        this.result.status = statusCode ? parseInt(statusCode) : 0
      } else {
        this.result.status = 0
      }
    },
    buildResultContent(error, response) {
      const headers = response.targetHeaders
      const contentType = headers['content-type'] || ''
      const contentDisposition = headers['content-disposition'] || ''
      const produces = this.currentItem.produces && this.currentItem.produces.join('')
      // 如果是下载文件
      this.openRightPanel()
      if (contentType.indexOf('stream') > -1 ||
        produces.indexOf('stream') > -1 ||
        contentDisposition.indexOf('attachment') > -1
      ) {
        const disposition = headers['content-disposition']
        const filename = this.getDispositionFilename(disposition)
        this.downloadFile(filename, response.raw)
      } else {
        let content = ''
        if (error) {
          content = error.message
        } else {
          // axios返回data部分
          const data = response.data
          if (data) {
            try {
              content = this.formatResponse(contentType, data)
            } catch (e) {
              console.error('格式转换错误', e)
              content = response.data
            }
          } else {
            // needle返回部分
            const uint8Array = response.raw
            if (uint8Array && uint8Array.length > 0) {
              if (contentType && contentType.toLowerCase().indexOf('image') > -1) {
                this.result.bodyIsImage = true
                content = (
                  'data:' + contentType.toLowerCase() + ';base64,' +
                  btoa(uint8Array.reduce((data, byte) => data + String.fromCharCode(byte), ''))
                )
              } else {
                this.result.bodyIsImage = false
                const resp = new TextDecoder().decode(uint8Array)
                try {
                  content = this.formatResponse(contentType, resp)
                } catch (e) {
                  console.error('格式转换错误', e)
                  content = resp
                }
              }
            }
          }
        }
        this.result.content = content
      }
    },
    formatResponse(contentType, stringBody) {
      if (this.isObject(stringBody)) {
        return this.formatJson(stringBody)
      }
      if (!contentType) {
        return stringBody
      }
      const contentTypeLower = contentType.toLowerCase()
      if (contentTypeLower.indexOf('json') > -1) {
        return this.formatJson(JSON.parse(stringBody))
      } else if (contentTypeLower.indexOf('xml') > -1) {
        return xmlFormatter(stringBody)
      } else {
        return stringBody
      }
    },
    formatJson(json) {
      return JSON.stringify(json, null, 4)
    },
    downloadFile(filename, buffer) {
      const url = window.URL.createObjectURL(new Blob([buffer]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
    },
    getDispositionFilename(disposition) {
      const dispositionArr = disposition.split(';')
      for (let i = 0; i < dispositionArr.length; i++) {
        const item = dispositionArr[i].trim()
        // filename="xx"
        if (item.toLowerCase().startsWith('filename')) {
          const result = item.match(new RegExp('filename="(.*?)"', 'i'))
          return result ? result[1] : ''
        }
      }
    },
    buildResultHeaders(response) {
      const headers = response.headers
      const targetHeadersString = headers['target-response-headers'] || JSON.stringify(headers)
      const targetHeaders = JSON.parse(targetHeadersString)
      response.targetHeaders = targetHeaders
      const headersData = []
      if (targetHeaders) {
        for (const key in targetHeaders) {
          headersData.push({ name: key, value: targetHeaders[key] })
        }
      }
      this.result.headerData = headersData
    },
    openRightPanel() {
      this.resultActive = 'body'
      this.rightSpanSize = 10
    }
  }
}
</script>
