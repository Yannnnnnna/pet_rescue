<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container" shadow="never">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="宠物ID">
          <el-input v-model="queryParams.petId" placeholder="请输入宠物ID" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="发布人ID">
          <el-input v-model="queryParams.userId" placeholder="请输入发布人ID" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据列表 -->
    <el-card class="table-container" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="petId" label="宠物ID" width="100" align="center" />
        <el-table-column prop="userId" label="发布人ID" width="100" align="center" />
        <el-table-column prop="content" label="内容摘要" min-width="200" show-overflow-tooltip />
        <el-table-column label="图片预览" width="120" align="center">
          <template #default="scope">
            <el-image
              v-if="getFirstImage(scope.row.images)"
              style="width: 50px; height: 50px"
              :src="getFirstImage(scope.row.images)"
              :preview-src-list="getAllImages(scope.row.images)"
              fit="cover"
              preview-teleported
            />
            <span v-else>无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" align="center">
            <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
            </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.auditStatus === 0"
              size="small"
              type="primary"
              @click="handleAudit(scope.row)"
            >
              审核
            </el-button>
            <el-button
              v-else
              size="small"
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <!-- 删除按钮预留，目前API未明确提供删除接口，但可以使用驳回代替或者添加删除逻辑 -->
            <!-- <el-button size="small" type="danger" link>删除</el-button> -->
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 审核/详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isAudit ? '审核领养日记' : '领养日记详情'"
      width="70%"
      top="5vh"
      destroy-on-close
    >
      <div class="audit-content" v-loading="detailLoading">
        <el-row :gutter="20">
          <!-- 左侧信息区 -->
          <el-col :span="10">
            <div class="info-section">
              <h3 class="section-title">宠物信息</h3>
              <div v-if="currentPet" class="pet-info">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="宠物ID">{{ currentPet.id }}</el-descriptions-item>
                  <el-descriptions-item label="品种">{{ currentPet.variety || '未知' }}</el-descriptions-item>
                  <el-descriptions-item label="性别">{{ currentPet.sex === 1 ? '公' : (currentPet.sex === 0 ? '母' : '未知') }}</el-descriptions-item>
                  <el-descriptions-item label="年龄">{{ currentPet.age || '未知' }}</el-descriptions-item>
                </el-descriptions>
              </div>
              <div v-else class="no-data">暂无宠物信息</div>
            </div>

            <div class="info-section">
              <h3 class="section-title">日记正文</h3>
              <div class="diary-content">
                {{ currentPost.content }}
              </div>
              <div class="publish-info">
                发布时间：{{ formatDate(currentPost.createTime) }}
              </div>
            </div>
          </el-col>

          <!-- 右侧图片区 -->
          <el-col :span="14">
            <h3 class="section-title">图片展示</h3>
            <div class="image-gallery">
              <el-empty v-if="!postImages.length" description="暂无图片" />
              <div v-else class="image-grid">
                <el-image
                  v-for="(img, index) in postImages"
                  :key="index"
                  :src="img"
                  :preview-src-list="postImages"
                  :initial-index="index"
                  fit="contain"
                  class="gallery-image"
                  preview-teleported
                />
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <template v-if="isAudit">
            <el-button @click="submitAudit(2)" type="danger" plain>驳回</el-button>
            <el-button @click="submitAudit(1)" type="primary">通过</el-button>
          </template>
          <el-button v-else @click="dialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdoptionPostList, reviewAdoptionPost } from '../../api/adoptionPost'
import { getPetDetail } from '../../api/pet'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isAudit = ref(false)
const detailLoading = ref(false)
const currentPost = ref({})
const currentPet = ref(null)
const postImages = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  petId: undefined,
  userId: undefined,
  auditStatus: 0 // 默认选中待审核
})

const fetchList = async () => {
  loading.value = true
  try {
    // 构造请求参数，如果为空字符串则传undefined，避免后端解析错误
    const params = {
      ...queryParams,
      petId: queryParams.petId || undefined,
      userId: queryParams.userId || undefined
    }
    const res = await getAdoptionPostList(params)
    if (res.code === 200) {
      tableData.value = res.data
      // 假设后端返回的是PageInfo或者类似的结构，如果只是数组，则需要调整total
      // 根据提供的API文档，返回的是ResultListAdoptionPost，data是Array<AdoptionPost>
      // 如果没有分页信息返回，可能需要模拟或者确认API是否支持分页返回total
      // 这里暂时假设返回的data就是列表，total未知，或者API实际上是分页结构
      // 如果API文档只说了返回array，那么可能没有分页总数。
      // 通常list接口会返回total。如果实在没有，先设为list.length或者0
      total.value = res.data.length // 临时处理
      // 如果后端支持分页但文档没写全，通常会返回 { rows: [], total: 100 } 或 similar
    } else {
      ElMessage.error(res.msg || '获取列表失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchList()
}

const resetQuery = () => {
  queryParams.petId = undefined
  queryParams.userId = undefined
  queryParams.auditStatus = undefined
  handleSearch()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchList()
}

const parseImages = (imagesStr) => {
  if (!imagesStr) return []
  try {
    // 尝试解析JSON数组
    const parsed = JSON.parse(imagesStr)
    if (Array.isArray(parsed)) return parsed
    return [imagesStr]
  } catch (e) {
    // 如果不是JSON，尝试逗号分隔
    if (imagesStr.includes(',')) {
      return imagesStr.split(',').map(s => s.trim())
    }
    return [imagesStr]
  }
}

const getFirstImage = (imagesStr) => {
  const imgs = parseImages(imagesStr)
  return imgs.length > 0 ? imgs[0] : ''
}

const getAllImages = (imagesStr) => {
  return parseImages(imagesStr)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已驳回'
  }
  return map[status] || '未知'
}

const loadPetInfo = async (petId) => {
  if (!petId) {
    currentPet.value = null
    return
  }
  try {
    const res = await getPetDetail(petId)
    if (res.code === 200) {
      currentPet.value = res.data
    } else {
      currentPet.value = null
    }
  } catch (error) {
    console.error(error)
    currentPet.value = null
  }
}

const openDialog = async (row, auditMode) => {
  currentPost.value = { ...row }
  isAudit.value = auditMode
  postImages.value = getAllImages(row.images)
  dialogVisible.value = true
  detailLoading.value = true
  
  await loadPetInfo(row.petId)
  
  detailLoading.value = false
}

const handleAudit = (row) => {
  openDialog(row, true)
}

const handleView = (row) => {
  openDialog(row, false)
}

const submitAudit = async (status) => {
  try {
    const res = await reviewAdoptionPost({
      postId: currentPost.value.id,
      status: status
    })
    
    if (res.code === 200) {
      ElMessage.success(status === 1 ? '审核通过' : '已驳回')
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
}
.table-container {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.diary-content {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  line-height: 1.6;
  min-height: 100px;
  white-space: pre-wrap;
  margin-bottom: 10px;
}
.publish-info {
  font-size: 12px;
  color: #909399;
  text-align: right;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}
.gallery-image {
  width: 100%;
  height: 150px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
.pet-info {
  margin-bottom: 20px;
}
.no-data {
  color: #909399;
  font-size: 14px;
  text-align: center;
  padding: 20px 0;
}
</style>
