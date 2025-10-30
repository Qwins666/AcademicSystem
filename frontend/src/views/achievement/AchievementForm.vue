<template>
  <div class="achievement-form">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑成果' : '申报成果' }}</h1>
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div class="form-container">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="成果标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入成果标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="成果类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择成果类型" style="width: 100%">
            <el-option label="论文" value="PAPER" />
            <el-option label="专利" value="PATENT" />
            <el-option label="证书" value="CERTIFICATE" />
            <el-option label="项目" value="PROJECT" />
          </el-select>
        </el-form-item>

        <el-form-item label="成果描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入成果描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="成果级别" prop="level">
          <el-select v-model="form.level" placeholder="请选择成果级别" style="width: 100%">
            <el-option label="国家级" value="NATIONAL" />
            <el-option label="省级" value="PROVINCIAL" />
            <el-option label="校级" value="SCHOOL" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="请输入标签，多个标签用逗号分隔"
            maxlength="500"
          />
        </el-form-item>

        <el-form-item label="成果文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :file-list="fileList"
            accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、Word、图片格式，文件大小不超过10MB
              </div>
            </template>
          </el-upload>
          <div v-if="selectedFile" class="file-info">
            <el-tag size="medium" type="success">已选择文件</el-tag>
            <span class="file-name">{{ selectedFile.name }}</span>
            <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '更新' : '提交' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createAchievement, updateAchievement, getAchievementById, uploadAchievementFile } from '@/api/achievement'

export default {
  name: 'AchievementForm',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const formRef = ref()
    const uploadRef = ref()
    const loading = ref(false)
    const fileList = ref([])
    const selectedFile = ref(null)
    
    const isEdit = computed(() => !!route.params.id)
    
    const form = reactive({
      title: '',
      type: '',
      description: '',
      level: '',
      tags: '',
      file: null,
      fileUrl: '',
      fileName: '',
      fileSize: 0
    })
    
    const rules = {
      title: [
        { required: true, message: '请输入成果标题', trigger: 'blur' },
        { min: 2, max: 200, message: '标题长度在2到200个字符', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择成果类型', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入成果描述', trigger: 'blur' },
        { min: 10, max: 1000, message: '描述长度在10到1000个字符', trigger: 'blur' }
      ],
      level: [
        { required: true, message: '请选择成果级别', trigger: 'change' }
      ]
    }
    
    const loadAchievementData = async () => {
      if (isEdit.value) {
        try {
          const achievementId = route.params.id
          const response = await getAchievementById(achievementId)
          if (response.data) {
            // 后端返回的是Map格式数据
            const achievementData = response.data
            // 复制所有字段，包括文件相关字段
            Object.assign(form, {
              title: achievementData.title || '',
              type: achievementData.type || '',
              description: achievementData.description || '',
              level: achievementData.level || '',
              tags: achievementData.tags || '',
              // 设置文件相关字段
              fileUrl: achievementData.fileUrl || '',
              fileName: achievementData.fileName || '',
              fileSize: achievementData.fileSize || 0
            })
            
            // 如果有文件信息，创建文件对象到fileList中
            if (achievementData.fileName) {
              const existingFile = {
                name: achievementData.fileName,
                size: achievementData.fileSize || 0,
                url: achievementData.fileUrl,
                status: 'success'
              }
              fileList.value = [existingFile]
            }
          }
        } catch (error) {
          console.error('加载成果数据失败:', error)
          ElMessage.error('加载成果数据失败')
        }
      }
    }
    
    const handleFileChange = (file, fileList) => {
      // 更新文件列表
      fileList.value = fileList
      // 只取最后一个文件作为选中文件
      if (file && file.raw) {
        selectedFile.value = file.raw
      } else if (fileList.length > 0) {
        // 如果有多个文件，取最后一个
        selectedFile.value = fileList[fileList.length - 1].raw
      }
    }
    
    const beforeUpload = (file) => {
      const isValidType = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'image/jpeg', 'image/png'].includes(file.type)
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isValidType) {
        ElMessage.error('只能上传 PDF、Word、图片格式的文件!')
        return false
      }
      if (!isLt10M) {
        ElMessage.error('文件大小不能超过 10MB!')
        return false
      }
      // 对于手动上传，我们需要返回true让文件进入文件列表
      return true
    }
    
    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    const handleSubmit = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          if (!selectedFile.value && !isEdit.value) {
            ElMessage.warning('请上传成果文件')
            return
          }
          
          loading.value = true
          try {
            // 准备提交数据
            const submitData = {
              title: form.title,
              type: form.type,
              description: form.description,
              level: form.level,
              tags: form.tags || ''
            }
            
            let result
            if (isEdit.value) {
              // 更新成果
              result = await updateAchievement(route.params.id, submitData)
              
              // 在编辑模式下，如果选择了新文件，也需要上传
              if (selectedFile.value) {
                try {
                  const formData = new FormData()
                  formData.append('file', selectedFile.value)
                  formData.append('achievementId', route.params.id)
                  
                  // 调用专门的文件上传接口
                  const uploadResult = await uploadAchievementFile(formData)
                  if (uploadResult.code === 200) {
                    ElMessage.success('成果更新成功，文件上传成功')
                  } else {
                    ElMessage.success('成果更新成功，但文件上传失败: ' + (uploadResult.msg || '未知错误'))
                  }
                } catch (uploadError) {
                  console.error('文件上传失败:', uploadError)
                  ElMessage.warning('成果更新成功，但文件上传失败，请稍后手动上传')
                }
              } else {
                ElMessage.success('成果更新成功')
              }
            } else {
              // 创建新成果
              result = await createAchievement(submitData)
              
              // 如果有文件且是新创建，上传文件
              if (selectedFile.value && result.data && result.data.id) {
                try {
                  const formData = new FormData()
                  formData.append('file', selectedFile.value)
                  formData.append('achievementId', result.data.id)
                  
                  // 调用专门的文件上传接口
                  const uploadResult = await uploadAchievementFile(formData)
                  if (uploadResult.code === 200) {
                    ElMessage.success('成果提交成功，文件上传成功')
                  } else {
                    ElMessage.success('成果提交成功，但文件上传失败: ' + (uploadResult.msg || '未知错误'))
                  }
                } catch (uploadError) {
                  console.error('文件上传失败:', uploadError)
                  ElMessage.warning('成果提交成功，但文件上传失败，请稍后手动上传')
                }
              } else {
                ElMessage.success('成果提交成功')
              }
            }
            
            router.push('/dashboard/achievements')
          } catch (error) {
            console.error('操作失败:', error)
            // 显示详细的错误信息
            const errorMsg = error.response?.data?.message || error.message || (isEdit.value ? '更新成果失败' : '提交成果失败')
            ElMessage.error(errorMsg)
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    const handleReset = () => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
      fileList.value = []
      selectedFile.value = null
      form.file = null
    }
    
    const handleBack = () => {
      router.push('/dashboard/achievements')
    }
    
    onMounted(() => {
      loadAchievementData()
    })
    
    return {
      formRef,
      uploadRef,
      loading,
      fileList,
      isEdit,
      form,
      rules,
      selectedFile,
      formatFileSize,
      handleFileChange,
      beforeUpload,
      handleSubmit,
      handleReset,
      handleBack
    }
  }
}
</script>

<style scoped>
.achievement-form {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
}

.form-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

.file-name {
  font-weight: 500;
  flex: 1;
}

.file-size {
  color: #606266;
  font-size: 14px;
}
</style>
