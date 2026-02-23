<template>
  <div class="article-list-container">
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-grid">
          <div class="filter-item">
            <label class="filter-label">关键字</label>
            <el-input v-model="queryParams.keyword" placeholder="标题/摘要" clearable @keyup.enter="handleSearch" class="filter-input" />
          </div>
          <div class="filter-item">
            <label class="filter-label">类型</label>
            <el-select v-model="queryParams.type" placeholder="全部类型" clearable class="filter-input">
              <el-option label="养宠百科" :value="0" />
              <el-option label="救助公告" :value="1" />
              <el-option label="活动" :value="2" />
              <el-option label="壁纸" :value="3" />
            </el-select>
          </div>
          <div class="filter-item">
            <label class="filter-label">分类</label>
            <el-input v-model="queryParams.category" placeholder="分类筛选" clearable @keyup.enter="handleSearch" class="filter-input" />
          </div>
        </div>
        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch" class="btn-search">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset" class="btn-reset">重置</el-button>
          <el-button type="success" :icon="Plus" @click="handleAdd" class="btn-add">新增</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="articleList"
        style="width: 100%"
        class="data-table"
      >
        <el-table-column label="封面图" width="100" align="center">
          <template #default="scope">
            <div class="cover-wrapper">
              <el-image
                class="cover-image"
                :src="scope.row.coverImg"
                :preview-src-list="[scope.row.coverImg]"
                fit="cover"
                preview-teleported
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" align="left" show-overflow-tooltip>
          <template #default="scope">
            <span class="article-title-text">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <span :class="['type-tag', getTypeClass(scope.row.type)]">
              {{ getTypeLabel(scope.row.type) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" align="center">
          <template #default="scope">
            <span>{{ scope.row.category || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center" sortable="custom">
          <template #default="scope">
            <span class="view-count">{{ scope.row.viewCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <div class="action-row">
                <el-button type="primary" link size="small" @click="handleView(scope.row)">
                  <el-icon><View /></el-icon>
                  <span>查看</span>
                </el-button>
                <el-button type="primary" link size="small" @click="handleEdit(scope.row)">
                  <el-icon><Edit /></el-icon>
                  <span>编辑</span>
                </el-button>
              </div>
              <div class="action-row">
                <el-button type="success" link size="small" @click="handleSetBanner(scope.row)">
                  <el-icon><Monitor /></el-icon>
                  <span>设为轮播</span>
                </el-button>
                <el-button type="danger" link size="small" @click="handleDelete(scope.row)">
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </el-button>
              </div>
            </div>
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

    <!-- 查看详情弹窗 (简易版，只展示基本信息和内容) -->
    <el-dialog
      v-model="detailVisible"
      title="文章详情"
      width="800px"
      destroy-on-close
      top="5vh"
    >
      <div v-if="currentArticle" class="article-detail">
        <h2 class="article-title">{{ currentArticle.title }}</h2>
        <div class="article-meta">
          <el-tag :type="getTypeTagType(currentArticle.type)" size="small">{{ getTypeLabel(currentArticle.type) }}</el-tag>
          <span class="meta-item">分类: {{ currentArticle.category }}</span>
          <span class="meta-item">作者: {{ currentArticle.author || '管理员' }}</span>
          <span class="meta-item">时间: {{ formatTime(currentArticle.createTime) }}</span>
          <span class="meta-item">浏览: {{ currentArticle.viewCount }}</span>
        </div>

        <!-- 活动信息 (类型为活动时显示) -->
        <div v-if="currentArticle.type === 2" class="activity-info">
          <div class="info-item">
            <strong>活动时间：</strong>
            {{ formatTime(currentArticle.activityStartTime) }} 至 {{ formatTime(currentArticle.activityEndTime) }}
          </div>
          <div class="info-item">
            <strong>活动地点：</strong>
            {{ currentArticle.activityAddress }}
          </div>
        </div>

        <!-- 摘要 (百科/公告显示) -->
        <div class="article-summary" v-if="[0, 1].includes(currentArticle.type) && currentArticle.summary">
          <strong>摘要：</strong>{{ currentArticle.summary }}
        </div>

        <!-- 图片展示区 -->
        <div class="article-cover" v-if="currentArticle.coverImg || currentArticle.wallpaperUrl">
           <!-- 壁纸类型优先展示高清壁纸 -->
           <el-image 
             v-if="currentArticle.type === 3 && currentArticle.wallpaperUrl"
             :src="currentArticle.wallpaperUrl" 
             fit="contain" 
             style="width: 100%; max-height: 600px" 
             :preview-src-list="[currentArticle.wallpaperUrl]"
             preview-teleported
           >
             <template #error>
               <div class="image-slot">高清壁纸加载失败</div>
             </template>
           </el-image>

           <!-- 其他类型展示封面/海报 -->
           <el-image 
             v-else-if="currentArticle.coverImg"
             :src="currentArticle.coverImg" 
             fit="contain" 
             style="width: 100%; max-height: 400px" 
             :preview-src-list="[currentArticle.coverImg]"
             preview-teleported
           />
        </div>

        <el-divider v-if="currentArticle.type !== 3" />
        
        <!-- 正文内容 (非壁纸类型显示) -->
        <div v-if="currentArticle.type !== 3" class="article-content" v-html="currentArticle.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList, deleteArticle } from '../../api/article'
import { addBanner } from '../../api/banner'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, View, Picture, Monitor } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const articleList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentArticle = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  type: undefined,
  category: ''
})

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

const getTypeLabel = (type) => {
  const map = {
    0: '养宠百科',
    1: '救助公告',
    2: '活动',
    3: '壁纸'
  }
  return map[type] || '未知'
}

const getTypeTagType = (type) => {
  const map = {
    0: 'primary',   // 蓝色
    1: 'danger',    // 红色
    2: 'warning',   // 黄色
    3: 'success'    // 紫色(ElementPlus Success是绿色，可以用color自定义，或者用 info)
  }
  if (type === 3) return 'success'
  if (type === 0) return ''
  return map[type] || 'info'
}

const getTypeClass = (type) => {
  const map = {
    0: 'encyclopedia',
    1: 'notice',
    2: 'activity',
    3: 'wallpaper'
  }
  return map[type] || 'encyclopedia'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getArticleList(queryParams)
    if (res.code === 200) {
      articleList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取列表失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('系统错误')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.type = undefined
  queryParams.category = ''
  handleSearch()
}

const handleAdd = () => {
  // Use named route for adding article to ensure correct path (/admin/article/editor)
  router.push({ name: 'ArticleEditor' })
}

const handleEdit = (row) => {
  // Use named route with params, handled by router config under /admin children
  // Route is defined as: path: 'article/editor/:id', name: 'ArticleEdit'
  // So the resulting path will be /admin/article/editor/123
  router.push({ name: 'ArticleEdit', params: { id: row.id } })
}

const handleView = (row) => {
  currentArticle.value = row
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除文章 "${row.title}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteArticle(row.id)
      if (res.code === 200) { // Assuming 200 is success for delete
        ElMessage.success('删除成功')
        fetchList()
      } else {
        // If API is not standard or returns boolean in data
         if (res.data === true) {
             ElMessage.success('删除成功')
             fetchList()
         } else {
             ElMessage.error(res.msg || '删除失败')
         }
      }
    } catch (error) {
      console.error(error)
      // ElMessage.error('删除失败')
    }
  })
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchList()
}

const handleSetBanner = (row) => {
  ElMessageBox.confirm(`确定要将文章 "${row.title}" 设为首页轮播图吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 准备轮播图数据
      const bannerData = {
        imgUrl: row.type === 3 ? row.wallpaperUrl : row.coverImg, // 如果是壁纸类型(3)，优先使用壁纸大图
        sortOrder: 0,
        status: 1,
        targetUrl: ''
      }
      
      // 根据文章类型设置跳转路径
      if (row.type === 1) { // 公告
         bannerData.targetUrl = `/pages/wiki/detail?id=${row.id}`
      } else if (row.type === 2) { // 活动
         bannerData.targetUrl = `/pages/wiki/detail?id=${row.id}`
      } else if (row.type === 0) { // 百科
         bannerData.targetUrl = `/pages/wiki/detail?id=${row.id}`
      } else if (row.type === 3) { // 壁纸
         bannerData.targetUrl = '' // 壁纸不跳转
      }

      await addBanner(bannerData)
      ElMessage.success('设置成功，请前往轮播图管理页面查看')
    } catch (error) {
      console.error(error)
      // ElMessage.error('设置失败')
    }
  })
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.article-list-container {
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
  width: 180px;
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

.btn-add {
  background: #10b981;
  border-color: #10b981;
  border-radius: 8px;
}

.btn-add:hover {
  background: #059669;
  border-color: #059669;
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

.cover-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  margin: 0 auto;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 20px;
}

.article-title-text {
  font-weight: 500;
  color: #1e293b;
}

.type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid;
}

.type-tag.encyclopedia {
  background: #eff6ff;
  color: #3b82f6;
  border-color: #bfdbfe;
}

.type-tag.notice {
  background: #fef2f2;
  color: #ef4444;
  border-color: #fecaca;
}

.type-tag.activity {
  background: #fffbeb;
  color: #f59e0b;
  border-color: #fde68a;
}

.type-tag.wallpaper {
  background: #f5f3ff;
  color: #8b5cf6;
  border-color: #ddd6fe;
}

.view-count {
  font-weight: 600;
  color: #f59e0b;
}

.time-text {
  color: #64748b;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-row .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
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

.article-detail {
  padding: 10px;
}

.article-title {
  text-align: center;
  margin-bottom: 15px;
  color: #1e293b;
  font-weight: 700;
}

.article-meta {
  text-align: center;
  margin-bottom: 20px;
  color: #64748b;
  font-size: 13px;
}

.meta-item {
  margin-left: 15px;
}

.article-summary {
  background-color: #f8fafc;
  padding: 12px;
  border-radius: 8px;
  color: #475569;
  margin-bottom: 15px;
  font-size: 14px;
  line-height: 1.6;
  border: 1px solid #e2e8f0;
}

.article-cover {
  margin-bottom: 20px;
  text-align: center;
}

.article-content {
  line-height: 1.8;
  color: #334155;
  font-size: 16px;
}

:deep(.article-content img) {
  max-width: 100%;
  height: auto;
}

.activity-info {
  background-color: #ecfdf5;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #059669;
  border: 1px solid #d1fae5;
}

.info-item {
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}
</style>
