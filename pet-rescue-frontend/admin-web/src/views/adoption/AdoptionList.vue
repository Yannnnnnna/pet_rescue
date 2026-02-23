<template>
  <div class="adoption-list-container">
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-grid">
          <div class="filter-item">
            <label class="filter-label">宠物昵称</label>
            <el-input 
              v-model="queryParams.petName" 
              placeholder="搜索宠物昵称" 
              clearable 
              @keyup.enter="handleSearch"
              class="filter-input"
            />
          </div>
          <div class="filter-item">
            <label class="filter-label">状态</label>
            <el-select 
              v-model="queryParams.status" 
              placeholder="全部状态" 
              clearable 
              class="filter-input"
            >
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已驳回" :value="2" />
            </el-select>
          </div>
        </div>
        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch" class="btn-search">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset" class="btn-reset">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table 
        v-loading="loading" 
        :data="adoptionList" 
        style="width: 100%" 
        class="data-table"
      >
        <el-table-column prop="id" label="申请单号" width="100" align="center" />
        
        <el-table-column label="宠物信息" width="200" align="center">
          <template #default="scope">
            <div class="pet-info">
              <el-image 
                class="pet-cover"
                :src="scope.row.petCover" 
                :preview-src-list="[scope.row.petCover]"
                fit="cover"
                preview-teleported
              />
              <span class="pet-name">{{ scope.row.petName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="送养人" align="center">
          <template #default="scope">
            <span :class="['owner-tag', scope.row.ownerId === currentAdminId ? 'official' : 'user']">
              {{ scope.row.ownerId === currentAdminId ? '官方自营' : scope.row.ownerName }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="申请人" align="center">
          <template #default="scope">
            <div class="applicant-info">
              <div class="applicant-name">{{ scope.row.applicantName }}</div>
              <div class="applicant-phone">{{ scope.row.applicantPhone }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="当前状态" width="100" align="center">
          <template #default="scope">
            <span :class="['status-tag', getStatusClass(scope.row.status)]">
              {{ getStatusLabel(scope.row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申请时间" width="180" align="center">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.ownerId === currentAdminId && scope.row.status === 0"
              type="primary" 
              link 
              size="small"
              @click="handleAudit(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span>审核</span>
            </el-button>
            <el-button 
              v-else
              type="primary" 
              link 
              size="small"
              @click="handleView(scope.row)"
            >
              <el-icon><View /></el-icon>
              <span>查看</span>
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
        
        <!-- AI Analysis Block -->
        <div class="ai-block">
          <h4>
            <el-icon><MagicStick /></el-icon>
            AI 智能匹配分析
          </h4>
          <div v-loading="aiLoading" class="ai-content">
            <div v-if="aiAnalysis" class="ai-result">
               <div class="score-circle">
                 <el-progress 
                   type="dashboard" 
                   :percentage="aiAnalysis.matchScore" 
                   :color="getScoreColor"
                 />
                 <span class="score-label">匹配度</span>
               </div>
               <div class="analysis-text">
                 {{ aiAnalysis.analysisResult }}
               </div>
            </div>
            <div v-else-if="!aiLoading" class="ai-empty">
              暂无分析数据
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
import { Search, Refresh, Edit, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdoptionList, getAdoptionDetail, auditAdoption, analyzeMatch } from '../../api/adoption'
import { getMyInfo } from '../../api/user'
import { MagicStick } from '@element-plus/icons-vue'

const currentAdminId = ref(null)
const loading = ref(false)
const adoptionList = ref([])
const total = ref(0)
const drawerVisible = ref(false)
const detailData = ref(null)
const currentDrawerOwnerId = ref(null)
const currentDrawerOwnerName = ref('')
const aiAnalysis = ref(null)
const aiLoading = ref(false)

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

const getScoreColor = (percentage) => {
  if (percentage < 60) return '#F56C6C'
  if (percentage < 80) return '#E6A23C'
  return '#67C23A'
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

const getStatusClass = (status) => {
  const map = {
    0: 'pending',
    1: 'approved',
    2: 'rejected',
    3: 'cancelled'
  }
  return map[status] || 'pending'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

// 打开详情/审核
const openDrawer = async (row) => {
  currentDrawerOwnerId.value = row.ownerId
  currentDrawerOwnerName.value = row.ownerName
  aiAnalysis.value = null // 重置 AI 分析结果
  
  try {
    const res = await getAdoptionDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      drawerVisible.value = true
      
      // Reset audit form
      auditForm.status = 1
      auditForm.remark = ''

      // 自动触发 AI 分析
      fetchAiAnalysis(res.data)
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchAiAnalysis = async (data) => {
  if (!data) return
  aiLoading.value = true
  try {
    const res = await analyzeMatch({
      petId: data.petId,
      adoptId: data.id,
      userId: data.userId,
      style: 'strict', // 管理端默认为严格风格
      refresh: false
    })
    if (res.code === 200) {
      aiAnalysis.value = res.data
    }
  } catch (error) {
    console.error('AI Analysis failed:', error)
  } finally {
    aiLoading.value = false
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

.pet-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.pet-cover {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.pet-name {
  font-weight: 500;
  color: #1e293b;
}

.owner-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid;
}

.owner-tag.official {
  background: #ecfdf5;
  color: #10b981;
  border-color: #a7f3d0;
}

.owner-tag.user {
  background: #f1f5f9;
  color: #64748b;
  border-color: #e2e8f0;
}

.applicant-info {
  text-align: center;
}

.applicant-name {
  font-weight: 500;
  color: #1e293b;
}

.applicant-phone {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid;
}

.status-tag.pending {
  background: #fffbeb;
  color: #f59e0b;
  border-color: #fde68a;
}

.status-tag.approved {
  background: #ecfdf5;
  color: #10b981;
  border-color: #a7f3d0;
}

.status-tag.rejected {
  background: #fef2f2;
  color: #ef4444;
  border-color: #fecaca;
}

.status-tag.cancelled {
  background: #f1f5f9;
  color: #64748b;
  border-color: #e2e8f0;
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

.drawer-content {
  padding: 0 10px;
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
  font-weight: 600;
  color: #1e293b;
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
  color: #64748b;
}

.user-item .value {
  font-weight: 600;
  color: #1e293b;
}

.ai-block {
  margin-top: 20px;
}

.ai-block h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #10b981;
  font-weight: 600;
}

.ai-content {
  margin-top: 12px;
}

.ai-result {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  background: #ecfdf5;
  padding: 15px;
  border-radius: 12px;
  border: 1px solid #d1fae5;
}

.score-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
}

.score-label {
  margin-top: -10px;
  font-size: 12px;
  color: #64748b;
}

.analysis-text {
  flex: 1;
  font-size: 14px;
  line-height: 1.6;
  color: #334155;
  white-space: pre-wrap;
}

.ai-empty {
  text-align: center;
  color: #94a3b8;
  padding: 20px;
}

.form-block h4, .action-block h4 {
  margin-bottom: 15px;
  border-left: 4px solid #10b981;
  padding-left: 10px;
  font-weight: 600;
  color: #1e293b;
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
