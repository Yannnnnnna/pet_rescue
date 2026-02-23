<template>
  <div class="user-list-container">
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-grid">
          <div class="filter-item">
            <label class="filter-label">关键字</label>
            <el-input v-model="queryParams.keyword" placeholder="用户名/昵称/手机号" clearable @keyup.enter="handleSearch" class="filter-input" />
          </div>
          <div class="filter-item">
            <label class="filter-label">角色</label>
            <el-select v-model="queryParams.role" placeholder="全部角色" clearable class="filter-input">
              <el-option label="普通用户" :value="0" />
              <el-option label="管理员" :value="1" />
            </el-select>
          </div>
        </div>
        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch" class="btn-search">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset" class="btn-reset">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        class="data-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="scope">
            <div class="avatar-wrapper">
              <el-avatar :src="scope.row.avatar" :size="48" shape="square">
                <template #error>
                  <el-icon><UserFilled /></el-icon>
                </template>
              </el-avatar>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" align="center">
          <template #default="scope">
            <span class="user-name">{{ scope.row.username || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" align="center">
          <template #default="scope">
            <span>{{ scope.row.nickname || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120" align="center">
          <template #default="scope">
            <span :class="['role-tag', scope.row.role === 1 ? 'admin' : 'user']">
              {{ scope.row.role === 1 ? '管理员' : '普通用户' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" align="center">
          <template #default="scope">
            <span>{{ scope.row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="积分" width="100" align="center" sortable>
          <template #default="scope">
            <span class="score-value">{{ scope.row.score || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" align="center" width="180">
          <template #default="scope">
            <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button type="danger" link size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <span class="total-text">共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Delete, UserFilled } from '@element-plus/icons-vue'
import { getUserList, removeUser } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

// 数据
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  role: undefined
})

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    if (res.code === 200) {
      userList.value = res.data.records
      total.value = Number(res.data.total)
    } else {
        ElMessage.error(res.msg || '获取用户列表失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.role = undefined
  handleSearch()
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username || row.nickname}" 吗？此操作不可恢复。`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await removeUser(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          getList()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

// 格式化时间
const formatDate = (time) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.user-list-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-card {
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
  justify-content: space-between;
}

.filter-grid {
  display: flex;
  gap: 16px;
  flex: 1;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  white-space: nowrap;
}

.filter-input {
  width: 200px;
}

.filter-input :deep(.el-input__wrapper) {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.2s ease;
}

.filter-input :deep(.el-input__wrapper:focus-within) {
  border-color: #10b981;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.1);
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.btn-search {
  background: #3b82f6;
  border-color: #3b82f6;
  border-radius: 8px;
}

.btn-search:hover {
  background: #2563eb;
  border-color: #2563eb;
}

.btn-reset {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #64748b;
}

.btn-reset:hover {
  background: #f8fafc;
  color: #334155;
}

.table-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
  overflow: hidden;
}

.data-table {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #64748b;
  --el-table-row-hover-bg-color: #f8fafc;
}

.data-table :deep(.el-table__header th) {
  font-weight: 600;
  font-size: 13px;
}

.data-table :deep(.el-table__body td) {
  font-size: 14px;
  color: #334155;
}

.avatar-wrapper {
  display: flex;
  justify-content: center;
}

.avatar-wrapper :deep(.el-avatar) {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.user-name {
  font-weight: 500;
  color: #1e293b;
}

.role-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid;
}

.role-tag.admin {
  background: #fef2f2;
  color: #ef4444;
  border-color: #fecaca;
}

.role-tag.user {
  background: #ecfdf5;
  color: #10b981;
  border-color: #a7f3d0;
}

.score-value {
  font-weight: 600;
  color: #f59e0b;
}

.time-text {
  color: #64748b;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #f1f5f9;
}

.total-text {
  font-size: 12px;
  color: #64748b;
}

.pagination {
  display: flex;
  align-items: center;
}
</style>
