<template>
  <el-table
    :data="data"
    border
    row-key="id"
    default-expand-all
    :tree-props="{ children: 'refs', hasChildren: 'hasChildren' }"
    :cell-style="cellStyleSmall()"
    :header-cell-style="headCellStyleSmall()"
    empty-text="无参数"
  >
    <el-table-column
      prop="name"
      label="名称"
      width="250"
    >
      <template slot-scope="scope">
        <span v-if="scope.row.name">{{ scope.row.name }}</span>
        <span v-if="!scope.row.name">-</span>
      </template>
    </el-table-column>
    <el-table-column
      v-if="showIn"
      prop="in"
      label="参数类型"
      width="100"
    />
    <el-table-column
      prop="type"
      label="数据类型"
      width="120"
    >
      <template slot-scope="scope">
        <span>{{ scope.row.type }}</span>
        <span v-show="scope.row.type === 'array' && scope.row.elementType">
          <el-tooltip effect="dark" :content="`元素类型：${scope.row.elementType}`" placement="top">
            <i class="el-icon-info" />
          </el-tooltip>
        </span>
        <span v-show="scope.row.type === 'object' && scope.row.elementType">
          <el-tooltip effect="dark" :content="`对象类型：${scope.row.elementType}`" placement="top">
            <i class="el-icon-info" />
          </el-tooltip>
        </span>
      </template>
    </el-table-column>
    <el-table-column
      prop="required"
      label="必须"
      width="60"
    >
      <template slot-scope="scope">
        <span :class="scope.row.required ? 'danger' : ''">{{ scope.row.required ? '是' : '否' }}</span>
      </template>
    </el-table-column>
    <el-table-column
      prop="description"
      label="描述"
    />
    <el-table-column
      prop="example"
      label="示例值"
    >
      <template slot-scope="scope">
        <div v-if="scope.row.type === 'enum'">
          {{ (scope.row.enums || []).join('、') }}
        </div>
        <div v-else>
          {{ scope.row.example }}
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
export default {
  name: 'ParameterTable',
  props: {
    data: {
      type: Array,
      default: () => []
    },
    tree: {
      type: Boolean,
      default: true
    },
    // 展示in字段
    showIn: {
      type: Boolean,
      default: false
    }
  }
}
</script>
