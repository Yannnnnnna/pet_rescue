<template>
  <div class="pet-list-container">

    <el-card class="box-card">
      <!-- 筛选区域 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="关键字">
            <el-input v-model="queryParams.keyword" placeholder="昵称/品种" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="queryParams.city" placeholder="城市" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 120px">
              <el-option label="猫" :value="0" />
              <el-option label="狗" :value="1" />
              <el-option label="鸟类" :value="2" />
              <el-option label="异宠" :value="3" />
              <el-option label="其他" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
              <el-option label="待领养" :value="0" />
              <el-option label="申请中" :value="1" />
              <el-option label="已领养" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            <el-button type="success" :icon="Plus" @click="handleAdd">新增宠物</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="petList"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="封面图" width="120" align="center">
          <template #default="scope">
            <el-image
              style="width: 80px; height: 80px; border-radius: 8px"
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
        <el-table-column prop="name" label="昵称" align="center" />
        <el-table-column prop="breed" label="品种" align="center" />
        <el-table-column label="性别" width="80" align="center">
          <template #default="scope">
            <el-tag :type="getSexTagType(scope.row.sex)" effect="plain">
              {{ getSexLabel(scope.row.sex) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" effect="dark">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" align="center" />
        <el-table-column prop="publisherId" label="发布人ID" width="100" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button type="info" link :icon="View" @click="handleView(scope.row)">查看</el-button>
            <el-button type="warning" link :icon="ChatDotRound" @click="handleConsultation(scope.row)">咨询记录</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="700px"
      destroy-on-close
      class="pet-dialog"
      top="5vh"
    >
      <el-form
        ref="petFormRef"
        :model="petForm"
        :rules="petRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="昵称" prop="name">
              <el-input v-model="petForm.name" placeholder="请输入宠物昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
             <el-form-item label="类型" prop="type">
              <el-select v-model="petForm.type" placeholder="请选择类型" style="width: 100%">
                <el-option label="猫" :value="0" />
                <el-option label="狗" :value="1" />
                <el-option label="鸟类" :value="2" />
                <el-option label="异宠" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品种" prop="breed">
              <el-input v-model="petForm.breed" placeholder="请输入品种" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model="petForm.age" placeholder="例如: 3个月" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="petForm.sex">
                <el-radio :label="0">母</el-radio>
                <el-radio :label="1">公</el-radio>
                <el-radio :label="2">未知</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="petForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="待领养" :value="0" />
                <el-option label="申请中" :value="1" />
                <el-option label="已领养" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
             <el-form-item label="是否绝育" prop="isSterilized">
              <el-radio-group v-model="petForm.isSterilized">
                <el-radio :label="0">否</el-radio>
                <el-radio :label="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否接种" prop="isVaccinated">
              <el-radio-group v-model="petForm.isVaccinated">
                <el-radio :label="0">否</el-radio>
                <el-radio :label="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="标签" prop="tags">
          <el-input v-model="petForm.tags" placeholder="多个标签用逗号分隔，如: 粘人,可爱" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
             <el-form-item label="所在地区">
              <el-cascader
                v-model="selectedLocation"
                :options="provinceCityOptions"
                placeholder="请选择省/市"
                style="width: 100%"
                @change="handleLocationChange"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
             <el-form-item label="详细地址" prop="address">
              <el-input v-model="petForm.address" placeholder="详细地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="封面图" prop="coverImg">
          <el-upload
            class="avatar-uploader"
            action="/api/file/upload"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders"
          >
            <img v-if="petForm.coverImg" :src="petForm.coverImg" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸: 1:1, 支持 jpg/png, 小于 5MB</div>
        </el-form-item>

        <el-form-item label="详情图" prop="detailImgList">
          <el-upload
            action="/api/file/upload"
            list-type="picture-card"
            :file-list="fileList"
            :on-success="handleDetailSuccess"
            :on-remove="handleRemove"
            :on-preview="handlePreview"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <el-dialog v-model="previewVisible">
            <img w-full :src="previewImage" alt="Preview Image" style="width: 100%" />
          </el-dialog>
        </el-form-item>

        <el-form-item label="详细介绍" prop="description">
          <el-input
            v-model="petForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入救助故事或详细介绍"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="萌宠档案"
      width="600px"
      class="pet-detail-dialog"
      destroy-on-close
      center
    >
      <div v-if="petDetail" class="detail-content">
        <!-- 头部信息 -->
        <div class="detail-header">
          <div class="detail-cover-wrapper">
            <el-image
              :src="petDetail.coverImg"
              class="detail-cover"
              fit="cover"
              :preview-src-list="[petDetail.coverImg]"
              preview-teleported
            />
          </div>
          <div class="detail-basic">
            <div class="detail-title-row">
              <h3 class="pet-name">{{ petDetail.name }}</h3>
              <el-tag :type="getSexTagType(petDetail.sex)" effect="dark" round class="sex-tag">
                <el-icon v-if="petDetail.sex === 1"><Male /></el-icon>
                <el-icon v-else-if="petDetail.sex === 0"><Female /></el-icon>
                {{ getSexLabel(petDetail.sex) }}
              </el-tag>
            </div>
            
            <div class="detail-badges">
               <el-tag effect="plain" type="warning" round>{{ petDetail.breed }}</el-tag>
               <el-tag effect="plain" type="info" round>{{ petDetail.age || '年龄未知' }}</el-tag>
               <el-tag :type="getStatusTagType(petDetail.status)" effect="light" round>
                  {{ getStatusLabel(petDetail.status) }}
               </el-tag>
            </div>
            
            <div class="detail-location">
               <el-icon><Location /></el-icon>
               {{ petDetail.city }} {{ petDetail.address }}
            </div>
          </div>
        </div>

        <!-- 详细属性 -->
        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="编号">#{{ petDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            {{ ['猫', '狗', '鸟类', '异宠', '其他'][petDetail.type] || '其他' }}
          </el-descriptions-item>
          <el-descriptions-item label="发布人ID">{{ petDetail.publisherId }}</el-descriptions-item>
          <el-descriptions-item label="健康状况">
             <el-space>
                <el-tag size="small" :type="petDetail.isSterilized ? 'success' : 'info'">
                  {{ petDetail.isSterilized ? '已绝育' : '未绝育' }}
                </el-tag>
                <el-tag size="small" :type="petDetail.isVaccinated ? 'success' : 'info'">
                  {{ petDetail.isVaccinated ? '已接种' : '未接种' }}
                </el-tag>
             </el-space>
          </el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <template v-if="petDetail.tags">
              <el-tag 
                v-for="tag in petDetail.tags.split(/[,，]/)" 
                :key="tag" 
                class="mx-1" 
                size="small" 
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </template>
            <span v-else class="text-gray">暂无标签</span>
          </el-descriptions-item>
          <el-descriptions-item label="救助故事" :span="2">
            <div class="description-text">{{ petDetail.description || '暂无详细介绍' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 更多图片 -->
        <div v-if="petDetail.detailImgList && petDetail.detailImgList.length" class="detail-images-section">
          <div class="section-title">
             <span>更多萌照</span>
             <div class="title-line"></div>
          </div>
          <div class="image-grid">
             <el-image
              v-for="(img, index) in petDetail.detailImgList"
              :key="index"
              :src="img"
              class="detail-grid-img"
              fit="cover"
              :preview-src-list="petDetail.detailImgList"
              :initial-index="index"
              preview-teleported
            />
          </div>
        </div>

  </div>
    </el-dialog>

    <!-- 咨询记录抽屉 -->
    <el-drawer
      v-model="consultationVisible"
      title="咨询记录"
      size="800px"
      direction="rtl"
      destroy-on-close
    >
      <div class="consultation-container">
        <!-- 左侧用户列表 -->
        <div class="consultation-sidebar">
          <div class="sidebar-header">咨询用户 ({{ consultationUsers.length }})</div>
          <div class="user-list">
            <div 
              v-for="user in consultationUsers" 
              :key="user.askUserId" 
              class="user-item"
              :class="{ active: currentConsultationUser?.askUserId === user.askUserId }"
              @click="handleSelectConsultationUser(user)"
            >
              <el-avatar :size="40" :src="user.askUserAvatar || ''" shape="square">
                <template #error><el-icon><UserFilled /></el-icon></template>
              </el-avatar>
              <div class="user-info">
                <div class="user-name">{{ user.askUserNickname || '未知用户' }}</div>
                <div class="last-time">{{ formatTime(user.lastTime) }}</div>
              </div>
              <el-badge :value="user.msgCount" class="msg-count" type="danger" v-if="user.msgCount > 0" />
            </div>
            <el-empty v-if="consultationUsers.length === 0" description="暂无咨询记录" />
          </div>
        </div>
        
        <!-- 右侧聊天记录 -->
        <div class="consultation-main">
          <div class="chat-header" v-if="currentConsultationUser">
            <span>与 {{ currentConsultationUser.askUserNickname }} 的对话</span>
          </div>
          <div class="chat-content" v-loading="historyLoading" ref="chatContentRef">
            <template v-if="currentConsultationUser">
              <div 
                v-for="msg in chatHistory" 
                :key="msg.id" 
                class="chat-message"
                :class="{ 'message-mine': msg.replyUserId, 'message-other': msg.askUserId }"
              >
                <!-- 注意：这里逻辑有点绕，根据API定义：
                     askUserId: 提问者ID
                     replyUserId: 回答者ID (发布人)
                     
                     如果这条消息有 replyUserId，说明是发布者(owner)回复的 -> 右边 (或左边？管理员视角通常看 owner 和 user 对话)
                     管理员视角：
                     - 提问者(user) -> 左边
                     - 回答者(owner) -> 右边
                     
                     msg.answer 存在时，通常是 owner 回复的。
                     msg.question 存在时，通常是 user 提问的。
                     
                     接口返回的是 array<PetConsultation>，每个对象包含 question 和 answer。
                     通常这种问答式咨询，一条记录包含一问一答。
                     所以我们需要把它们拆解成时间流展示？
                     或者接口返回的就是单条消息记录？
                     
                     看接口字段：
                     id, petId, askUserId, replyUserId, question, answer, status, createTime, replyTime
                     
                     这看起来像是一条记录包含 "提问" 和 "回答" (如果有)。
                     所以展示逻辑应该是：
                     1. 显示提问 (askUserId, createTime, question) -> 左边
                     2. 如果有回答 (answer, replyTime) -> 显示回答 -> 右边
                -->
                
                <div class="message-group">
                   <!-- 提问 (用户) -->
                   <div class="message-row other">
                      <el-avatar :size="30" :src="currentConsultationUser.askUserAvatar" />
                      <div class="message-bubble">
                         <div class="message-text">{{ msg.question }}</div>
                         <div class="message-time">{{ formatTime(msg.createTime) }}</div>
                      </div>
                   </div>
                   
                   <!-- 回复 (送养人) -->
                   <div class="message-row mine" v-if="msg.answer">
                      <div class="message-bubble">
                         <div class="message-text">{{ msg.answer }}</div>
                         <div class="message-time">{{ formatTime(msg.replyTime) }}</div>
                      </div>
                      <el-avatar :size="30" :src="currentConsultationPet?.coverImg" /> <!-- 用宠物头像代表送养人 -->
                   </div>
                </div>
              </div>
              <el-empty v-if="chatHistory.length === 0" description="暂无对话" />
            </template>
            <el-empty v-else description="请选择左侧用户查看详情" />
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { Search, Refresh, Plus, Edit, Delete, Picture, View, Male, Female, Location, ChatDotRound, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pcTextArr } from 'element-china-area-data'
import { getPetList, addPet, updatePet, removePet, getPetDetail } from '../../api/pet'
import { getConsultationSummary, getConsultationHistory } from '../../api/consultation'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

// --- 数据定义 ---
const loading = ref(false)
const petList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  city: '',
  type: null,
  status: null
})

// --- 弹窗相关 ---
const dialogVisible = ref(false)
const dialogTitle = ref('新增宠物')
const petFormRef = ref(null)
const submitLoading = ref(false)

// 初始表单数据
const initialForm = {
  id: null,
  name: '',
  type: 0,
  breed: '',
  age: '',
  sex: 2,
  status: 0,
  coverImg: '',
  detailImgList: [],
  tags: '',
  description: '',
  isSterilized: 0,
  isVaccinated: 0,
  province: '',
  city: '',
  address: ''
}

const petForm = reactive({ ...initialForm })

// 详情弹窗数据
const detailVisible = ref(false)
const petDetail = ref(null)

// 地区选择
const selectedLocation = ref([])

// 处理省市数据，确保直辖市的二级选项与一级相同（满足后端需求）
const provinceCityOptions = pcTextArr.map(province => {
  const directCities = ['北京市', '天津市', '上海市', '重庆市']
  if (directCities.includes(province.label)) {
    return {
      ...province,
      children: [
        { value: province.label, label: province.label }
      ]
    }
  }
  return province
})

// 图片上传列表 (用于详情图展示)
const fileList = ref([])
const previewVisible = ref(false)
const previewImage = ref('')

const petRules = {
  name: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  coverImg: [{ required: true, message: '请上传封面图', trigger: 'change' }]
}

// 上传相关
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// --- 方法 ---

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPetList(queryParams)
    if (res.code === 200) {
      petList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('Fetch list error:', error)
  } finally {
    loading.value = false
  }
}

// 搜索与重置
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.city = ''
  queryParams.type = null
  queryParams.status = null
  handleSearch()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增宠物'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑宠物'
  // 填充表单数据
  Object.assign(petForm, row)
  
  // 处理详情图列表回显
  // 优先读取 detailImgs (字符串), 兼容 detailImgList
  let imgs = []
  const sourceImgs = row.detailImgs || row.detailImgList

  if (Array.isArray(sourceImgs)) {
    imgs = sourceImgs
  } else if (sourceImgs && typeof sourceImgs === 'string') {
    try {
      // 尝试 JSON 解析
      imgs = JSON.parse(sourceImgs)
    } catch (e) {
      // 逗号分隔
      imgs = sourceImgs.split(',').filter(item => item)
    }
  }
  
  // 确保是数组
  if (!Array.isArray(imgs)) imgs = []

  petForm.detailImgList = imgs
  
  // 生成 fileList 用于 Upload 组件显示
  fileList.value = imgs.map((url, index) => ({
    name: `img-${index}`,
    url: url
  }))
  
  // 回显地区
  if (row.province && row.city) {
    selectedLocation.value = [row.province, row.city]
  } else {
    selectedLocation.value = []
  }

  dialogVisible.value = true
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await getPetDetail(row.id)
    if (res.code === 200) {
      const data = res.data
      
      // 处理图片列表兼容性
      const sourceImgs = data.detailImgs || data.detailImgList
      let imgs = []

      if (Array.isArray(sourceImgs)) {
        imgs = sourceImgs
      } else if (sourceImgs && typeof sourceImgs === 'string') {
         try {
           imgs = JSON.parse(sourceImgs)
         } catch (e) {
           imgs = sourceImgs.split(',').filter(item => item)
         }
      }
      
      data.detailImgList = Array.isArray(imgs) ? imgs : []
      
      petDetail.value = data
      detailVisible.value = true
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  } catch (error) {
    console.error('Fetch detail error:', error)
    ElMessage.error('获取详情失败')
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除宠物 "${row.name}" 吗?`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        // row.id 已经是 BigInt 类型 (如果用了 json-bigint)，需要转字符串传给后端
        // 或者 axios 会自动处理，只要 URL 正确
        const res = await removePet(row.id.toString())
        if (res.code === 200) {
          ElMessage.success('删除成功')
          fetchList()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error('Delete error:', error)
      }
    })
    .catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!petFormRef.value) return
  await petFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const isEdit = !!petForm.id
        const api = isEdit ? updatePet : addPet
        
        // 构建提交数据
        const submitData = { ...petForm }
        
        // 将图片数组转换为逗号分隔的字符串
        if (Array.isArray(submitData.detailImgList) && submitData.detailImgList.length > 0) {
          submitData.detailImgs = submitData.detailImgList.join(',')
        } else {
          submitData.detailImgs = ''
        }
        // 如果后端不接受 detailImgList 字段，可以删除
        // delete submitData.detailImgList

        const res = await api(submitData)
        
        if (res.code === 200) {
          ElMessage.success(isEdit ? '修改成功' : '新增成功')
          dialogVisible.value = false
          fetchList()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('Submit error:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(petForm, initialForm)
  // 清空数组
  petForm.detailImgList = []
  selectedLocation.value = []
  fileList.value = []
  if (petFormRef.value) {
    petFormRef.value.clearValidate()
  }
}

// 封面图上传成功
const handleCoverSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    petForm.coverImg = response.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 详情图上传成功
const handleDetailSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    const url = response.data
    petForm.detailImgList.push(url)
    // 更新 fileList 保持同步（Element Plus 会自动添加，但我们需要确保 url 正确）
    // 这里其实不需要手动 push 到 fileList，因为 Upload 组件会自动维护
    // 但我们需要更新 petForm.detailImgList
    ElMessage.success('上传成功')
  } else {
    // 上传失败，从 fileList 中移除该文件
    const index = fileList.value.indexOf(uploadFile)
    if (index !== -1) {
      fileList.value.splice(index, 1)
    }
    ElMessage.error(response.msg || '上传失败')
  }
}

// 移除详情图
const handleRemove = (uploadFile, uploadFiles) => {
  // 重新计算 detailImgList
  // uploadFiles 是当前剩余的文件列表
  // 注意：如果是回显的图片，url 是直接可用的；如果是新上传的，response 中有 url
  petForm.detailImgList = uploadFiles.map(file => {
    if (file.response && file.response.code === 200) {
      return file.response.data
    }
    return file.url
  })
}

// 预览详情图
const handlePreview = (uploadFile) => {
  previewImage.value = uploadFile.url
  previewVisible.value = true
}

// 图片上传前校验
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('图片格式必须是 JPG 或 PNG!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 20) {
    ElMessage.error('图片大小不能超过 20MB!')
    return false
  }
  return true
}

// 地区选择变化
const handleLocationChange = (val) => {
  if (val && val.length > 0) {
    petForm.province = val[0]
    petForm.city = val[1] || ''
  } else {
    petForm.province = ''
    petForm.city = ''
  }
}

// --- 咨询记录相关 ---
const consultationVisible = ref(false)
const consultationUsers = ref([])
const currentConsultationUser = ref(null)
const currentConsultationPet = ref(null)
const chatHistory = ref([])
const historyLoading = ref(false)
const chatContentRef = ref(null)

// 打开咨询记录
const handleConsultation = async (row) => {
  currentConsultationPet.value = row
  consultationVisible.value = true
  consultationUsers.value = []
  currentConsultationUser.value = null
  chatHistory.value = []
  
  try {
    const res = await getConsultationSummary(row.id)
    if (res.code === 200) {
      consultationUsers.value = res.data || []
      // 默认选中第一个
      if (consultationUsers.value.length > 0) {
        handleSelectConsultationUser(consultationUsers.value[0])
      }
    }
  } catch (error) {
    console.error('Fetch consultation summary error:', error)
    ElMessage.error('获取咨询列表失败')
  }
}

// 选择咨询用户
const handleSelectConsultationUser = async (user) => {
  currentConsultationUser.value = user
  historyLoading.value = true
  chatHistory.value = []
  
  try {
    // user.askUserId 是提问者的ID
    const res = await getConsultationHistory(currentConsultationPet.value.id, user.askUserId)
    if (res.code === 200) {
      chatHistory.value = res.data || []
      scrollToBottom()
    }
  } catch (error) {
    console.error('Fetch history error:', error)
    ElMessage.error('获取聊天记录失败')
  } finally {
    historyLoading.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    if (chatContentRef.value) {
      chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
    }
  }, 100)
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

// 辅助函数
const getSexLabel = (val) => {
  const map = { 0: '母', 1: '公', 2: '未知' }
  return map[val] || '未知'
}

const getSexTagType = (val) => {
  const map = { 0: 'danger', 1: '', 2: 'info' }
  return map[val] || 'info'
}

const getStatusLabel = (val) => {
  const map = { 0: '待领养', 1: '申请中', 2: '已领养' }
  return map[val] || '未知'
}

const getStatusTagType = (val) => {
  const map = { 0: 'success', 1: 'warning', 2: 'info' }
  return map[val] || 'info'
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.pet-list-container {
  padding: 0;
}

.box-card {
  border-radius: 8px;
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

/* 弹窗样式调整 */
.pet-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 10px;
}

/* 上传组件样式 */
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.2;
}

/* 详情弹窗样式 */
.pet-detail-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.pet-detail-dialog .el-dialog__header {
  margin-right: 0;
  padding: 20px;
  background-color: #fdf6ec;
  border-bottom: 1px solid #faecd8;
}

.pet-detail-dialog .el-dialog__title {
  color: #d46b08;
  font-weight: bold;
}

.detail-content {
  padding: 0 10px;
}

.detail-header {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.detail-cover-wrapper {
  flex-shrink: 0;
  width: 140px;
  height: 140px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 4px solid #fff;
}

.detail-cover {
  width: 100%;
  height: 100%;
}

.detail-basic {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.pet-name {
  margin: 0;
  font-size: 24px;
  color: #303133;
  font-weight: 700;
}

.sex-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-location {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
}

.detail-descriptions {
  margin-bottom: 24px;
}

.detail-descriptions :deep(.el-descriptions__label) {
  width: 80px;
  justify-content: flex-end;
}

.description-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #606266;
}

.detail-images-section {
  margin-top: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-weight: bold;
  color: #303133;
}

.title-line {
  flex: 1;
  height: 1px;
  background: #ebeef5;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.detail-grid-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.detail-grid-img:hover {
  transform: scale(1.05);
}

.text-gray {
  color: #909399;
}

.mx-1 {
  margin-left: 0.25rem;
  margin-right: 0.25rem;
}

/* 咨询记录样式 */
.consultation-container {
  display: flex;
  height: calc(100vh - 120px); /* 减去抽屉header的高度 */
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.consultation-sidebar {
  width: 250px;
  border-right: 1px solid #e4e7ed;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 15px;
  font-weight: bold;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.user-list {
  flex: 1;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.user-item:hover {
  background-color: #e6f7ff;
}

.user-item.active {
  background-color: #bae7ff;
}

.user-info {
  margin-left: 10px;
  flex: 1;
  overflow: hidden;
}

.user-name {
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.last-time {
  font-size: 12px;
  color: #909399;
}

.msg-count {
  margin-left: 5px;
}

.consultation-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.chat-header {
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: bold;
  background-color: #fff;
}

.chat-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f5f5;
}

.chat-message {
  margin-bottom: 20px;
}

.message-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  max-width: 80%;
}

.message-row.other {
  align-self: flex-start;
}

.message-row.mine {
  align-self: flex-end;
}

.message-bubble {
  margin: 0 10px;
  padding: 10px 15px;
  border-radius: 8px;
  position: relative;
  word-break: break-all;
}

.message-row.other .message-bubble {
  background-color: #fff;
  border-top-left-radius: 0;
}

.message-row.mine .message-bubble {
  background-color: #95ec69;
  border-top-right-radius: 0;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  text-align: right;
}
</style>
