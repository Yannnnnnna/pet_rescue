<template>
  <div class="article-editor-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑文章' : '发布文章' }}</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <el-form
        ref="articleFormRef"
        :model="articleForm"
        :rules="rules"
        label-width="120px"
        class="article-form"
      >
        <!-- 基础信息区域 -->
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="文章标题" prop="title">
              <el-input v-model="articleForm.title" placeholder="请输入标题" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="8">
             <el-form-item label="文章类型" prop="type">
              <el-select v-model="articleForm.type" placeholder="请选择类型" style="width: 100%" @change="handleTypeChange">
                <el-option label="养宠百科" :value="0" />
                <el-option label="救助公告" :value="1" />
                <el-option label="活动" :value="2" />
                <el-option label="壁纸" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="articleForm.category" placeholder="例如: 疾病医疗, 喂养指南, 萌宠壁纸" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tags">
               <el-input v-model="articleForm.tags" placeholder="多个标签用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 封面图 (根据类型显示不同Label) -->
        <el-form-item :label="coverLabel" prop="coverImg">
          <el-upload
            class="cover-uploader"
            action="/api/file/upload"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            :headers="uploadHeaders"
          >
            <img v-if="articleForm.coverImg" :src="articleForm.coverImg" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸: {{ articleForm.type === 3 ? '1:1 (列表缩略图)' : '16:9 (文章封面/活动海报)' }}, 支持 jpg/png, 小于 20MB</div>
        </el-form-item>

        <!-- 壁纸特有字段 -->
        <template v-if="articleForm.type === 3">
          <el-form-item label="高清壁纸" prop="wallpaperUrl">
             <el-upload
              class="wallpaper-uploader"
              action="/api/file/upload"
              :show-file-list="false"
              :on-success="handleWallpaperSuccess"
              :before-upload="beforeWallpaperUpload"
              :headers="uploadHeaders"
            >
              <img v-if="articleForm.wallpaperUrl" :src="articleForm.wallpaperUrl" class="wallpaper-image" />
              <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
            </el-upload>
             <div class="upload-tip">请上传高清原图，用于用户下载 (小于 50MB)</div>
          </el-form-item>
        </template>

        <!-- 活动特有字段 -->
        <template v-if="articleForm.type === 2">
          <el-form-item label="活动时间" prop="activityTimeRange">
            <el-date-picker
              v-model="articleForm.activityTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="活动地点" prop="activityAddress">
             <el-input v-model="articleForm.activityAddress" placeholder="请输入活动详细地址" />
          </el-form-item>
        </template>

        <!-- 摘要 (百科/公告显示) -->
        <el-form-item v-if="[0, 1].includes(articleForm.type)" label="摘要" prop="summary">
          <el-input
            v-model="articleForm.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要（列表页展示），选填"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 富文本编辑器 (壁纸类型不显示) -->
        <el-form-item v-if="articleForm.type !== 3" label="正文内容" prop="content">
          <div style="border: 1px solid #ccc; width: 100%">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              :mode="mode"
            />
            <Editor
              style="height: 500px; overflow-y: hidden;"
              v-model="articleForm.content"
              :defaultConfig="editorConfig"
              :mode="mode"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">保存发布</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, shallowRef, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getArticleDetail, saveArticle } from '../../api/article'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const router = useRouter()
const route = useRoute()

// WangEditor Setup
const editorRef = shallowRef()
const mode = 'default'
const toolbarConfig = {}
const editorConfig = { 
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
       server: '/api/file/upload',
       fieldName: 'file',
       maxFileSize: 20 * 1024 * 1024, // 20MB
       customInsert(res, insertFn) {
          if (res.code === 200) {
             insertFn(res.data, 'image', res.data)
          } else {
             ElMessage.error(res.msg || '上传图片失败')
          }
       }
    }
  }
}

const articleFormRef = ref(null)
const submitLoading = ref(false)
const isEdit = computed(() => !!route.params.id)

// Form Data
const articleForm = reactive({
  id: undefined,
  title: '',
  type: 0,
  category: '',
  tags: '',
  coverImg: '',
  summary: '',
  content: '',
  // Activity Fields
  activityTimeRange: [], // [startTime, endTime]
  activityAddress: '',
  // Wallpaper Fields
  wallpaperUrl: ''
})

// Dynamic Labels
const coverLabel = computed(() => {
  if (articleForm.type === 2) return '活动海报'
  if (articleForm.type === 3) return '列表缩略图'
  return '封面图'
})

