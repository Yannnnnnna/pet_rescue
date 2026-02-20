<template>
  <div class="banner-list-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增轮播图</el-button>
        <el-button :icon="Refresh" @click="fetchList">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="bannerList" border style="width: 100%">
        <el-table-column prop="sortOrder" label="排序" width="80" align="center">
          <template #default="scope">
            <span v-if="!scope.row.isEditing">{{ scope.row.sortOrder }}</span>
            <el-input-number v-else v-model="scope.row.editSortOrder" :min="0" :max="999" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="图片" width="180" align="center">
          <template #default="scope">
            <el-image
              style="width: 160px; height: 90px; border-radius: 4px"
              :src="scope.row.imgUrl"
              :preview-src-list="[scope.row.imgUrl]"
              fit="cover"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="targetUrl" label="跳转路径" min-width="350">
          <template #default="scope">
             <div v-if="!scope.row.isEditing">{{ scope.row.targetUrl || '无' }}</div>
             <div v-else class="edit-row-container">
               <el-select 
                 v-model="scope.row.editBusinessType" 
                 placeholder="类型" 
                 style="width: 110px"
                 size="small"
                 @change="(val) => handleRowTypeChange(scope.row, val)"
               >
                  <el-option label="不跳转" value="none" />
                  <el-option label="公告" value="notice" />
                  <el-option label="活动" value="activity" />
                  <el-option label="文章" value="article" />
                  <el-option label="壁纸" value="wallpaper" />
               </el-select>
               
               <el-select
                 v-if="scope.row.editBusinessType !== 'none' && scope.row.editBusinessType !== 'wallpaper'"
                 v-model="scope.row.editContentId"
                 placeholder="选择内容"
                 filterable
                 remote
                 :remote-method="(query) => searchRowContent(scope.row, query)"
                 :loading="scope.row.searchLoading"
                 @change="(val) => handleRowContentChange(scope.row, val)"
                 style="width: 200px; margin-left: 5px;"
                 size="small"
                 @visible-change="(visible) => visible && searchRowContent(scope.row, '')"
               >
                 <el-option
                   v-for="item in scope.row.contentOptions"
                   :key="item.id"
                   :label="item.label"
                   :value="item.id"
                 />
               </el-select>
             </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="!scope.row.isEditing" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '显示' : '隐藏' }}
            </el-tag>
            <el-switch
              v-else
              v-model="scope.row.editStatus"
              :active-value="1"
              :inactive-value="0"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <template v-if="!scope.row.isEditing">
              <el-button type="primary" link :icon="Edit" @click="handleEditRow(scope.row)">编辑</el-button>
              <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            </template>
            <template v-else>
               <el-button type="success" link :icon="Check" @click="handleSaveRow(scope.row)">保存</el-button>
               <el-button type="info" link :icon="Close" @click="handleCancelRow(scope.row)">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增弹窗 (仅保留新增功能) -->
    <el-dialog
      v-model="dialogVisible"
      title="新增轮播图"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="图片地址" prop="imgUrl">
          <el-input v-model="formData.imgUrl" placeholder="请输入图片URL或上传" />
          <!-- 预留上传组件位置，目前使用Input -->
        </el-form-item>
        
        <el-form-item label="业务类型">
          <el-select v-model="businessType" placeholder="选择业务类型" @change="handleBusinessTypeChange">
            <el-option label="不跳转" value="none" />
            <el-option label="救助公告" value="notice" />
            <el-option label="社区活动" value="activity" />
            <el-option label="养宠百科" value="article" />
            <el-option label="宠物详情" value="pet" />
            <el-option label="壁纸" value="wallpaper" />
          </el-select>
        </el-form-item>

        <el-form-item label="具体内容" v-if="businessType !== 'none' && businessType !== 'wallpaper'">
          <el-select
            v-model="selectedContentId"
            placeholder="请选择具体内容"
            filterable
            remote
            :remote-method="searchContent"
            :loading="searchLoading"
            @change="handleContentChange"
            style="width: 100%"
          >
            <el-option
              v-for="item in contentOptions"
              :key="item.id"
              :label="item.label"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="跳转路径" prop="targetUrl">
          <el-input v-model="formData.targetUrl" placeholder="自动生成或手动输入" readonly />
          <div class="form-tip">选择业务类型和具体内容后自动生成</div>
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Edit, Delete, Refresh, Check, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBannerList, addBanner, updateBanner, deleteBanner } from '../../api/banner'
import { getArticleList, getArticleDetail } from '../../api/article'
import { getPetList, getPetDetail } from '../../api/pet'
import { formatDate } from '../../utils/format'

const loading = ref(false)
const bannerList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const formData = reactive({
  imgUrl: '',
  targetUrl: '',
  sortOrder: 0,
  status: 1
})

