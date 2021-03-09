<template>
  <div class="app-container">
    <h4>全局Headers</h4>
    <el-button type="primary" size="mini" style="margin-bottom: 10px" @click="onHeaderAdd">添加Header</el-button>
    <el-table
      :data="settings.globalHeaders"
      border
      :header-cell-style="cellStyleSmall()"
      :cell-style="cellStyleSmall()"
    >
      <el-table-column label="Name" prop="configKey" width="300px" />
      <el-table-column label="Value" prop="configValue" />
      <el-table-column
        label="操作"
        width="150"
      >
        <template slot-scope="scope">
          <el-button type="text" size="mini" @click="onHeaderUpdate(scope.row)">修改</el-button>
          <el-button type="text" size="mini" @click="onHeaderDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <h4>多个Method重复，只显示</h4>
    <el-form ref="allowMethodsRef" :model="settings">
      <el-form-item prop="allowMethods" :rules="[{ type: 'array', required: true, message: '请至少选择一个', trigger: 'change' }]">
        <el-checkbox-group v-model="settings.allowMethods">
          <el-checkbox v-for="method in allMethods" :key="method" :label="method" />
        </el-checkbox-group>
      </el-form-item>
      <el-button type="primary" @click="onSaveAllowMethods">保存</el-button>
    </el-form>
    <h4>调试Host</h4>
    <el-alert title="调试页面请求Host，不填则用默认的" :closable="false" style="margin-bottom: 10px;" />
    <el-form ref="debugHostRef" :model="settings">
      <el-form-item prop="debugHost">
        <el-input v-model="settings.debugHost" placeholder="如：http://10.0.10.11:8080" />
      </el-form-item>
      <el-button type="primary" @click="onSaveDebugHost">保存</el-button>
    </el-form>
    <!--dialog-->
    <el-dialog
      :title="dialogHeaderTitle"
      :visible.sync="dialogHeaderVisible"
      :close-on-click-modal="false"
      @close="resetForm('dialogHeaderForm')"
    >
      <el-form
        ref="dialogHeaderForm"
        :rules="dialogFormRules"
        :model="dialogHeaderFormData"
        label-width="120px"
        size="mini"
      >
        <el-form-item
          prop="configKey"
          label="Name"
        >
          <el-input v-model="dialogHeaderFormData.configKey" placeholder="name" />
        </el-form-item>
        <el-form-item
          prop="configValue"
          label="Value"
        >
          <el-input v-model="dialogHeaderFormData.configValue" placeholder="value" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogHeaderVisible = false">取 消</el-button>
        <el-button type="primary" @click="onDialogHeaderSave">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'Setting',
  data() {
    return {
      settings: {
        globalHeaders: [],
        allowMethods: [],
        debugHost: ''
      },
      allMethods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS', 'HEAD'],
      dialogHeaderVisible: false,
      dialogHeaderTitle: '',
      dialogHeaderFormData: {
        id: 0,
        swaggerId: '',
        configKey: '',
        configValue: ''
      },
      dialogFormRules: {
        configKey: [
          { required: true, message: '不能为空', trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value && !/^[a-zA-Z0-9\-_]+$/.test(value)) {
              callback(new Error('格式错误，支持大小写英文、数字、-、下划线'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
        ], configValue: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadSettings()
  },
  methods: {
    loadSettings() {
      const swaggerId = this.getUrlSwaggerId() || this.getSwaggerId();
      this.get('/systemconfig/get', { swaggerId: swaggerId }, resp => {
        this.settings = resp.data
      })
    },
    loadHeaders() {
      this.loadGlobalHeaders(resp => {
        this.settings.globalHeaders = resp.data
      })
    },
    onHeaderAdd() {
      this.dialogHeaderTitle = '新增Header'
      this.dialogHeaderVisible = true
      this.dialogHeaderFormData.id = 0
    },
    onHeaderUpdate(row) {
      this.dialogHeaderTitle = '修改Header'
      this.dialogHeaderVisible = true
      this.$nextTick(() => {
        Object.assign(this.dialogHeaderFormData, row)
      })
    },
    onHeaderDelete(row) {
      this.confirm(`确认要删除该记录吗？`, function(done) {
        const data = {
          id: row.id
        }
        this.post('/systemconfig/globalHeader/delete', data, () => {
          done()
          this.tip('删除成功')
          this.loadHeaders()
        })
      })
    },
    onDialogHeaderSave() {
      this.$refs.dialogHeaderForm.validate((valid) => {
        if (valid) {
          const uri = this.dialogHeaderFormData.id ? '/systemconfig/globalHeader/update' : '/systemconfig/globalHeader/add'
          this.dialogHeaderFormData.swaggerId = this.getUrlSwaggerId() || this.getSwaggerId();
          this.post(uri, this.dialogHeaderFormData, () => {
            this.dialogHeaderVisible = false
            this.loadHeaders()
          })
        }
      })
    },
    onSaveAllowMethods() {
      this.$refs.allowMethodsRef.validate((valid) => {
        if (valid) {
          this.post('/systemconfig/allowMethod/set', { list: this.settings.allowMethods }, () => {
            this.alert('保存成功', '提示', () => {
              location.reload()
            })
          })
        }
      })
    },
    onSaveDebugHost() {
      this.$refs.debugHostRef.validate((valid) => {
        if (valid) {
          const data = {
            swaggerId: this.getUrlSwaggerId() || this.getSwaggerId(),
            debugHost: this.settings.debugHost
          }
          this.post('/systemconfig/debughost/set', data, () => {
            this.tipSuccess('保存成功')
          })
        }
      })
    }
  }
}
</script>
