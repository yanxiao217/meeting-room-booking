<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="24"><Calendar /></el-icon>
        </div>
        <h1 class="app-title">会议预约系统</h1>
        <p class="app-subtitle">后台管理</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        size="large"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="formData.username"
            placeholder="用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>测试账号: admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await authStore.login(formData.username, formData.password)
      ElMessage.success('登录成功')
      
      const redirect = route.query.redirect as string || '/'
      router.push(redirect)
    } catch (error) {
      // 错误已在拦截器中处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #FAFAF9;
}

.login-card {
  width: 380px;
  padding: 40px;
  background: #FFFFFF;
  border: 1px solid #F0EDE8;
  border-radius: 16px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
  
  .logo-icon {
    width: 56px;
    height: 56px;
    background: linear-gradient(135deg, #5B8FB9 0%, #4A7A9E 100%);
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin: 0 auto 16px;
  }
  
  .app-title {
    font-size: 22px;
    font-weight: 600;
    color: #2D3748;
    margin: 0 0 4px;
  }
  
  .app-subtitle {
    font-size: 14px;
    color: #718096;
    margin: 0;
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    padding: 4px 12px;
  }
  
  .login-btn {
    width: 100%;
    height: 44px;
    border-radius: 8px;
    font-weight: 500;
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  
  p {
    color: #A0AEC0;
    font-size: 12px;
    margin: 0;
  }
}
</style>
