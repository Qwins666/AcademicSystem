<template>
  <div class="activity-form">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑活动' : '发布活动' }}</h1>
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
        <el-form-item label="活动标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入活动标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择活动类型" style="width: 100%">
            <el-option label="讲座" value="LECTURE" />
            <el-option label="赛事" value="COMPETITION" />
            <el-option label="培训" value="TRAINING" />
          </el-select>
        </el-form-item>

        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入活动描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="主办方" prop="organizer">
          <el-input
            v-model="form.organizer"
            placeholder="请输入主办方"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="活动地点" prop="location">
          <el-input
            v-model="form.location"
            placeholder="请输入活动地点"
            maxlength="200"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="报名截止时间" prop="registrationDeadline">
          <el-date-picker
            v-model="form.registrationDeadline"
            type="datetime"
            placeholder="选择报名截止时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="最大参与人数">
          <el-input-number
            v-model="form.maxParticipants"
            :min="1"
            :max="1000"
            placeholder="请输入最大参与人数"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="活动状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="DRAFT">草稿</el-radio>
            <el-radio label="PUBLISHED">发布</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '更新' : '发布' }}
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
import { createActivity, updateActivity, getActivityById } from '@/api/activity'

export default {
  name: 'ActivityForm',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const formRef = ref()
    const loading = ref(false)
    
    const isEdit = computed(() => !!route.params.id)
    
    const form = reactive({
      title: '',
      type: '',
      description: '',
      organizer: '',
      location: '',
      startTime: '',
      endTime: '',
      registrationDeadline: '',
      maxParticipants: 100,
      status: 'DRAFT'
    })
    
    const rules = {
      title: [
        { required: true, message: '请输入活动标题', trigger: 'blur' },
        { min: 2, max: 200, message: '标题长度在2到200个字符', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择活动类型', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入活动描述', trigger: 'blur' },
        { min: 10, max: 1000, message: '描述长度在10到1000个字符', trigger: 'blur' }
      ],
      organizer: [
        { required: true, message: '请输入主办方', trigger: 'blur' }
      ],
      location: [
        { required: true, message: '请输入活动地点', trigger: 'blur' }
      ],
      startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' }
      ],
      endTime: [
        { required: true, message: '请选择结束时间', trigger: 'change' }
      ],
      registrationDeadline: [
        { required: true, message: '请选择报名截止时间', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择活动状态', trigger: 'change' }
      ]
    }

    // 格式化日期为后端接受的格式
    const formatDateForBackend = (dateString) => {
      if (!dateString) return ''
      // 将 "2025-10-17T10:24:02" 转换为 "2025-10-17 10:24:02"
      return dateString.replace('T', ' ')
    }

    // 格式化日期为前端显示的格式
    const formatDateForFrontend = (dateString) => {
      if (!dateString) return ''
      // 将 "2025-10-17 10:24:02" 转换为 "2025-10-17T10:24:02"
      // 或者保持原样，如果已经是ISO格式
      if (dateString.includes('T')) {
        return dateString
      }
      return dateString.replace(' ', 'T')
    }
    
    const loadActivityData = async () => {
      if (isEdit.value) {
        try {
          loading.value = true
          const response = await getActivityById(route.params.id)
          if (response.code === 200) {
            // 处理日期时间格式 - 修改这里
            const data = { ...response.data }
            if (data.startTime) {
              data.startTime = formatDateForFrontend(data.startTime)
            }
            if (data.endTime) {
              data.endTime = formatDateForFrontend(data.endTime)
            }
            if (data.registrationDeadline) {
              data.registrationDeadline = formatDateForFrontend(data.registrationDeadline)
            }
            Object.assign(form, data)
          } else {
            ElMessage.error(response.message || '加载活动数据失败')
          }
        } catch (error) {
          ElMessage.error('加载活动数据失败，请稍后重试')
        } finally {
          loading.value = false
        }
      }
    }
    
    const handleSubmit = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          console.log("开始加载...")
          try {
            // 在提交前格式化日期，并排除不需要的字段
            const submitData = {
              title: form.title,
              type: form.type,
              description: form.description,
              organizer: form.organizer,
              location: form.location,
              startTime: formatDateForBackend(form.startTime),
              endTime: formatDateForBackend(form.endTime),
              registrationDeadline: formatDateForBackend(form.registrationDeadline),
              maxParticipants: form.maxParticipants,
              status: form.status
            } 
            const response = isEdit.value 
              ? await updateActivity(route.params.id, submitData)
              : await createActivity(submitData)
            console.log("结束加载...响应数据:",response.data,"isEdit.value数据:",isEdit.value)
            
            if (response.code === 200) {
              ElMessage.success(response.message || (isEdit.value ? '活动更新成功' : '活动发布成功'))
              router.push('/dashboard/activities')
            } else {
              ElMessage.error(response.message || (isEdit.value ? '活动更新失败' : '活动发布失败'))
            }
          } catch (error) {
            ElMessage.error('操作失败，请稍后重试')
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
    }
    
    const handleBack = () => {
      router.push('/dashboard/activities')
    }
    
    onMounted(() => {
      loadActivityData()
    })
    
    return {
      formRef,
      loading,
      isEdit,
      form,
      rules,
      handleSubmit,
      handleReset,
      handleBack
    }
  }
}
</script>

<style scoped>
.activity-form {
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
</style>
