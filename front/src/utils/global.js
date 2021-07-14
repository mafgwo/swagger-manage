/*
注册全局方法
 */
import Vue from 'vue'
import needle from 'needle'
import axios from 'axios'

global.docInfo = ''
global.itemsMap = false

// 创建axios实例
const client = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // api 的 base_url
  timeout: 60 * 1000 // 请求超时时间，毫秒
})

const Base64 = require('js-base64').Base64

const baseURL = process.env.VUE_APP_BASE_API || `${location.protocol}//${location.host}`
const swaggerIdKey = 'swagger-admin-swaggerid'

const TYPE_VALUE = {
  boolean: true,
  float: 0.0,
  double: 0.0,
  byte: 0,
  short: 0,
  integer: 0,
  number: 0,
  long: 0,
  string: '',
  char: ''
}

Object.assign(Vue.prototype, {
  /**
   * GET请求接口
   * @param uri uri
   * @param data 请求数据
   * @param callback 成功时回调
   * @param errorCallback 失败时回调
   */
  get: function(uri, data, callback, errorCallback) {
    const that = this
    needle.request('GET', baseURL + uri, data, {
      // 设置header
      headers: {}
    }, (error, response) => {
      if (error) {
        console.log(error)
      }
      that.doResponse(error, response, callback, errorCallback)
    })
  },
  /**
   * 请求接口
   * @param uri uri
   * @param data 请求数据
   * @param callback 成功时回调
   * @param errorCallback 失败时回调
   */
  post: function(uri, data, callback, errorCallback) {
    const that = this
    needle.request('POST', baseURL + uri, data, {
      // 指定这一句即可
      json: true
    }, (error, response) => {
      if (error) {
        console.log(error)
      }
      that.doResponse(error, response, callback, errorCallback)
    })
  },
  request(method, uri, data, headers, isJson, isForm, isMultipart, callback) {
    // 如果是文件上传，使用axios，needle上传文件不完美
    if (isMultipart) {
      this.doMultipart(uri, data, headers, callback)
      return
    }
    const that = this
    if (isForm) {
      headers['Content-Type'] = 'application/x-www-form-urlencoded'
    }
    needle.request(method, baseURL + uri, data, {
      // 设置header
      headers: headers,
      multipart: isMultipart,
      json: isJson,
      timeout: 120000
    }, (error, response) => {
      callback.call(that, error, response)
    })
  },
  doMultipart(uri, data, headers, callback) {
    const that = this
    const formData = new FormData()
    for (const name in data) {
      if (name === '__files__') {
        const fileConfigs = data[name]
        fileConfigs.forEach(fileConfig => {
          fileConfig.files.forEach(file => {
            formData.append(fileConfig.name, file)
          })
        })
      } else {
        formData.append(name, data[name])
      }
    }
    client.post(uri, formData, {
      headers: headers
    }).then(function(response) {
      callback.call(that, null, response)
    }).catch(function(error) {
      callback.call(that, error, null)
    })
  },
  doResponse(error, response, callback, errorCallback) {
    // 成功
    if (!error && response.statusCode === 200) {
      const resp = response.body
      const code = resp.code
      if (code === '0') { // 成功
        callback && callback.call(this, resp)
      } else {
        this.$message.error(resp.msg || '请求异常，请查看日志')
        errorCallback && errorCallback.call(this, resp)
      }
    } else {
      this.$message.error('请求异常，请查看日志')
    }
  },
  loadDoc() {
    const swaggerId = this.getUrlSwaggerId() || this.getSwaggerId()
    if (!swaggerId) {
      this.get('/swagger/get/first', {}, resp => {
        const data = resp.data
        data && this.initDoc(data.id)
      })
    } else {
      this.initDoc(swaggerId)
    }
  },
  setSwaggerId(id) {
    localStorage.setItem(swaggerIdKey, id + '')
  },
  getSwaggerId() {
    return parseInt(localStorage.getItem(swaggerIdKey) || 0)
  },
  clearCache() {
    this.removeSwaggerId()
  },
  removeSwaggerId() {
    localStorage.removeItem(swaggerIdKey)
  },
  createResponseExample: function(params) {
    const responseJson = {}
    for (let i = 0; i < params.length; i++) {
      const row = params[i]
      if (row.in === 'header') {
        continue
      }
      let val
      // 如果有子节点
      if (row.refs && row.refs.length > 0) {
        const childrenValue = this.createResponseExample(row.refs)
        // 如果是数组
        if (row.type === 'array') {
          val = [childrenValue]
        } else {
          val = childrenValue
        }
      } else {
        // 单值
        val = row.example || this.getDefaultValue(row)
      }
      responseJson[row.name] = val
    }
    const isOneArray = Object.keys(responseJson).length === 1 && this.isArray(Object.values(responseJson)[0])
    if (isOneArray) {
      return Object.values(responseJson)[0]
    }
    return responseJson
  },
  getDefaultValue(row) {
    const type = row.type
    if (type === 'enum') {
      return row.enums[0]
    } else if (type === 'object') {
      return {}
    } else if (type === 'array') {
      return []
    }
    const val = TYPE_VALUE[type]
    return val === undefined ? '' : val
  },
  loadGlobalHeaders(callback) {
    const swaggerId = this.getSwaggerId()
    this.get('/systemconfig/globalHeader/list', { swaggerId: swaggerId }, resp => {
      callback.call(this, resp)
    })
  },
  /**
   * tip，使用方式：this.tip('操作成功')，this.tip('错误', 'error')
   * @param msg 内容
   * @param type success / info / warning / error
   */
  tip: function(msg, type) {
    this.$message({
      message: msg,
      type: type || 'success'
    })
  },
  tipSuccess: function(msg) {
    this.tip(msg, 'success')
  },
  tipError: function(msg) {
    this.tip(msg, 'error')
  },
  tipInfo: function(msg) {
    this.tip(msg, 'info')
  },
  /**
   * 提醒框
   * @param msg 消息
   * @param okHandler 成功回调
   * @param cancelHandler
   */
  confirm: function(msg, okHandler, cancelHandler) {
    const that = this
    this.$confirm(msg, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      beforeClose: (action, instance, done) => {
        if (action === 'confirm') {
          okHandler.call(that, done)
        } else if (action === 'cancel') {
          if (cancelHandler) {
            cancelHandler.call(that, done)
          } else {
            done()
          }
        } else {
          done()
        }
      }
    }).catch(function() {})
  },
  /**
   * 提示框
   * <pre>
   * this.alert('注册成功', '提示', function() {
      this.goRoute(`/login`)
     })
   * </pre>
   * @param msg
   * @param title
   * @param callback
   */
  alert: function(msg, title, callback) {
    const that = this
    this.$alert(msg, title || '提示', {
      confirmButtonText: '确定',
      callback: action => {
        callback && callback.call(that, action)
      }
    })
  },
  /**
   * 重置表单
   * @param formName 表单元素的ref
   */
  resetForm(formName) {
    const frm = this.$refs[formName]
    frm && frm.resetFields()
  },
  logout: function() {
  },
  goRoute: function(path) {
    this.$router.push({ path: path })
  },
  /**
   * array转tree，必须要有id,parentId属性
   * @param arr 数组
   * @param parentId 父节点id，第一次调用传0
   * @returns {Array} 返回树array
   */
  convertTree: function(arr, parentId) {
    if (!arr) {
      return []
    }
    // arr是返回的数据parentId父id
    const temp = []
    const treeArr = arr
    treeArr.forEach((item, index) => {
      if (item.parentId === parentId) {
        // 递归调用此函数
        treeArr[index].children = this.convertTree(treeArr, treeArr[index].id)
        temp.push(treeArr[index])
      }
    })
    return temp
  },
  setAttr: function(key, val) {
    localStorage.setItem(key, val)
  },
  getAttr: function(key) {
    return localStorage.getItem(key)
  },
  cellStyleSmall: function() {
    return { padding: '5px 0' }
  },
  headCellStyleSmall: function() {
    return { padding: '5px 0' }
  },
  parseJSON: function(str, callback, errorCallback) {
    let isJson = false
    if (typeof str === 'string') {
      try {
        const obj = JSON.parse(str)
        isJson = (typeof obj === 'object') && obj
        if (isJson) {
          callback.call(this, obj)
        }
      } catch (e) {
        isJson = false
      }
    }
    if (!isJson) {
      errorCallback.call(this)
    }
  },
  isObject: function(obj) {
    return Object.prototype.toString.call(obj) === '[object Object]'
  },
  isArray: function(obj) {
    return Object.prototype.toString.call(obj) === '[object Array]'
  },
  encodeEmail: function(email) {
    if (email && email.indexOf('@') > -1) {
      let ret = ''
      const arr = email.split('@')
      const account = arr[0]
      if (account.length <= 3) {
        ret = `${account.substring(0, 1)}***@${arr[1]}`
      } else {
        ret = `${account.substring(0, 3)}***@${arr[1]}`
      }
      return ret
    } else {
      return ''
    }
  },
  formatterMoney: function(row, column, cellValue, index) {
    return formatMoney(cellValue)
  },
  formatMoney: function(cellValue) {
    return formatMoney(cellValue)
  },
  formatDate: function(time) {
    const y = time.getFullYear()
    const m = time.getMonth() + 1
    const d = time.getDate()
    const h = time.getHours()
    const mm = time.getMinutes()
    const s = time.getSeconds()
    return `${y}-${this._add0(m)}-${this._add0(d)} ${this._add0(h)}:${this._add0(mm)}:${this._add0(s)}`
  },
  _add0: function(m) {
    return m < 10 ? '0' + m : m
  },
  encodePathAndMethod: function(path, method) {
    return Base64.encode(path) + '_' + method
  },
  decodePathAndMethod: function() {
    const pathAndMethod = this.getPathAndMethodLast()
    const pathAndMethodArr = pathAndMethod.split('_')
    if (pathAndMethodArr.length !== 2) {
      return {}
    }
    return {
      path: Base64.decode(pathAndMethodArr[0]),
      method: pathAndMethodArr[1]
    }
  },
  getPathAndMethodLast: function() {
    const url = window.location.href
    const startIndex = url.lastIndexOf('/')
    return url.substring(startIndex + 1, url.length)
  },
  getUrlSwaggerId: function() {
    const url = window.location.href
    if (url.indexOf('/#/swagger/') <= -1) {
      return 0
    }
    return parseInt(url.split('/#/swagger/')[1].split('/')[0])
  }
})

const formatMoney = function(cellValue) {
  return '￥' + (cellValue / 100).toFixed(2)
}
