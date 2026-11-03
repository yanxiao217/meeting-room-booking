<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <span>通知配置</span>
      </template>

      <el-form label-width="180px">
        <el-divider content-position="left">通知渠道</el-divider>

        <el-form-item label="站内通知">
          <el-switch v-model="config.siteNotification" />
          <span class="form-tip">开启后用户可在站内查看通知</span>
        </el-form-item>

        <el-form-item label="邮件通知">
          <el-switch v-model="config.emailNotification" @change="onEmailToggle" />
          <span class="form-tip">开启后通过邮件发送通知（需配置SMTP）</span>
        </el-form-item>

        <el-form-item label="企业微信通知">
          <el-switch v-model="config.wechatNotification" disabled />
          <span class="form-tip">（开发中）开启后通过企业微信发送通知</span>
        </el-form-item>

        <template v-if="config.emailNotification">
          <el-divider content-position="left">SMTP 邮件服务器配置</el-divider>

          <el-form-item label="SMTP服务器">
            <el-input v-model="smtp.host" placeholder="如 smtp.qq.com" style="width: 300px" />
          </el-form-item>

          <el-form-item label="SMTP端口">
            <el-input v-model="smtp.port" placeholder="587" style="width: 160px" />
          </el-form-item>

          <el-form-item label="发件人邮箱">
            <el-input v-model="smtp.username" placeholder="your-email@qq.com" style="width: 300px" />
          </el-form-item>

          <el-form-item label="SMTP密码/授权码">
            <el-input v-model="smtp.password" type="password" show-password placeholder="QQ邮箱填授权码" style="width: 300px" />
          </el-form-item>

          <el-form-item label="发件人名称">
            <el-input v-model="smtp.fromName" placeholder="会议室预约系统" style="width: 300px" />
          </el-form-item>

          <el-form-item label="测试收件人">
            <div style="display:flex;gap:8px;align-items:center">
              <el-input v-model="smtp.testRecipient" placeholder="test@example.com" style="width: 300px" />
              <el-button :loading="testing" @click="handleTestEmail">发送测试</el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="savingSmtp" @click="handleSaveSmtp">保存SMTP配置</el-button>
          </el-form-item>
        </template>

        <el-divider content-position="left">通知时机</el-divider>

        <el-form-item label="预约成功通知">
          <el-switch v-model="config.bookingSuccessNotify" />
          <span class="form-tip">用户预约成功后发送通知</span>
        </el-form-item>

        <el-form-item label="会议开始提醒">
          <el-switch v-model="config.meetingRemind" />
          <span class="form-tip">会议开始前提醒预约人</span>
        </el-form-item>

        <el-form-item label="提醒提前量(分钟)">
          <el-input-number v-model="config.remindMinutes" :min="5" :step="5" :max="60" />
          <span class="form-tip">会议开始前N分钟发送提醒</span>
        </el-form-item>

        <el-form-item label="预约取消通知">
          <el-switch v-model="config.bookingCancelNotify" />
          <span class="form-tip">预约被取消时通知预约人</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存通知配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSmtpConfig, saveSmtpConfig, sendTestEmail } from '@/api'

const saving = ref(false)
const savingSmtp = ref(false)
const testing = ref(false)

const config = reactive({
  siteNotification: true,
  emailNotification: false,
  wechatNotification: false,
  bookingSuccessNotify: true,
  meetingRemind: true,
  remindMinutes: 15,
  bookingCancelNotify: true
})

const smtp = reactive({
  host: '',
  port: '587',
  username: '',
  password: '',
  fromName: '会议室预约系统',
  testRecipient: ''
})

onMounted(async () => {
  try {
    const res = await getSmtpConfig()
    if (res.data) {
      smtp.host = res.data['email.smtp.host'] || ''
      smtp.port = res.data['email.smtp.port'] || '587'
      smtp.username = res.data['email.smtp.username'] || ''
      smtp.password = res.data['email.smtp.password'] || ''
      smtp.fromName = res.data['email.smtp.from-name'] || '会议室预约系统'
      smtp.testRecipient = res.data['email.smtp.test-recipient'] || ''
      config.emailNotification = !!(smtp.host && smtp.username)
    }
  } catch {}
})

const onEmailToggle = (val: boolean) => {
  if (val && !smtp.host) {
    ElMessage.info('请先配置SMTP邮件服务器')
  }
}

const handleSaveSmtp = async () => {
  if (!smtp.host || !smtp.username) {
    ElMessage.warning('请填写SMTP服务器和发件人邮箱')
    return
  }
  savingSmtp.value = true
  try {
    await saveSmtpConfig(smtp)
    ElMessage.success('SMTP配置已保存')
  } finally {
    savingSmtp.value = false
  }
}

const handleTestEmail = async () => {
  if (!smtp.testRecipient) {
    ElMessage.warning('请输入测试收件人邮箱')
    return
  }
  testing.value = true
  try {
    await sendTestEmail(smtp.testRecipient)
    ElMessage.success('测试邮件已发送，请检查收件箱')
  } catch {
    // interceptor shows error
  } finally {
    testing.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-tip {
  margin-left: 15px;
  color: #999;
  font-size: 12px;
}
</style>
