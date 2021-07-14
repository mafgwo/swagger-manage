<template>
  <div class="navbar">
    <div class="project-select">
      <el-select v-model="swaggerId" placeholder="选择项目" size="medium" style="width: 250px;" @change="onProjectSelect">
        <div class="swagger-select-panel">
          <ul v-for="item in options" :key="item.id" class="el-select-group__wrap">
            <div v-if="item.groups.length === 1">
              <el-option
                v-for="group in item.groups"
                :key="group.id"
                :label="group.name"
                :value="group.id + ''"
              >
                <span style="float: left">{{ group.name }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  <el-link type="primary" icon="el-icon-edit" style="margin-right: 20px;" @click.stop="onProjectUpdate(item)" />
                  <el-link type="danger" icon="el-icon-delete" @click.stop="onProjectDelete(item)" />
                </span>
              </el-option>
            </div>
            <div v-else>
              <li class="el-select-group__title">
                <span style="float: left;margin-right: 20px;">{{ item.name || item.host }} </span>
                <span style="float: right; color: #8492a6; font-size: 13px;padding-right: 20px;">
                  <el-link type="primary" icon="el-icon-edit" style="margin-right: 20px;" @click.stop="onProjectUpdate(item)" />
                  <el-link type="danger" icon="el-icon-delete" @click.stop="onProjectDelete(item)" />
                </span>
              </li>
              <el-option
                v-for="group in item.groups"
                :key="group.id"
                :label="item.name ? `${item.name} - ${group.name}` : group.name"
                :value="group.id + ''"
              >
                <span>{{ group.name }}</span>
              </el-option>
            </div>
          </ul>
        </div>
      </el-select>
      <el-button type="text" @click="onProjectAdd">添加项目</el-button>
      <el-button type="text" @click="onImportJson">导入JSON</el-button>
      <div class="right-menu">
        <el-tooltip placement="left" content="同步远程文档">
          <el-button
            v-show="swaggerId"
            :loading="syncLoading"
            type="primary"
            icon="el-icon-refresh"
            size="mini"
            style="margin-right: 10px"
            @click="refreshDoc"
          />
        </el-tooltip>
      </div>
    </div>
    <el-dialog
      :title="projectTitle"
      :visible.sync="projectDlgShow"
    >
      <el-form
        ref="projectForm"
        :model="projectFormData"
        :rules="projectRule"
        size="mini"
        label-width="100px"
      >
        <el-form-item class="project-dialog-tip">
          <el-alert title="确保项目已启动并且配置了Swagger" :closable="false" />
        </el-form-item>
        <el-form-item label="项目地址" prop="host">
          <span slot="label">
            项目地址
            <el-popover
              placement="top"
              trigger="hover"
            >
              <p>
                <img src="@/assets/images/tip.png" style="width: 400px;">
              </p>
              <el-link slot="reference" :underline="false" class="el-icon-question" />
            </el-popover>
          </span>
          <el-input
            v-model="projectFormData.host"
            placeholder="到contextPath，如：http://localhost:8080，http://localhost:8081/cms"
            show-word-limit
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="网关地址">
          <el-input v-model="projectFormData.gatewayHost" placeholder="选填 网关地址如: http://app-api.dev.zyun.link" show-word-limit maxlength="100" />
        </el-form-item>
        <el-form-item label="网关规则">
          <el-input v-model="projectFormData.gatewayRule" placeholder="选填 网关替换规则如: /api(?<segment>.*), /auth$\{segment}" show-word-limit maxlength="100" />
        </el-form-item>
        <el-form-item label="项目名称" prop="host">
          <el-input v-model="projectFormData.name" placeholder="项目名称 如: 大电机3.0设备服务" show-word-limit maxlength="100" />
        </el-form-item>
        <el-form-item label="Basic认证">
          <el-col :span="12" style="padding-right: 10px;">
            <el-input v-model="projectFormData.basicAuthUsername" placeholder="选填，username" style="width: 100%;" />
          </el-col>
          <el-col :span="12">
            <el-input v-model="projectFormData.basicAuthPassword" placeholder="选填，password" style="width: 100%;" />
          </el-col>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="projectDlgShow = false">取 消</el-button>
        <el-button :loading="projectLoading" type="primary" @click="onProjectSave">保 存</el-button>
      </div>
    </el-dialog>
    <!-- 导入json -->
    <el-dialog
      :title="importJsonTitle"
      :visible.sync="importJsonDlgShow"
    >
      <el-form
        ref="importJsonForm"
        :model="importJsonFormData"
        :rules="importJsonRule"
        size="mini"
        label-width="100px"
      >
        <el-alert title="输入返回JSON的URL，确保能访问该URL" :closable="false" style="margin-bottom: 20px;" />
        <el-form-item label="URL" prop="host">
          <el-input
            v-model="importJsonFormData.host"
            placeholder="输入URL，如：http://localhost:8080/swagger/doc.json"
            show-word-limit
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="网关地址">
          <el-input v-model="importJsonFormData.gatewayHost" placeholder="选填 网关地址如: http://app-api.dev.zyun.link" show-word-limit maxlength="100" />
        </el-form-item>
        <el-form-item label="网关规则">
          <el-input v-model="importJsonFormData.gatewayRule" placeholder="选填 网关替换规则如: /api(?<segment>.*), /auth$\{segment}" show-word-limit maxlength="100" />
        </el-form-item>
        <el-form-item label="Basic认证">
          <el-col :span="12" style="padding-right: 10px;">
            <el-input v-model="importJsonFormData.basicAuthUsername" placeholder="选填，username" style="width: 100%;" />
          </el-col>
          <el-col :span="12">
            <el-input v-model="importJsonFormData.basicAuthPassword" placeholder="选填，password" style="width: 100%;" />
          </el-col>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="importJsonDlgShow = false">取 消</el-button>
        <el-button :loading="importJsonLoading" type="primary" @click="onImportJsonSave">导 入</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
export default {
  data() {
    return {
      swaggerId: '0',
      options: [],
      syncLoading: false,
      dbTypeConfig: [],
      // add project
      projectTitle: '添加项目',
      projectDlgShow: false,
      projectLoading: false,
      projectFormData: {
        id: 0,
        name: '',
        gatewayRule: '',
        gatewayHost: '',
        host: '',
        basicAuthUsername: '',
        basicAuthPassword: ''
      },
      projectRule: {
        host: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ]
      },
      importJsonTitle: '导入JSON',
      importJsonDlgShow: false,
      importJsonLoading: false,
      importJsonFormData: {
        id: 0,
        gatewayRule: '',
        gatewayHost: '',
        host: '',
        basicAuthUsername: '',
        basicAuthPassword: ''
      },
      importJsonRule: {
        host: [
          { required: true, message: '不能为空', trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value && !/^http(s)?:\/\/.+$/i.test(value)) {
              callback(new Error('URL格式不正确'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar'
    ]),
    stateSwaggerId() {
      return this.$store.state.settings.swaggerId
    }
  },
  watch: {
    stateSwaggerId(val) {
      this.swaggerId = val + ''
    }
  },
  created() {
    this.swaggerId = (this.getUrlSwaggerId() || this.getSwaggerId() || this.$store.state.settings.swaggerId) + ''
    this.loadProjectInfo()
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    loadProjectInfo() {
      this.get('/project/list/all', {}, resp => {
        this.options = resp.data
        if (this.options.length === 0) {
          this.onProjectAdd()
        }
      })
    },
    onProjectSelect(swaggerId) {
      this.swaggerId = swaggerId
      this.setSwaggerId(this.swaggerId)
      this.initDoc(swaggerId, () => {
        this.$router.push('/dashboard')
      })
    },
    onProjectAdd() {
      this.projectTitle = '添加项目'
      this.projectFormData.id = 0
      this.projectLoading = false
      this.projectDlgShow = true
    },
    onImportJson() {
      this.importJsonFormData.id = 0
      this.importJsonLoading = false
      this.importJsonDlgShow = true
    },
    onImportJsonSave() {
      this.$refs.importJsonForm.validate((valid) => {
        if (valid) {
          this.importJsonLoading = true
          this.formatHost(this.importJsonFormData)
          if (this.importJsonFormData.id) {
            this.post('/project/update', this.importJsonFormData, resp => {
              const swaggerInfoList = resp.data
              const swaggerId = swaggerInfoList[0].id
              this.syncDocAndReload(swaggerId)
            })
          } else {
            this.post('/project/importJson', this.importJsonFormData, () => {
              this.importJsonDlgShow = false
              this.tip('导入成功')
              this.loadProjectInfo()
            }, () => {
              this.importJsonLoading = false
            })
          }
        }
      })
    },
    onProjectUpdate(item) {
      this.projectTitle = '修改项目'
      if (item.isImport === 0) {
        Object.assign(this.projectFormData, item)
        this.projectDlgShow = true
        this.projectLoading = false
      } else {
        Object.assign(this.importJsonFormData, item)
        this.importJsonDlgShow = true
      }
    },
    onProjectDelete(item) {
      const arr = item.groups.filter(row => row.id === this.swaggerId)
      const name = item.name || item.host
      this.confirm(`确认要删除 ${name} 吗？`, function(done) {
        const isRemoveCache = this.options.length === 1 || arr.length > 0
        const data = {
          id: item.id
        }
        this.post('/project/delete', data, function() {
          // 如果删除最后一个，清除本地缓存
          if (isRemoveCache) {
            this.clearCache()
          }
          done()
          this.reloadHome()
        })
      })
    },
    refreshDoc() {
      this.syncLoading = true
      this.syncDoc(this.swaggerId, () => {
        this.syncLoading = false
        this.alert('同步成功', '提示', () => {
          this.reloadHome()
        })
      }, () => {
        this.syncLoading = false
      })
    },
    syncDocAndReload(swaggerId) {
      this.syncDoc(swaggerId, () => {
        this.reloadHome()
      })
    },
    reloadHome() {
      location.href = '/'
    },
    onProjectSave() {
      this.$refs.projectForm.validate((valid) => {
        if (valid) {
          this.projectLoading = true
          this.formatHost(this.projectFormData)
          if (this.projectFormData.id) {
            this.post('/project/update', this.projectFormData, resp => {
              const swaggerInfoList = resp.data
              const swaggerId = swaggerInfoList[0].id
              this.syncDocAndReload(swaggerId)
            }, () => {
              this.projectLoading = false
            })
          } else {
            this.post('/project/add', this.projectFormData, resp => {
              this.projectDlgShow = false
              this.tip('添加成功')
              this.loadProjectInfo()
            }, () => {
              this.projectLoading = false
            })
          }
        }
      })
    },
    formatHost(obj) {
      let host = obj.host
      if (host) {
        host = obj.host.trim()
        if (host.endsWith('/')) {
          host = host.substring(0, host.length - 1)
        }
        obj.host = host
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  .project-dialog-tip .el-form-item__content {line-height: 0;}
  .swagger-select-panel .el-select-group__wrap {list-style: initial;}
  .navbar-div {float: right;margin-top: 5px;margin-right: 10px;}
.user-head {
  cursor: pointer;
  margin-top: 6px;margin-right: 10px;
}
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  .project-select {
    padding: 5px;
  }

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 40px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-container {

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
