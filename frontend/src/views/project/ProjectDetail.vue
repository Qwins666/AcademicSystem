<template>
  <div class="project-detail">
    <div class="page-header">
      <h1>{{ project.name || '项目详情' }}</h1>
      <div class="header-actions">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-button 
          type="primary" 
          @click="handleEdit"
          v-if="canEdit"
        >
          编辑项目
        </el-button>
      </div>
    </div>

    <!-- 项目基本信息卡片 -->
    <el-card class="info-card" v-loading="loading">
      <div class="info-content">
        <div class="info-row">
          <div class="info-item">
            <label>项目名称：</label>
            <span>{{ project.name }}</span>
          </div>
          <div class="info-item">
            <label>项目类型：</label>
            <el-tag :type="getTypeTag(project.type)">{{ getTypeName(project.type) }}</el-tag>
          </div>
          <div class="info-item">
            <label>项目级别：</label>
            <el-tag :type="getLevelTag(project.level)">{{ getLevelName(project.level) }}</el-tag>
          </div>
          <div class="info-item">
            <label>项目状态：</label>
            <el-tag :type="getStatusTag(project.status)">{{ getStatusName(project.status) }}</el-tag>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <label>项目负责人：</label>
            <span>{{ project.leaderName }}</span>
          </div>
          <div class="info-item">
            <label>项目经费：</label>
            <span>¥{{ project.funding ? project.funding.toLocaleString() : '0.00' }}</span>
          </div>
          <div class="info-item">
            <label>开始日期：</label>
            <span>{{ project.startDate }}</span>
          </div>
          <div class="info-item">
            <label>结束日期：</label>
            <span>{{ project.endDate }}</span>
          </div>
        </div>
        <div class="info-row full-width">
          <div class="info-item full-width">
            <label>项目描述：</label>
            <div class="description-content">{{ project.description }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="project-tabs">
      <!-- 成员管理 -->
      <el-tab-pane label="项目成员" name="members">
        <div class="tab-content">
          <div class="tab-header">
            <h3>成员列表</h3>
            <el-button 
              type="primary" 
              @click="showInviteDialog = true"
              v-if="canEdit"
            >
              <el-icon><Plus /></el-icon>
              邀请成员
            </el-button>
          </div>
          <el-table :data="members" style="width: 100%">
            <el-table-column prop="userName" label="用户名" width="120" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="studentId" label="学号/工号" width="120" />
            <el-table-column prop="department" label="院系" width="150" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.role === 'LEADER' ? 'danger' : 'primary'">
                  {{ scope.row.role === 'LEADER' ? '负责人' : '成员' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="joinTime" label="加入时间" min-width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleRemoveMember(scope.row)"
                  v-if="canEdit && scope.row.role !== 'LEADER'"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 阶段管理 -->
      <el-tab-pane label="项目阶段" name="milestones">
        <div class="tab-content">
          <div class="tab-header">
            <h3>项目阶段进度</h3>
            <el-button 
              type="primary" 
              @click="initProjectMilestones"
              v-if="canEdit && project.status !== 'COMPLETED' && project.status !== 'CANCELLED' && milestones.length === 0"
            >
              <el-icon><Plus /></el-icon>
              初始化项目阶段
            </el-button>
          </div>
          
          <!-- 阶段进度节点 - 横向排列 -->
          <div class="milestone-timeline">
            <div class="milestone-container">
              <div 
                v-for="(milestone, index) in predefinedMilestones" 
                :key="milestone.key"
                class="milestone-node"
                :class="{ 
                  'completed': getMilestoneById(milestone.key)?.status === 'COMPLETED',
                  'pending': getMilestoneById(milestone.key)?.status === 'PENDING',
                  'overdue': isMilestoneOverdue(getMilestoneById(milestone.key)),
                  'active': index === currentActiveMilestoneIndex
                }"
              >
              <div class="milestone-header">
                <div class="milestone-number">{{ index + 1 }}</div>
                <div class="milestone-info">
                  <h4>{{ milestone.name }}</h4>
                  <div class="milestone-due-date">
                    截止日期: {{ getMilestoneById(milestone.key)?.dueDate ? getMilestoneById(milestone.key).dueDate : '未设置' }}
                  </div>
                </div>
                <el-tag 
                  v-if="getMilestoneById(milestone.key)"
                  :type="getMilestoneStatusTag(getMilestoneById(milestone.key).status)"
                >
                  {{ getMilestoneStatusName(getMilestoneById(milestone.key).status) }}
                </el-tag>
                <el-button 
                  v-if="canEdit && project.status !== 'COMPLETED' && project.status !== 'CANCELLED' && (!getMilestoneById(milestone.key) || ['PENDING', 'OVERDUE'].includes(getMilestoneById(milestone.key).status))"
                  type="text" 
                  size="small" 
                  @click="editMilestone(getMilestoneById(milestone.key) || { key: milestone.key, name: milestone.name } )"
                >
                  {{ getMilestoneById(milestone.key) ? '编辑' : '添加' }}
                </el-button>
              </div>
              
              <div v-if="getMilestoneById(milestone.key)" class="milestone-content">
                <div class="milestone-description">
                  <strong>阶段描述:</strong>
                  <p>{{ getMilestoneById(milestone.key).description || '无描述' }}</p>
                </div>
                
                <!-- 中期检查和结题验收阶段显示文件上传 -->
                <div 
                  v-if="(milestone.key === 'midterm' || milestone.key === 'conclusion') && canEdit"
                  class="milestone-files"
                >
                  <strong>相关文件:</strong>
                  <el-upload
                    class="upload-demo"
                    :action="uploadUrl"
                    :headers="{ Authorization: `Bearer ${token}` }"
                    :on-success="handleMilestoneFileUploadSuccess"
                    :on-error="handleFileUploadError"
                    :data="getMilestoneUploadData(milestone.key)"
                    :disabled="!canEdit || project.status === 'COMPLETED' || project.status === 'CANCELLED'"
                    drag
                  >
                    <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                    <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
                  </el-upload>
                  
                  <!-- 文件列表 -->
                  <div v-if="getMilestoneFiles(milestone.key).length > 0" class="milestone-file-list">
                    <div 
                      v-for="file in getMilestoneFiles(milestone.key)" 
                      :key="file.id"
                      class="file-item"
                    >
                      <a href="#" @click.prevent="handleFileDownload(file)">{{ file.fileName }}</a>
                      <el-button 
                        type="danger" 
                        size="small" 
                        icon="Delete"
                        @click="handleDeleteFile(file)"
                      />
                    </div>
                  </div>
                </div>
                
                <!-- 操作按钮 -->
                <div v-if="canEdit && project.status !== 'COMPLETED' && project.status !== 'CANCELLED'" class="milestone-actions">
                  <el-button 
                    v-if="getMilestoneById(milestone.key).status !== 'COMPLETED'"
                    type="primary" 
                    size="small" 
                    @click="handleUpdateMilestoneStatus(getMilestoneById(milestone.key), 'COMPLETED')"
                  >
                    标记为完成
                  </el-button>
                  <el-button 
                    v-if="getMilestoneById(milestone.key).status === 'COMPLETED'"
                    type="warning" 
                    size="small" 
                    @click="handleUpdateMilestoneStatus(getMilestoneById(milestone.key), 'PENDING')"
                  >
                    标记为待完成
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 文件管理 -->
      <el-tab-pane label="项目文件" name="files">
        <div class="tab-content">
          <div class="tab-header">
            <el-upload
              class="upload-demo"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleFileUploadSuccess"
              :on-error="handleFileUploadError"
              :data="getGeneralUploadData()"
              :disabled="!canEdit || project.status === 'COMPLETED' || project.status === 'CANCELLED'"
              drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
                <div class="el-upload__tip">
                  支持上传中期报告、结题材料等项目相关文件
                </div>
            </el-upload>
          </div>
          <h3>文件列表</h3><br/>
          <el-table :data="files" style="width: 100%">
            <el-table-column prop="fileName" label="文件名" min-width="250">
              <template #default="scope">
                <el-button type="text" @click="handleFileDownload(scope.row)">{{ scope.row.fileName }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="fileSize" label="文件大小" width="100">
              <template #default="scope">
                {{ formatFileSize(scope.row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="fileType" label="文件类型" width="100" />
            <el-table-column prop="uploaderName" label="上传者" width="120" />
            <el-table-column prop="uploadTime" label="上传时间" width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleDeleteFile(scope.row)"
                  v-if="canEdit"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 邀请成员对话框 -->
    <el-dialog title="邀请成员" v-model="showInviteDialog" width="600px">
      <el-form :model="inviteForm" :rules="inviteRules" ref="inviteFormRef">
        <el-form-item label="选择学生" prop="userId">
          <el-select 
            v-model="inviteForm.userId" 
            placeholder="请选择学生" 
            style="width: 100%"
            show-search
            :filter-method="filterStudentMethod"
            clearable
          >
            <el-option 
              v-for="student in availableStudents" 
              :key="student.id" 
              :label="`${student.realName} (${student.studentId})`" 
              :value="student.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showInviteDialog = false">取消</el-button>
          <el-button type="primary" @click="handleInviteMember">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑阶段对话框 -->
    <el-dialog 
      :title="editingMilestone ? '编辑阶段' : '添加阶段'" 
      v-model="showAddMilestoneDialog" 
      width="600px"
    >
      <el-form :model="milestoneForm" :rules="milestoneRules" ref="milestoneFormRef">
        <el-form-item label="阶段名称" prop="name">
          <el-input v-model="milestoneForm.name" placeholder="请输入阶段名称" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker 
            v-model="milestoneForm.dueDate" 
            type="date" 
            placeholder="选择截止日期" 
            style="width: 100%" 
            format="YYYY-MM-DD" 
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="阶段描述" prop="description">
          <el-input 
            v-model="milestoneForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入阶段描述" 
            maxlength="500" 
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddMilestoneDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSaveMilestone">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getProjectById, 
  getProjectMembers, 
  addProjectMember, 
  removeProjectMember,
  getProjectMilestones,
  addProjectMilestone,
  updateProjectMilestone,
  deleteProjectMilestone,
  getProjectFiles,
  uploadProjectFile,
  deleteProjectFile,
  getAvailableStudents,
  getUserDetailAtProject
} from '@/api/project'
import { getUserDetail } from '@/api/user'
import store from '@/store'

export default {
  name: 'ProjectDetail',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const projectId = computed(() => route.params.id)
    
    // 状态变量
    const loading = ref(false)
    const project = reactive({})
    // 确保表格数据始终是数组类型
    const members = ref([])
    const milestones = ref([])
    const files = ref([])
    const availableStudents = ref([])
    const activeTab = ref('members')
    const showInviteDialog = ref(false)
    const showAddMilestoneDialog = ref(false)
    const editingMilestone = ref(null)
    const inviteForm = reactive({ userId: '' })
    // 用于跟踪已提示的阶段，避免重复提示
    const remindedMilestones = ref(new Set())
    
    // 阶段文件上传成功处理函数 - 提前定义以避免渲染警告
    const handleMilestoneFileUploadSuccess = async (response) => {
      ElMessage.success('文件上传成功')
      await loadProjectFiles()
    }
    const milestoneForm = reactive({ 
      name: '', 
      dueDate: '', 
      description: '',
      key: '' // 添加key字段用于识别预定义阶段
    })
    
    // 预定义的项目阶段
    const predefinedMilestones = [
      { key: 'proposal', name: '项目立项' },
      { key: 'literature', name: '文献调研' },
      { key: 'design', name: '方案设计与开发' },
      { key: 'midterm', name: '中期检查' },
      { key: 'achievements', name: '成果整理' },
      { key: 'conclusion', name: '结题验收' }
    ]
    
    const inviteFormRef = ref()
    const milestoneFormRef = ref()
    
    const inviteRules = {
      userId: [
        { required: true, message: '请选择学生', trigger: 'change' }
      ]
    }
    
    const milestoneRules = {
      name: [
        { required: true, message: '请输入阶段名称', trigger: 'blur' },
        { min: 2, max: 200, message: '名称长度在2到200个字符', trigger: 'blur' }
      ],
      dueDate: [
        { required: true, message: '请选择截止日期', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入阶段描述', trigger: 'blur' },
        { min: 5, max: 500, message: '描述长度在5到500个字符', trigger: 'blur' }
      ]
    }
    
    const token = computed(() => store.getters.token)
    
    // 增强用户信息获取逻辑，优先从store获取当前登录用户信息
    const getUserInfo = () => {
      try {
        // 1. 优先从Vuex store获取用户信息
        const storeUser = store.getters.user;
        if (storeUser) {
          console.log('从store获取的用户信息:', storeUser);
          return {
            id: storeUser.id,
            roles: storeUser.roles || []
          };
        }
        
        // 2. 如果store中没有，尝试从localStorage获取
        const userInfoStr = localStorage.getItem('userInfo');
        console.log('从本地存储获取的用户信息:', userInfoStr);
        if (userInfoStr) {
          return JSON.parse(userInfoStr);
        }
        
        // 3. 如果都没有，返回默认值
        console.log('未找到用户信息，返回默认值');
        return {
          id: null,
          roles: []
        };
      } catch (error) {
        console.error('获取用户信息失败:', error);
        return {
          id: null,
          roles: []
        };
      }
    }
    
    const userId = computed(() => {
      const userInfo = getUserInfo();
      console.log('用户信息:', userInfo);
      return userInfo?.id || '';
    })
    
    const userRole = computed(() => {
      const userInfo = getUserInfo();
      return userInfo?.roles?.[0] || '';
    })
    
    const canEdit = computed(() => {
      try {
        // 获取当前用户ID和角色
        const currentUserId = Number(userId.value) || 0;
        const role = userRole.value || '';
        
        // 获取项目负责人ID
        const projectLeaderId = Number(project.leaderId) || 0;
        
        // 权限判断逻辑：只有项目负责人可以编辑
        const isProjectLeader = currentUserId === projectLeaderId;
        // 只有项目负责人才能编辑
        const result = isProjectLeader;
        
        // 添加调试日志
        console.log('canEdit计算:', {
          currentUserId,
          role,
          projectLeaderId,
          isProjectLeader,
          result
        });
        
        return Boolean(result); // 确保返回布尔值
      } catch (error) {
        console.error('计算canEdit时出错:', error);
        return false; // 出错时默认不允许编辑，保证安全性
      }
    })
    
    // 在开发环境添加额外的辅助函数来验证用户状态
    const validateUserStatus = () => {
      const userInfo = getUserInfo();
      console.log('验证用户状态:');
      console.log('1. localStorage中是否有userInfo:', !!localStorage.getItem('userInfo'));
      console.log('2. 用户ID:', userId.value);
      console.log('3. 用户角色:', userRole.value);
      console.log('4. 项目负责人ID:', project.leaderId);
      console.log('5. canEdit值:', canEdit.value);
    }
    
    const uploadUrl = computed(() => {
      // 使用相对路径，vue.config.js已配置代理
      return `/api/projects/${projectId.value}/files`
    })
    
    // 获取类型名称
    const getTypeName = (type) => {
      const typeMap = {
        'RESEARCH': '研究项目',
        'INNOVATION': '创新项目',
        'PRACTICE': '实践项目'
      }
      return typeMap[type] || type
    }
    
    // 获取类型标签样式
    const getTypeTag = (type) => {
      const tagMap = {
        'RESEARCH': 'primary',
        'INNOVATION': 'success',
        'PRACTICE': 'warning'
      }
      return tagMap[type] || 'info'
    }
    
    // 获取级别名称
    const getLevelName = (level) => {
      const levelMap = {
        'NATIONAL': '国家级',
        'PROVINCIAL': '省级',
        'SCHOOL': '校级'
      }
      return levelMap[level] || level
    }
    
    // 获取级别标签样式
    const getLevelTag = (level) => {
      const tagMap = {
        'NATIONAL': 'danger',
        'PROVINCIAL': 'warning',
        'SCHOOL': 'success'
      }
      return tagMap[level] || 'info'
    }
    
    // 获取状态名称
    const getStatusName = (status) => {
      const statusMap = {
        'PLANNING': '规划中',
        'ONGOING': '进行中',
        'ACTIVE': '进行中', // 添加对ACTIVE状态的支持
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }
    
    // 获取状态标签样式
    const getStatusTag = (status) => {
      const tagMap = {
        'PLANNING': 'info',
        'ONGOING': 'success',
        'ACTIVE': 'success', // 添加对ACTIVE状态的支持
        'COMPLETED': 'warning',
        'CANCELLED': 'danger'
      }
      return tagMap[status] || 'info'
    }
    
    // 获取阶段状态名称
    const getMilestoneStatusName = (status) => {
      const statusMap = {
        'PENDING': '待完成',
        'COMPLETED': '已完成',
        'OVERDUE': '已逾期'
      }
      return statusMap[status] || status
    }
    
    // 获取阶段状态标签样式
    const getMilestoneStatusTag = (status) => {
      const tagMap = {
        'PENDING': 'warning',
        'COMPLETED': 'success',
        'OVERDUE': 'danger'
      }
      return tagMap[status] || 'info'
    }
    
    // 根据key获取阶段对象，也支持通过名称匹配
    const getMilestoneById = (key) => {
      if (!key) return null
      
      // 尝试通过key直接匹配（首选方式）
      let milestone = milestones.value.find(m => m.key === key)
      
      // 如果找不到，尝试通过预定义名称匹配
      if (!milestone) {
        const predefined = predefinedMilestones.find(p => p.key === key)
        if (predefined) {
          // 尝试通过名称匹配
          milestone = milestones.value.find(m => m.name === predefined.name)
          
          // 如果仍未找到，尝试通过部分名称匹配
          if (!milestone) {
            milestone = milestones.value.find(m => 
              m.name && m.name.includes(predefined.name)
            )
          }
        }
      }
      
      // 最后的备选方案：如果有且只有一个阶段，且key匹配预定义阶段，就返回它
      if (!milestone && milestones.value.length === 1) {
        const predefined = predefinedMilestones.find(p => p.key === key)
        if (predefined) {
          milestone = milestones.value[0]
        }
      }
      
      return milestone
    }

    
    
    // 判断阶段是否逾期
    const isMilestoneOverdue = (milestone) => {
      if (!milestone || !milestone.dueDate || milestone.status === 'COMPLETED') {
        return false
      }
      const dueDate = new Date(milestone.dueDate)
      const today = new Date()
      return dueDate < today
    }
    
    // 检查阶段是否将在24小时内到期
    const isMilestoneDueWithin24Hours = (milestone) => {
      if (!milestone || !milestone.dueDate || milestone.status === 'COMPLETED') {
        return false
      }
      const dueDate = new Date(milestone.dueDate)
      const today = new Date()
      const timeDiff = dueDate - today
      const hoursDiff = timeDiff / (1000 * 60 * 60)
      // 在24小时内且未逾期
      return hoursDiff <= 24 && hoursDiff >= 0
    }
    
    // 检查即将到期的阶段并显示提示
    const checkUpcomingMilestones = async () => {
      try {
        // 确保里程碑数据已加载
        if (milestones.value.length === 0) {
          await loadProjectMilestones()
        }
        
        // 检查每个阶段
        for (const milestone of milestones.value) {
          const milestoneId = milestone.id || milestone.key || milestone.name
          
          // 如果阶段将在24小时内到期且尚未提示过
          if (isMilestoneDueWithin24Hours(milestone) && !remindedMilestones.value.has(milestoneId)) {
            // 显示确认弹窗
            await ElMessageBox.alert(
              `项目阶段"${milestone.name}"将在24小时内到期，请及时完成！`,
              '阶段到期提醒',
              {
                confirmButtonText: '我知道了',
                type: 'warning',
                closeOnClickModal: false,
                closeOnPressEscape: false
              }
            )
            
            // 标记为已提示
            remindedMilestones.value.add(milestoneId)
          }
        }
      } catch (error) {
        // 用户点击取消或关闭按钮时会抛出错误，这里静默处理
        console.log('阶段提醒弹窗已关闭')
      }
    }
    
    // 获取当前活动阶段索引
    const currentActiveMilestoneIndex = computed(() => {
      // 找到最后一个已完成的阶段
      const completedMilestones = milestones.value.filter(m => m.status === 'COMPLETED')
      if (completedMilestones.length === 0) {
        return 0 // 没有完成的阶段，返回第一个阶段
      }
      
      // 获取最后一个完成阶段的索引
      const lastCompletedKey = completedMilestones[completedMilestones.length - 1].key
      const lastCompletedIndex = predefinedMilestones.findIndex(m => m.key === lastCompletedKey)
      
      // 如果所有阶段都已完成，返回最后一个阶段
      if (lastCompletedIndex === predefinedMilestones.length - 1) {
        return lastCompletedIndex
      }
      
      // 否则返回下一个未完成的阶段
      return lastCompletedIndex + 1
    })
    
    // 获取阶段相关的文件
    const getMilestoneFiles = (milestoneKey) => {
      return files.value.filter(file => file.milestoneKey === milestoneKey)
    }
    
    // 初始化项目阶段
    const initProjectMilestones = async () => {
      try {
        // 为每个预定义阶段创建初始记录
        for (const predefined of predefinedMilestones) {
          const existingMilestone = getMilestoneById(predefined.key)
          if (!existingMilestone) {
            await addProjectMilestone(projectId.value, {
              name: predefined.name,
              key: predefined.key,
              status: 'PENDING'
            })
          }
        }
        await loadProjectMilestones()
        ElMessage.success('项目阶段初始化成功')
      } catch (error) {
        console.error('初始化项目阶段失败:', error)
        ElMessage.error('初始化项目阶段失败')
      }
    }
    

    
    // 格式化文件大小
    const formatFileSize = (size) => {
      if (!size) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let i = 0
      while (size >= 1024 && i < units.length - 1) {
        size /= 1024
        i++
      }
      return `${size.toFixed(2)} ${units[i]}`
    }
    
    // 加载项目详情
    const loadProjectDetail = async () => {
      loading.value = true
      try {
        const res = await getProjectById(projectId.value)
        // 从响应对象中提取data部分，这是实际的项目数据
        Object.assign(project, res.data || res)
        console.log('项目详情:', project)
        
        // 如果有leaderId但没有leaderName，则尝试获取负责人姓名
        if (project.leaderId && !project.leaderName) {
          try {
            // 尝试从成员列表中查找负责人信息
            const membersRes = await getProjectMembers(projectId.value)
            const membersData = Array.isArray(membersRes) ? membersRes : (membersRes?.data || [])
            const leader = membersData.find(m => m.userId === project.leaderId)
            
            if (leader && leader.realName) {
              project.leaderName = leader.realName
            } else {
              // 如果从成员列表中找不到，则尝试通过用户ID直接获取用户信息
                try {
                  const userRes = await getUserDetailAtProject(project.leaderId)
                  const userData = userRes.data || userRes
                  if (userData.realName) {
                    project.leaderName = userData.realName
                  } else {
                    project.leaderName = '未知负责人'
                  }
                } catch (userError) {
                  console.warn('获取用户信息失败:', userError)
                  project.leaderName = '未知负责人'
                }
            }
          } catch (memberError) {
            console.warn('获取成员列表失败:', memberError)
            project.leaderName = '未知负责人'
          }
        }
      } catch (error) {
        console.error('加载项目详情失败:', error)
        ElMessage.error('加载项目详情失败')
      } finally {
        loading.value = false
      }
    }
    
    // 加载项目成员
    const loadProjectMembers = async () => {
      try {
        const res = await getProjectMembers(projectId.value)
        console.log('项目成员响应:', res)
        // 确保返回值始终是数组
        let memberList = Array.isArray(res) ? res : (res?.data || [])
        
        // 为每个成员获取用户详细信息
        const membersWithUserInfo = await Promise.all(
          memberList.map(async (member) => {
            try {
              // 使用getUserDetailAtProject获取用户详细信息
              const userRes = await getUserDetailAtProject(member.userId)
              console.log('获取成员用户信息:', userRes)
              const userInfo = userRes?.data || userRes
              
              // 将用户详细信息合并到成员对象中
              return {
                ...member,
                realName: userInfo.realName || '未知',
                studentId: userInfo.studentId || userInfo.teacherId || '未知',
                department: userInfo.department || '未知'
              }
            } catch (error) {
              console.error(`获取用户${member.userId}信息失败:`, error)
              // 出错时返回原始成员信息，避免影响整个列表
              return {
                ...member,
                realName: '未知',
                studentId: '未知',
                department: '未知'
              }
            }
          })
        )
        
        members.value = membersWithUserInfo
      } catch (error) {
        console.error('加载项目成员失败:', error)
        ElMessage.error('加载项目成员失败')
        members.value = [] // 出错时确保仍是数组
      }
    }
    
    // 加载可用学生列表（未加入项目的学生）
    const loadAvailableStudents = async () => {
      try {
        // 使用新的接口获取学生列表，教师也可以访问
        const res = await getAvailableStudents()
        // 确保返回值始终是数组
        const students = Array.isArray(res) ? res : (res?.data || [])
        const memberIds = members.value.map(m => m.userId)
        availableStudents.value = students.filter(student => !memberIds.includes(student.id))
      } catch (error) {
        console.error('加载学生列表失败:', error)
        ElMessage.error('加载学生列表失败')
        availableStudents.value = [] // 出错时确保仍是数组
      }
    }
    
    // 加载项目阶段
    const loadProjectMilestones = async () => {
      try {
        console.log('开始获取项目阶段数据，项目ID:', projectId.value)
        const startTime = Date.now()
        const res = await getProjectMilestones(projectId.value)
        const endTime = Date.now()
        
        console.log(`API调用耗时: ${endTime - startTime}ms`)
        console.log('API返回原始响应:', JSON.stringify(res))
        
        // 检查响应结构
        console.log('响应类型:', typeof res)
        console.log('响应是否有data属性:', res && res.hasOwnProperty('data'))
        console.log('data属性类型:', res && typeof res.data)
        
        // 确保返回值始终是数组
        milestones.value = Array.isArray(res) ? res : (res?.data || [])
        console.log('处理后的项目阶段数据:', JSON.stringify(milestones.value))
        console.log('阶段数据长度:', milestones.value.length)
        
        // 如果有数据，打印第一条数据的详细信息
        if (milestones.value.length > 0) {
          console.log('第一条阶段数据详情:', JSON.stringify(milestones.value[0]))
          console.log('是否包含dueDate字段:', milestones.value[0].hasOwnProperty('dueDate'))
        }
      } catch (error) {
        console.error('加载项目阶段失败:', error)
        console.error('错误详情:', error.response ? JSON.stringify(error.response) : error.message)
        ElMessage.error('加载项目阶段失败')
        milestones.value = [] // 出错时确保仍是数组
      }
    }
    
    // 加载项目文件
    const loadProjectFiles = async () => {
      try {
        const res = await getProjectFiles(projectId.value)
        // 确保返回值始终是数组
        files.value = Array.isArray(res) ? res : (res?.data || [])
      } catch (error) {
        console.error('加载项目文件失败:', error)
        ElMessage.error('加载项目文件失败')
        files.value = [] // 出错时确保仍是数组
      }
    }
    
    // 邀请成员
    const handleInviteMember = async () => {
      if (!inviteFormRef.value) return
      
      await inviteFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            await addProjectMember(projectId.value, inviteForm.userId)
            ElMessage.success('邀请成员成功')
            showInviteDialog.value = false
            await loadProjectMembers()
            await loadAvailableStudents()
            
            // 重置表单
            inviteForm.userId = ''
          } catch (error) {
            console.error('邀请成员失败:', error)
            ElMessage.error('邀请成员失败')
          }
        }
      })
    }
    
    // 移除成员
    const handleRemoveMember = async (member) => {
      ElMessageBox.confirm('确定要移除该成员吗？', '确认移除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await removeProjectMember(projectId.value, member.userId)
          ElMessage.success('移除成员成功')
          await loadProjectMembers()
          await loadAvailableStudents()
        } catch (error) {
          console.error('移除成员失败:', error)
          ElMessage.error('移除成员失败')
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    // 编辑阶段
    const editMilestone = (milestone) => {
      editingMilestone.value = milestone
      Object.assign(milestoneForm, {
        name: milestone.name,
        dueDate: milestone.dueDate || '',
        description: milestone.description || '',
        key: milestone.key
      })
      showAddMilestoneDialog.value = true
    }
    
    // 保存阶段
    const handleSaveMilestone = async () => {
      if (!milestoneFormRef.value) return
      
      await milestoneFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            // 确保包含key字段
            const saveData = { ...milestoneForm }
            
            // 查找对应的阶段对象以获取id
            let milestoneId = null
            if (editingMilestone.value) {
              // 尝试通过key查找对应的阶段id
              if (editingMilestone.value.key) {
                const existingMilestone = milestones.value.find(m => {
                  // 先尝试直接匹配key
                  const milestoneObj = getMilestoneById(editingMilestone.value.key)
                  return milestoneObj && milestoneObj.id
                })
                if (existingMilestone && getMilestoneById(editingMilestone.value.key)) {
                  milestoneId = getMilestoneById(editingMilestone.value.key).id
                }
              }
              // 如果有直接的id字段，优先使用
              if (editingMilestone.value.id) {
                milestoneId = editingMilestone.value.id
              }
            }
            
            if (milestoneId) {
              // 编辑阶段
              await updateProjectMilestone(projectId.value, milestoneId, saveData)
              ElMessage.success('更新阶段成功')
            } else {
              // 添加阶段
              await addProjectMilestone(projectId.value, saveData)
              ElMessage.success('添加阶段成功')
            }
            
            showAddMilestoneDialog.value = false
            await loadProjectMilestones()
            
            // 重置表单
            editingMilestone.value = null
            milestoneForm.name = ''
            milestoneForm.dueDate = ''
            milestoneForm.description = ''
            milestoneForm.key = ''
          } catch (error) {
            console.error('保存阶段失败:', error)
            // 尝试获取更具体的错误信息
            const errorMessage = error.response?.data?.msg || '保存阶段失败'
            ElMessage.error(errorMessage)
          }
        }
      })
    }
    
    // 更新阶段状态
    const handleUpdateMilestoneStatus = async (milestone, status) => {
      // 根据状态生成对应的确认消息
      let statusText = ''
      if (status === 'COMPLETED') {
        statusText = '已完成'
      } else if (status === 'PENDING') {
        statusText = '待完成'
      } else if (status === 'OVERDUE') {
        statusText = '已逾期'
      }
      
      ElMessageBox.confirm(`确定要将该阶段标记为${statusText}吗？`, '确认操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await updateProjectMilestone(projectId.value, milestone.id, { status })
          ElMessage.success('更新阶段状态成功')
          await loadProjectMilestones()
        } catch (error) {
          console.error('更新阶段状态失败:', error)
          const errorMessage = error.response?.data?.msg || '更新阶段状态失败'
          ElMessage.error(errorMessage)
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    // 删除阶段 - 暂时禁用，因为我们使用预定义阶段
    const handleDeleteMilestone = async (milestone) => {
      ElMessage.warning('预定义阶段不允许删除，请编辑或更新状态')
    }
    
    // 此函数已移至setup函数早期部分
    
    // 文件上传成功
    const handleFileUploadSuccess = async () => {
      ElMessage.success('文件上传成功')
      await loadProjectFiles()
    }
    
    // 文件上传失败
    const handleFileUploadError = () => {
      ElMessage.error('文件上传失败')
    }
    
    // 下载文件
    const handleFileDownload = async (file) => {
      try {
        // 检查文件是否为PDF格式
        const isPdf = file.fileName.toLowerCase().endsWith('.pdf');
        
        if (isPdf) {
          // 对于PDF文件，显示选择框
          ElMessageBox.confirm('请选择操作', 'PDF文件操作', {
            confirmButtonText: '预览PDF',
            cancelButtonText: '下载附件',
            type: 'info',
            closeOnClickModal: false,
            closeOnPressEscape: false,
            customClass: 'pdf-operation-dialog'
          }).then(() => {
            // 预览PDF操作
            previewPdf(file);
          }).catch(() => {
            // 下载文件操作
            performDownload(file);
          });
        } else {
          // 非PDF文件直接下载
          performDownload(file);
        }
      } catch (error) {
        console.error('文件操作失败:', error);
        ElMessage.error('操作失败，请重试');
      }
    }
    
    // 执行文件下载的具体逻辑
    const performDownload = (file) => {
      try {
        // 通过后端API下载文件，使用文件ID而不是直接URL
        // 构建API下载路径
        const downloadUrl = `/api/projects/${projectId.value}/files/${file.id}/download`;
        
        // 创建隐藏的下载链接
        const link = document.createElement('a');
        
        // 直接设置带token的URL
        const linkWithToken = `${downloadUrl}?token=${token.value}`;
        link.href = linkWithToken;
        link.download = file.fileName;
        
        // 隐藏链接
        link.style.display = 'none';
        
        // 添加到文档并触发下载
        document.body.appendChild(link);
        link.click();
        
        // 清理
        setTimeout(() => {
          document.body.removeChild(link);
        }, 100);
      } catch (error) {
        console.error('文件下载失败:', error);
        ElMessage.error('文件下载失败，请重试');
      }
    }
    
    // 预览PDF文件
    const previewPdf = (file) => {
      try {
        // 构建PDF预览URL（使用带token的API端点）
        const previewUrl = `/api/projects/${projectId.value}/files/${file.id}/download?token=${token.value}`;
        
        // 在新标签页中打开PDF进行在线预览
        // 浏览器会自动处理PDF的显示，而不是下载
        window.open(previewUrl, '_blank');
        
        // 提供预览提示
        ElMessage.success('正在打开PDF预览');
      } catch (error) {
        console.error('PDF预览失败:', error);
        ElMessage.error('PDF预览失败，请重试');
      }
    }
    
    // 删除文件
    const handleDeleteFile = async (file) => {
      ElMessageBox.confirm('确定要删除该文件吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteProjectFile(projectId.value, file.id)
          ElMessage.success('删除文件成功')
          await loadProjectFiles()
        } catch (error) {
          console.error('删除文件失败:', error)
          ElMessage.error('删除文件失败')
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    // 编辑项目
    const handleEdit = () => {
      router.push(`/dashboard/projects/${projectId.value}/edit`)
    }
    
    // 过滤学生的方法
    const filterStudentMethod = (query, option) => {
      // 从option中获取label文本
      const label = option.label || '';
      // 将查询字符串和标签文本都转为小写进行匹配
      return label.toLowerCase().includes(query.toLowerCase());
    }

    // 返回列表
    const handleBack = () => {
      router.push('/dashboard/projects')
    }
    
    // 监听邀请对话框打开，加载可用学生列表
    watch(showInviteDialog, (newVal) => {
      if (newVal) {
        loadAvailableStudents()
      }
    })
    
    // 监听阶段对话框关闭，重置表单
    watch(showAddMilestoneDialog, (newVal) => {
      if (!newVal && milestoneFormRef.value) {
        milestoneFormRef.value.resetFields()
        editingMilestone.value = null
        milestoneForm.name = ''
        milestoneForm.dueDate = ''
        milestoneForm.description = ''
      }
    })
    
    // 监听标签页切换，加载对应数据
    watch(activeTab, (newVal) => {
      if (newVal === 'members') {
        loadProjectMembers()
      } else if (newVal === 'milestones') {
        loadProjectMilestones()
      } else if (newVal === 'files') {
        loadProjectFiles()
      }
    })
    
    onMounted(async () => {
      // 先加载项目详情，确保project对象有数据
      await loadProjectDetail()
      await loadProjectMembers()
      await loadProjectMilestones() // 加载项目阶段数据，确保阶段截止日期能正确显示
      await loadProjectFiles() // 加载项目文件列表，确保在milestones标签页中也能显示文件
      
      // 页面加载完成后验证用户状态和权限
      setTimeout(() => {
        validateUserStatus();
      }, 500); // 稍微延迟执行，确保所有数据都已加载
      
      // 页面加载完成后检查即将到期的阶段
      console.log('检查到期的canEdit:', canEdit.value); // 调试日志
      if(canEdit.value){
        checkUpcomingMilestones();
      }
      
    })

    // 添加计算属性来确保 headers 正确设置
    const uploadHeaders = computed(() => {
      const token = store.getters.token;
      console.log('上传文件时的Token:', token); // 调试日志
      return {
        'Authorization': `Bearer ${token}`
      };
    });

    const uploadData = computed(() => {
      return {
        projectId: projectId.value,
        milestoneId: '', // 根据实际情况设置
        milestoneKey: '', // 根据实际情况设置
        uploaderId: userId.value
      };
    });

    // 获取阶段上传数据
    const getMilestoneUploadData = (milestoneKey) => {
      const milestone = getMilestoneById(milestoneKey)
      return {
        projectId: projectId.value,
        milestoneId: milestone ? milestone.id : 0, // 确保是数字，不是 undefined
        milestoneKey: milestoneKey,
        uploaderId: userId.value
      }
    }

    // 获取通用上传数据
    const getGeneralUploadData = () => {
      return {
        projectId: projectId.value,
        milestoneId: 0, // 通用文件使用默认 milestoneId
        uploaderId: userId.value
      }
    }
    
    return {
      loading,
      project,
      members,
      milestones,
      files,
      availableStudents,
      activeTab,
      showInviteDialog,
      showAddMilestoneDialog,
      editingMilestone,
      inviteForm,
      predefinedMilestones,
      milestoneForm,
      inviteRules,
      milestoneRules,
      inviteFormRef,
      milestoneFormRef,
      token,
      canEdit,
      uploadUrl,
      currentActiveMilestoneIndex,
      uploadHeaders,
      uploadData,
      getTypeName,
      getTypeTag,
      getLevelName,
      getLevelTag,
      getStatusName,
      getStatusTag,
      getMilestoneStatusName,
      getMilestoneStatusTag,
      getMilestoneById,
      isMilestoneOverdue,
      getMilestoneFiles,
      initProjectMilestones,
      formatFileSize,
      handleInviteMember,
      handleRemoveMember,
      editMilestone,
      handleSaveMilestone,
      handleUpdateMilestoneStatus,
      handleDeleteMilestone,
      handleFileUploadSuccess,
      handleFileUploadError,
      handleMilestoneFileUploadSuccess,
      handleFileDownload,
      handleDeleteFile,
      handleEdit,
      handleBack,
      validateUserStatus,
      filterStudentMethod,
      getMilestoneUploadData,
      getGeneralUploadData
    }
  }
}
</script>

<style scoped>
.project-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
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

.header-actions {
  display: flex;
  gap: 10px;
}

.info-card {
  margin-bottom: 20px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-row {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.info-row.full-width {
  flex-direction: column;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.info-item.full-width {
  width: 100%;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.description-content {
  color: #303133;
  line-height: 1.6;
  min-height: 60px;
}

.project-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.tab-header h3 {
  margin: 0;
  color: #2c3e50;
}

.upload-demo {
  width: 100%;
}

/* 减小上传区域高度为原来的一半 */
.upload-demo .el-upload-dragger {
  height: 100px !important; /* 默认为200px，减小到100px */
  line-height: 100px;
}

.upload-demo .el-icon--upload {
  font-size: 24px;
  margin-bottom: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 里程碑样式 - 横向排列 */
.milestone-timeline {
  margin-top: 20px;
  padding: 20px 0;
}

.milestone-container {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  position: relative;
  min-height: 200px;
}

.milestone-container::before {
  content: '';
  position: absolute;
  top: 30px;
  left: 60px;
  right: 60px;
  height: 4px;
  background-color: #e0e0e0;
  z-index: 0;
}

.milestone-node {
  flex: 1;
  max-width: 200px;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
  margin: 0 10px;
  background-color: white;
}

.milestone-node:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.milestone-node.completed {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.milestone-node.pending {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.milestone-node.overdue {
  border-color: #f56c6c;
  background-color: #fef0f0;
}

.milestone-node.active {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

/* 连接线 */
.milestone-node:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 30px;
  right: -30px;
  width: 30px;
  height: 4px;
  background-color: #409eff;
}

.milestone-node.completed:not(:last-child)::after {
  background-color: #67c23a;
}

.milestone-node.overdue:not(:last-child)::after {
  background-color: #f56c6c;
}

.milestone-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  margin-bottom: 10px;
}

.milestone-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-bottom: 10px;
}

.milestone-node.completed .milestone-number {
  background-color: #67c23a;
}

.milestone-node.overdue .milestone-number {
  background-color: #f56c6c;
}

.milestone-info {
  text-align: center;
}

.milestone-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  word-wrap: break-word;
}

.milestone-due-date {
  font-size: 12px;
  color: #606266;
}

.milestone-content {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e0e0e0;
  font-size: 12px;
}

.milestone-description {
  margin-bottom: 10px;
}

.milestone-description p {
  margin: 5px 0 0 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.milestone-files {
  margin-top: 10px;
  font-size: 12px;
}

.milestone-file-list {
  margin-top: 5px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 11px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.file-item a {
  flex: 1;
  color: #409eff;
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-item a:hover {
  text-decoration: underline;
}
</style>