// Dynamic Rules
const rules = computed(() => {
  const common = {
    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
    type: [{ required: true, message: '请选择类型', trigger: 'change' }],
    coverImg: [{ required: true, message: '请上传图片', trigger: 'change' }],
  }

  // Activity
  if (articleForm.type === 2) {
    return {
      ...common,
      activityTimeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
      activityAddress: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
      content: [{ required: true, message: '请输入活动详情', trigger: 'blur' }]
    }
  }

  // Wallpaper
  if (articleForm.type === 3) {
    return {
      ...common,
      wallpaperUrl: [{ required: true, message: '请上传高清壁纸', trigger: 'change' }]
    }
  }

  // Article (0, 1)
  return {
    ...common,
    content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
  }
})

// Auth Header
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: token } : {}
})

// Lifecycle
onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
})

const handleCreated = (editor) => {
  editorRef.value = editor
}

// Handlers
const handleTypeChange = () => {
  // Clear validation when switching types to avoid sticking error messages for hidden fields
  if (articleFormRef.value) {
    articleFormRef.value.clearValidate()
  }
}

const handleCoverSuccess = (response) => {
  if (response.code === 200) {
    articleForm.coverImg = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleWallpaperSuccess = (response) => {
  if (response.code === 200) {
    articleForm.wallpaperUrl = response.data
    ElMessage.success('壁纸上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const beforeCoverUpload = (rawFile) => {
  const isLt20M = rawFile.size / 1024 / 1024 < 20
  if (!isLt20M) {
    ElMessage.error('图片大小不能超过 20MB!')
    return false
  }
  return true
}

const beforeWallpaperUpload = (rawFile) => {
  const isLt50M = rawFile.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('壁纸大小不能超过 50MB!')
    return false
  }
  return true
}

const fetchDetail = async (id) => {
  try {
    const res = await getArticleDetail(id)
    if (res.code === 200) {
      const data = res.data
      Object.assign(articleForm, data)
      // Map activity time fields to range array
      if (data.type === 2 && data.activityStartTime && data.activityEndTime) {
        articleForm.activityTimeRange = [data.activityStartTime, data.activityEndTime]
      }
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('系统错误')
  }
}

const submitForm = async () => {
  if (!articleFormRef.value) return
  await articleFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      // Custom validation for Rich Text (except Wallpaper type)
      if (articleForm.type !== 3) {
        const editor = editorRef.value
        if (!editor || editor.isEmpty()) {
            ElMessage.warning('请输入正文内容')
            return
        }
      }

      submitLoading.value = true
      
      // Prepare Payload
      const payload = { ...articleForm }
      
      // 1. Handle Activity Time (Common logic)
      if (payload.type === 2 && articleForm.activityTimeRange?.length === 2) {
        payload.activityStartTime = articleForm.activityTimeRange[0]
        payload.activityEndTime = articleForm.activityTimeRange[1]
      } else {
        payload.activityStartTime = null
        payload.activityEndTime = null
      }
      
      // 2. Clean up fields based on Type to ensure data consistency
      if (payload.type === 0 || payload.type === 1) {
          // Article: Clear Activity, Wallpaper
          payload.activityAddress = null
          payload.wallpaperUrl = null
      } else if (payload.type === 2) {
          // Event: Clear Summary, Wallpaper
          payload.summary = ''
          payload.wallpaperUrl = null
      } else if (payload.type === 3) {
          // Wallpaper: Clear Summary, Content, Activity Address
          payload.summary = ''
          payload.content = ''
          payload.activityAddress = null
      }

      // Remove UI-only fields
      delete payload.activityTimeRange

      try {
        const res = await saveArticle(payload)
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
          goBack()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('系统错误')
      } finally {
        submitLoading.value = false
      }
    } else {
      console.log('error submit!', fields)
      ElMessage.warning('请检查表单填写')
    }
  })
}

const goBack = () => {
  router.push({ name: 'ArticleList' })
}

onMounted(() => {
  if (isEdit.value) {
    fetchDetail(route.params.id)
  }
})
</script>

<style scoped>
.article-editor-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.article-form {
  max-width: 1000px;
  margin: 0 auto;
}
.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 320px; /* Default width */
  height: 180px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.cover-uploader:hover {
  border-color: #409eff;
}
.wallpaper-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 350px; /* Portrait for wallpaper usually */
  display: flex;
  justify-content: center;
  align-items: center;
}
.wallpaper-uploader:hover {
   border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.wallpaper-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}
.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}
</style>
