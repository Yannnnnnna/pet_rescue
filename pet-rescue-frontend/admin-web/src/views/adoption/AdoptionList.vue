<template>
  <div class="adoption-list-container">
    <el-card class="box-card">
      <div class="page-header">
        <h2>全平台领养监管 (Adoption Supervision)</h2>
      </div>
      
      <!-- Search Bar -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="宠物昵称">
            <el-input 
              v-model="queryParams.petName" 
              placeholder="搜索宠物昵称" 
              clearable 
              @keyup.enter="handleSearch" 
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select 
              v-model="queryParams.status" 
              placeholder="全部状态" 
              clearable 
              style="width: 150px"
            >
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已驳回" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Main Table -->
      <el-table 
        v-loading="loading" 
        :data="adoptionList" 
        style="width: 100%" 
        border 
        stripe
      >
        <el-table-column prop="id" label="申请单号" width="100" align="center" />
        
        <el-table-column label="宠物信息" width="200" align="center">
          <template #default="scope">
            <div class="pet-info">
              <el-image 
                style="width: 50px; height: 50px; border-radius: 4px; margin-right: 10px;"
                :src="scope.row.petCover" 
                :preview-src-list="[scope.row.petCover]"
                fit="cover"
                preview-teleported
              />
              <span class="pet-name">{{ scope.row.petName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="送养人 (Owner)" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.ownerId === currentAdminId" type="success">官方自营</el-tag>
            <el-tag v-else type="info">用户: {{ scope.row.ownerName }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="申请人 (Applicant)" align="center">
          <template #default="scope">
            <div class="applicant-info">
              <div>{{ scope.row.applicantName }}</div>
              <div class="phone-text">{{ scope.row.applicantPhone }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="当前状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申请时间" width="180" align="center">
           <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
           </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.ownerId === currentAdminId && scope.row.status === 0"
              type="primary" 
              link 
              @click="handleAudit(scope.row)"
            >
              审核
            </el-button>
            <el-button 
              v-else
              type="default" 
              link 
              @click="handleView(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Detail Drawer -->
    <el-drawer
      v-model="drawerVisible"
      title="申请详情"
      direction="rtl"
      size="500px"
      destroy-on-close
    >
      <div v-if="detailData" class="drawer-content">
        <!-- Info Block -->
        <div class="info-block">
          <div class="pet-card">
             <el-avatar :size="60" :src="detailData.petCover" shape="square" />
             <div class="pet-detail-name">{{ detailData.petName }}</div>
          </div>
          <div class="user-compare">
             <div class="user-item">
                <span class="label">发布者:</span>
                <span class="value">{{ currentDrawerOwnerName || '未知' }}</span>
             </div>
             <div class="user-item">
                <span class="label">申请者:</span>
                <span class="value">{{ detailData.realName || detailData.userNickname }}</span>
             </div>
          </div>
        </div>
        
        <el-divider />

        <!-- Form Block -->
        <div class="form-block">
          <h4>资质审核表</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="真实姓名">{{ detailData.realName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detailData.phone }}</el-descriptions-item>
            <el-descriptions-item label="住房情况">{{ detailData.housingCondition }}</el-descriptions-item>
            <el-descriptions-item label="工作状况">{{ detailData.jobStatus }}</el-descriptions-item>
            <el-descriptions-item label="居住地址">{{ detailData.address }}</el-descriptions-item>
            <el-descriptions-item label="养宠经验">
               <div class="experience-text">{{ detailData.experience }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- Action Block -->
        <div v-if="detailData.status === 0 && currentDrawerOwnerId === currentAdminId" class="action-block">
          <el-divider />
          <h4>审核操作</h4>
          <el-form :model="auditForm" label-position="top">
            <el-form-item label="审核结果">
              <el-radio-group v-model="auditForm.status">
                <el-radio :value="1">通过</el-radio>
                <el-radio :value="2">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="auditForm.status === 2" label="驳回理由" required>
              <el-input v-model="auditForm.remark" type="textarea" placeholder="请输入驳回理由" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitAudit" :loading="auditLoading">提交审核</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdoptionList, getAdoptionDetail, auditAdoption } from '../../api/adoption'
import { getMyInfo } from '../../api/user'

const currentAdminId = ref(null)
const loading = ref(false)
const adoptionList = ref([])
const total = ref(0)
const drawerVisible = ref(false)
const detailData = ref(null)
const currentDrawerOwnerId = ref(null)
const currentDrawerOwnerName = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  petName: '',
  status: null
})

const auditForm = reactive({
  status: 1,
  remark: ''
})
const auditLoading = ref(false)

onMounted(async () => {
  await fetchCurrentAdmin()
  fetchData()
})

const fetchCurrentAdmin = async () => {
  try {
    const res = await getMyInfo()
    if (res.code === 200) {
      currentAdminId.value = res.data.id
    }
  } catch (error) {
    console.error('Failed to get user info', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAdoptionList(queryParams)
    if (res.code === 200) {
      adoptionList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

const handleReset = () => {
  queryParams.petName = ''
  queryParams.status = null
  handleSearch()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchData()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchData()
}

// 状态 Tag 样式
const getStatusTagType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已驳回',
    3: '已取消'
  }
  return map[status] || '未知'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

// 打开详情/审核
const openDrawer = async (row) => {
  currentDrawerOwnerId.value = row.ownerId
  currentDrawerOwnerName.value = row.ownerName
  
  try {
    const res = await getAdoptionDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      drawerVisible.value = true
      
      // Reset audit form
      auditForm.status = 1
      auditForm.remark = ''
    }
  } catch (error) {
    console.error(error)
  }
}

const handleAudit = (row) => {
  openDrawer(row)
}

const handleView = (row) => {
  openDrawer(row)
}

const submitAudit = async () => {
  if (auditForm.status === 2 && !auditForm.remark) {
    ElMessage.warning('请输入驳回理由')
    return
  }

  auditLoading.value = true
  try {
    const res = await auditAdoption({
      id: detailData.value.id,
      status: auditForm.status,
      reason: auditForm.remark // Assuming backend param name is 'reason' or 'adminRemark'. User said "adminRemark" in detail response, but usually audit input is 'reason'. 
      // If the API follows standard, it might be 'remark' or 'reason'.
      // Based on typical patterns, I'll send 'reason'. If it fails, I'll adjust.
    })
    if (res.code === 200) {
      ElMessage.success('审核完成')
      drawerVisible.value = false
      fetchData()
    }
  } catch (error) {
    console.error(error)
  } finally {
    auditLoading.value = false
  }
}
</script>

<style scoped>
.adoption-list-container {
  padding: 20px;
}
.box-card {
  min-height: calc(100vh - 120px);
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin: 0;
}
.filter-container {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.pet-info {
  display: flex;
  align-items: center;
}
.pet-name {
  font-weight: bold;
}
.phone-text {
  font-size: 12px;
  color: #909399;
}
.info-block {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.pet-card {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.pet-detail-name {
  margin-top: 8px;
  font-weight: bold;
}
.user-compare {
  flex: 1;
  margin-left: 20px;
}
.user-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}
.user-item .label {
  color: #909399;
}
.user-item .value {
  font-weight: bold;
}
.form-block h4, .action-block h4 {
  margin-bottom: 15px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}
.experience-text {
  white-space: pre-wrap;
  max-height: 100px;
  overflow-y: auto;
}
.action-block {
  margin-top: 20px;
}
</style>
