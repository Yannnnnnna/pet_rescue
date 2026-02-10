<template>
  <div class="article-list-container">
    <el-card class="box-card">
      <!-- 筛选区域 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="关键字">
            <el-input v-model="queryParams.keyword" placeholder="标题/摘要" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 120px">
              <el-option label="养宠百科" :value="0" />
              <el-option label="救助公告" :value="1" />
              <el-option label="活动" :value="2" />
              <el-option label="壁纸" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
             <el-input v-model="queryParams.category" placeholder="分类筛选" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            <el-button type="success" :icon="Plus" @click="handleAdd">新增</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="articleList"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column label="封面图" width="120">
          <template #default="scope">
            <el-image
              style="width: 80px; height: 80px; border-radius: 4px"
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
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" align="left" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)" effect="plain">
              {{ getTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" align="center" />
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center" sortable="custom" />
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button type="info" link :icon="View" @click="handleView(scope.row)">查看</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, View, Picture } from '@element-plus/icons-vue'

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
    // 用户提到：壁纸-紫色，百科-蓝色
  }
  // 如果想严格对应颜色，可以返回 '' 然后在style里写class，或者直接用 success/warning 等预设
  // 这里暂时用 element 预设
  if (type === 3) return 'success' // 壁纸
  if (type === 0) return '' // default blue
  return map[type] || 'info'
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
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

.article-detail {
  padding: 10px;
}

.article-title {
  text-align: center;
  margin-bottom: 15px;
  color: #303133;
}

.article-meta {
  text-align: center;
  margin-bottom: 20px;
  color: #909399;
  font-size: 13px;
}

.meta-item {
  margin-left: 15px;
}

.article-summary {
  background-color: #f4f4f5;
  padding: 10px;
  border-radius: 4px;
  color: #606266;
  margin-bottom: 15px;
  font-size: 14px;
  line-height: 1.6;
}

.article-cover {
  margin-bottom: 20px;
  text-align: center;
}

.article-content {
  line-height: 1.8;
  color: #333;
  font-size: 16px;
}
/* Handle rich text content images */
:deep(.article-content img) {
  max-width: 100%;
  height: auto;
}

.activity-info {
  background-color: #f0f9eb;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.info-item {
  margin-bottom: 8px;
  font-size: 14px;
}
.info-item:last-child {
  margin-bottom: 0;
}
</style>
