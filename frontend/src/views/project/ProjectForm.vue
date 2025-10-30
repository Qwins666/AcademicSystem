<template>
  <div class="project-form">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑项目' : '创建项目' }}</h1>
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
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入项目名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="项目类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择项目类型" style="width: 100%">
            <el-option label="研究项目" value="RESEARCH" />
            <el-option label="创新项目" value="INNOVATION" />
            <el-option label="实践项目" value="PRACTICE" />
          </el-select>
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="项目级别" prop="level">
          <el-select v-model="form.level" placeholder="请选择项目级别" style="width: 100%">
            <el-option label="国家级" value="NATIONAL" />
            <el-option label="省级" value="PROVINCIAL" />
            <el-option label="校级" value="SCHOOL" />
          </el-select>
        </el-form-item>

        <el-form-item label="项目经费">
          <el-input-number
            v-model="form.funding"
            :min="0"
            :max="10000000"
            placeholder="请输入项目经费"
            style="width: 100%"
            :precision="2"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="项目状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="PLANNING">规划中</el-radio>
            <el-radio label="ONGOING">进行中</el-radio>
            <el-radio label="COMPLETED">已完成</el-radio>
            <el-radio label="CANCELLED">已取消</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '更新' : '创建' }}
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
import { createProject, updateProject, getProjectById } from '@/api/project'

export default {
  name: 'ProjectForm',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const formRef = ref()
    const loading = ref(false)
    
    const isEdit = computed(() => !!route.params.id)
    
    const form = reactive({
      name: '',
      type: '',
      description: '',
      level: '',
      funding: 0,
      startDate: '',
      endDate: '',
      status: 'PLANNING'
    })
    
    const rules = {
      name: [
        { required: true, message: '请输入项目名称', trigger: 'blur' },
        { min: 2, max: 200, message: '名称长度在2到200个字符', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择项目类型', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入项目描述', trigger: 'blur' },
        { min: 10, max: 1000, message: '描述长度在10到1000个字符', trigger: 'blur' }
      ],
      level: [
        { required: true, message: '请选择项目级别', trigger: 'change' }
      ],
      startDate: [
        { required: true, message: '请选择开始日期', trigger: 'change' }
      ],
      endDate: [
        { required: true, message: '请选择结束日期', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择项目状态', trigger: 'change' }
      ]
    }
    
    const loadProjectData = async () => {
      if (isEdit.value) {
        try {
          // 调用API获取项目详情
          const projectId = route.params.id
          const res = await getProjectById(projectId)
          console.log('获取项目详情响应:', res)
          
          // 更新表单数据 - 处理可能的嵌套数据结构
          let projectData = res
          // 检查是否需要从res.data中获取实际项目数据
          if (res && res.data) {
            projectData = res.data
          }
          
          if (projectData && projectData.id) {
            Object.assign(form, projectData)
          } else {
            throw new Error('获取项目数据失败')
          }
        } catch (error) {
          console.error('加载项目数据失败:', error)
          ElMessage.error('加载项目数据失败')
        }
      }
    }
    
    const handleSubmit = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            // 准备提交的数据
            const submitData = { ...form }
            console.log('提交数据:', submitData)
            
            // 根据是编辑还是创建操作调用不同的API
            if (isEdit.value) {
              // 正确调用updateProject，传递id和数据两个参数
              const projectId = route.params.id
              await updateProject(projectId, submitData)
              ElMessage.success('项目更新成功')
            } else {
              await createProject(submitData)
              ElMessage.success('项目创建成功')
            }
            
            router.push('/dashboard/projects')
          } catch (error) {
            console.error('操作失败:', error)
            ElMessage.error(error.response?.data?.message || '操作失败')
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
      router.push('/dashboard/projects')
    }
    
    onMounted(() => {
      loadProjectData()
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
.project-form {
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