const businessType = ref('none')
const selectedContentId = ref(null)
const contentOptions = ref([])
const searchLoading = ref(false)

const rules = {
  imgUrl: [{ required: true, message: '请输入图片地址', trigger: 'blur' }],
  targetUrl: [{ required: false, message: '自动生成跳转路径', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getBannerList()
    if (res.code === 200) {
      // 列表数据处理
      bannerList.value = res.data.map(item => ({
        ...item,
        isEditing: false,
        editSortOrder: item.sortOrder,
        editStatus: item.status,
        editBusinessType: 'none',
        editContentId: null,
        contentOptions: [],
        searchLoading: false
      }))
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

const handleAdd = () => {
  // 重置表单
  Object.assign(formData, {
    imgUrl: '',
    targetUrl: '',
    sortOrder: 0,
    status: 1
  })
  businessType.value = 'none'
  selectedContentId.value = null
  contentOptions.value = []
  
  dialogVisible.value = true
}

const handleEditRow = (row) => {
  // 退出其他行编辑状态
  bannerList.value.forEach(item => {
    item.isEditing = false
  })

  // 设置当前行编辑状态
  row.isEditing = true
  row.editSortOrder = row.sortOrder
  row.editStatus = row.status
  row.contentOptions = []
  row.searchLoading = false

  // 反向解析业务类型
  if (!row.targetUrl) {
    row.editBusinessType = 'none'
  } else if (row.targetUrl.includes('/pages/wiki/detail')) {
     // 新路径：公告/活动/文章
     // 由于路径一样，我们这里可能无法准确知道是哪种类型，
     // 默认回显为 'article'，用户可以自己改。
     row.editBusinessType = 'article' 
     const match = row.targetUrl.match(/id=(\d+)/)
     if (match) row.editContentId = parseInt(match[1])
  } else if (row.targetUrl.includes('/pages/notice/detail')) {
    // 兼容旧路径
    row.editBusinessType = 'notice'
    const match = row.targetUrl.match(/id=(\d+)/)
    if (match) row.editContentId = parseInt(match[1])
  } else if (row.targetUrl.includes('/pages/activity/detail')) {
    // 兼容旧路径
    row.editBusinessType = 'activity'
    const match = row.targetUrl.match(/id=(\d+)/)
    if (match) row.editContentId = parseInt(match[1])
  } else if (row.targetUrl.includes('/pages/pet/detail')) {
    row.editBusinessType = 'pet'
    const match = row.targetUrl.match(/id=(\d+)/)
    if (match) row.editContentId = parseInt(match[1])
  } else {
    row.editBusinessType = 'none'
  }

  // 尝试获取Label
  if (row.editContentId && row.editBusinessType !== 'none') {
    fetchDetailForRow(row, row.editBusinessType, row.editContentId)
  }
}

const handleCancelRow = (row) => {
  row.isEditing = false
}

const handleSaveRow = async (row) => {
  try {
    // 构建更新数据
    const updateData = {
      id: row.id,
      imgUrl: row.imgUrl,
      sortOrder: row.editSortOrder,
      status: row.editStatus,
      targetUrl: row.targetUrl // 默认保持不变
    }

    // 根据编辑的类型重新生成 targetUrl
    // 只有当类型改变或者ID改变时，或者原本就是编辑状态，我们重新生成覆盖
    if (row.editBusinessType === 'none') {
      updateData.targetUrl = ''
    } else if (row.editBusinessType === 'wallpaper') {
      updateData.targetUrl = '' // 壁纸不跳转
    } else if (row.editContentId) {
      // 按照新需求生成路径
      if (row.editBusinessType === 'notice') {
        updateData.targetUrl = `/pages/wiki/detail?id=${row.editContentId}` // 需求变更为 wiki/detail
      } else if (row.editBusinessType === 'activity') {
        updateData.targetUrl = `/pages/wiki/detail?id=${row.editContentId}` // 需求变更为 wiki/detail
      } else if (row.editBusinessType === 'article') {
        updateData.targetUrl = `/pages/wiki/detail?id=${row.editContentId}`
      } else if (row.editBusinessType === 'pet') {
         // 宠物路径未变
        updateData.targetUrl = `/pages/pet/detail?id=${row.editContentId}`
      }
    }
    
    // 如果用户没有修改类型且没有修改ID，但是修改了排序或状态，我们应该保留原有的 targetUrl
    // 但上面的逻辑已经覆盖了 targetUrl 的生成。
    // 注意：如果 row.editContentId 为空（比如用户切了类型但没选内容），那么 targetUrl 可能会错。
    // 应该加个校验：如果不是 none/wallpaper，必须选内容
    if (row.editBusinessType !== 'none' && row.editBusinessType !== 'wallpaper' && !row.editContentId) {
       ElMessage.warning('请选择具体内容')
       return
    }

    await updateBanner(updateData)
    ElMessage.success('更新成功')
    row.isEditing = false
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

const handleRowTypeChange = (row, val) => {
  row.editContentId = null
  row.contentOptions = []
  if (val !== 'none' && val !== 'wallpaper') {
     searchRowContent(row, '')
  }
}

const searchRowContent = async (row, query) => {
  if (row.editBusinessType === 'none' || row.editBusinessType === 'wallpaper') return
  
  row.searchLoading = true
  try {
    let res
    if (row.editBusinessType === 'notice') {
      // 公告 type=1
      res = await getArticleList({ pageNum: 1, pageSize: 20, type: 1, keyword: query })
    } else if (row.editBusinessType === 'activity') {
      // 活动 type=2
      res = await getArticleList({ pageNum: 1, pageSize: 20, type: 2, keyword: query })
    } else if (row.editBusinessType === 'article') {
      // 文章 type=0 (百科)
      res = await getArticleList({ pageNum: 1, pageSize: 20, type: 0, keyword: query })
    } else if (row.editBusinessType === 'pet') {
      // 宠物
      res = await getPetList({ pageNum: 1, pageSize: 20, keyword: query })
    }

    if (res && res.code === 200) {
      if (row.editBusinessType === 'pet') {
        row.contentOptions = res.data.records.map(item => ({
          id: item.id,
          label: item.name
        }))
      } else {
        row.contentOptions = res.data.records.map(item => ({
          id: item.id,
          label: item.title
        }))
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    row.searchLoading = false
  }
}

const handleRowContentChange = (row, val) => {
  // 可以在这里实时预览生成的 URL
}

const fetchDetailForRow = async (row, type, id) => {
   try {
     let res
     let label = `ID:${id}`
     // 注意：这里用 getArticleDetail 获取标题用于回显
     // 不管是 notice/activity/article 都是 article 详情
     if (['notice', 'activity', 'article'].includes(type)) {
       res = await getArticleDetail(id)
       if (res.code === 200) label = res.data.title
     } else if (type === 'pet') {
       res = await getPetDetail(id)
       if (res.code === 200) label = res.data.name
     }
     
     const exists = row.contentOptions.find(item => item.id === id)
     if (!exists) {
       row.contentOptions.push({ id, label })
     }
   } catch (e) {
     console.error(e)
   }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该轮播图吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBanner(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleBusinessTypeChange = () => {
  selectedContentId.value = null
  contentOptions.value = []
  if (businessType.value === 'none') {
    formData.targetUrl = ''
  } else if (businessType.value === 'wallpaper') {
    formData.targetUrl = '' // 壁纸不跳转
  } else {
    formData.targetUrl = ''
    // 切换类型时，自动加载一次列表
    searchContent('')
  }
}

const handleContentChange = (val) => {
  if (!val) return
  if (businessType.value === 'notice') {
    formData.targetUrl = `/pages/wiki/detail?id=${val}`
  } else if (businessType.value === 'activity') {
    formData.targetUrl = `/pages/wiki/detail?id=${val}`
  } else if (businessType.value === 'article') {
    formData.targetUrl = `/pages/wiki/detail?id=${val}`
  } else if (businessType.value === 'pet') {
    formData.targetUrl = `/pages/pet/detail?id=${val}`
  }
}

const searchContent = async (query) => {
  if (businessType.value === 'none' || businessType.value === 'wallpaper') return
  
  searchLoading.value = true
  try {
    let res
    if (businessType.value === 'notice') {
      // 公告
      res = await getArticleList({ pageNum: 1, pageSize: 20, type: 1, keyword: query })
    } else if (businessType.value === 'activity') {
      // 社区活动
      res = await getArticleList({ pageNum: 1, pageSize: 20, type: 2, keyword: query })
    } else if (businessType.value === 'article') {
      // 养宠百科
      res = await getArticleList({ pageNum: 1, pageSize: 20, type: 0, keyword: query })
    } else if (businessType.value === 'pet') {
      // 宠物详情
      res = await getPetList({ pageNum: 1, pageSize: 20, keyword: query })
    }

    if (res && res.code === 200) {
      if (businessType.value === 'pet') {
        contentOptions.value = res.data.records.map(item => ({
          id: item.id,
          label: item.name
        }))
      } else {
        contentOptions.value = res.data.records.map(item => ({
          id: item.id,
          label: item.title
        }))
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    searchLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (businessType.value !== 'none' && businessType.value !== 'wallpaper' && !selectedContentId.value) {
         ElMessage.warning('请选择具体内容')
         return
      }
      
      try {
        await addBanner(formData)
        ElMessage.success('新增成功')
        dialogVisible.value = false
        fetchList()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.banner-list-container {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 5px;
}
</style>
