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
        </el-input>
        <div v-show="pathData.length > 0" class="path-param">
          <el-table
            :data="pathData"
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
              <span>Headers <span class="param-count">({{ headerData.length + globalHeaderData.length }})</span></span>
            </span>
            <div>
              <h4>全局Headers</h4>
              <el-table
                :data="globalHeaderData"
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
            <div v-show="headerData.length > 0">
              <h4>接口Headers</h4>
              <el-table
                :data="headerData"
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
        <el-tabs v-model="requestActive" type="card" style="margin-top: 10px">
          <el-tab-pane label="Body" name="body">
            <span slot="label" class="result-header-label">
              <el-badge :is-dot="hasBody()" type="danger">
                <span>Body</span>
              </el-badge>
            </span>
            <el-radio-group v-model="postActive" size="small" style="margin-bottom: 20px;">
              <el-radio-button label="text" class="json-badge">Text</el-radio-button>
              <el-radio-button label="form">x-www-form-urlencoded <span class="param-count">({{ formData.length }})</span></el-radio-button>
              <el-radio-button label="multipart">multipart <span class="param-count">({{ multipartData.length }})</span></el-radio-button>
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
                  <el-input v-model="jsonBody" type="textarea" :autosize="{ minRows: 2, maxRows: 100}" />
                </el-form-item>
              </el-form>
            </div>
            <div v-show="showBody('form')">
              <el-table
                :data="formData"
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
                :data="multipartData"
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
              <span>Query <span class="param-count">({{ queryData.length }})</span></span>
            </span>
            <el-table
              :data="queryData"
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
      rightSpanSize: 0,
      currentItem: null,
      itemMap: null,
      currentMethod: 'GET',
      cellStyle: { paddingTop: '5px', paddingBottom: '5px' },
      requestUrl: '',
      requestGatewayUrl: '',
      // 网关请求时需要
      authorization: '',
      jsonBody: '',
      contentType: 'application/json;charset=UTF-8',
      requestActive: 'body',
      postActive: 'form',
      collapseActive: '',
      formData: [],
      multipartData: [],
      queryData: [],
      uploadFiles: [],
      fieldTypes: [
        { type: 'text', label: '文本' },
        { type: 'file', label: '文件' }
      ],
      globalHeaderData: [],
      headerData: [],
      pathData: [],
      resultActive: 'result',
      sendLoading: false,
      result: {
        bodyIsImage: false,
        headerData: [],
        content: '',
        status: 0
      }
    }
  },
  computed: {
    url: {
      get() {
        let url = this.requestUrl
        this.pathData.forEach(row => {
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
          return null;
        }
        this.pathData.forEach(row => {
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
        this.globalHeaderData = resp.data
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
      this.headerData = headerData
      this.queryData = queryData
      this.formData = formData
      this.multipartData = multipartData
      this.pathData = pathData
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
        this.jsonBody = JSON.stringify(jsonObj, null, 4)
      }
      this.requestActive = isQueryStringMethod ? 'query' : 'body'
      if (this.multipartData.length > 0) {
        this.postActive = 'multipart'
      } else {
        let active = ''
        if (hasBody) {
          active = 'text'
        } else if (this.formData.length > 0) {
          active = 'form'
        }
        this.postActive = active
        if (!this.postActive) {
          this.requestActive = 'query'
        }
      }
    },
    isQueryStringMethod(item) {
      return ['get', 'head'].indexOf(item.method.toLowerCase()) > -1
    },
    hasBody() {
      return this.jsonBody.length > 0 || this.formData.length > 0 || this.multipartData.length > 0
    },
    showBody(active) {
      return this.postActive === active
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
      let data = this.getParamObj(this.queryData)
      let isJson = false
      let isForm = false
      let isMultipart = false
      // 如果请求body
      switch (this.postActive) {
        case 'text':
          headers['Content-Type'] = this.contentType
          data = this.jsonBody
          if (this.contentType.indexOf('json') > -1) {
            isJson = true
          }
          break
        case 'form':
          isForm = true
          data = this.getParamObj(this.formData)
          break
        case 'multipart':
          isMultipart = this.multipartData.length > 0 || this.uploadFiles.length > 0
          data = this.getParamObj(this.multipartData)
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
      let data = this.getParamObj(this.queryData)
      let isJson = false
      let isForm = false
      let isMultipart = false
      // 如果请求body
      switch (this.postActive) {
        case 'text':
          headers['Content-Type'] = this.contentType
          data = this.jsonBody
          if (this.contentType.indexOf('json') > -1) {
            isJson = true
          }
          break
        case 'form':
          isForm = true
          data = this.getParamObj(this.formData)
          break
        case 'multipart':
          isMultipart = this.multipartData.length > 0 || this.uploadFiles.length > 0
          data = this.getParamObj(this.multipartData)
          break
        default:
      }
      this.sendLoading = true
      this.request(item.method, '/doc/proxy', data, headers, isJson, isForm, isMultipart, this.doProxyResponse)
    },
    buildRequestHeaders() {
      const headers = {}
      this.headerData.forEach(row => {
        headers[row.name] = row.example || ''
      })
      this.globalHeaderData.forEach(row => {
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
