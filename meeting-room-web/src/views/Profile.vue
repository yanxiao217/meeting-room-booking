<template>
  <div class="profile-page">
    <section class="panel identity">
      <span class="avatar">{{ initial }}</span>
      <div class="identity-main">
        <h2 class="name">{{ user?.realName || user?.username }}</h2>
        <p class="handle">@{{ user?.username }}</p>
      </div>
      <span class="pill plain">{{ roleLabel }}</span>
    </section>

    <section class="panel fields">
      <div class="field">
        <span class="field-label">用户名</span>
        <span class="field-value">{{ user?.username }}</span>
      </div>
      <div class="field">
        <span class="field-label">姓名</span>
        <template v-if="!editing">
          <span class="field-value">{{ user?.realName || '未填写' }}</span>
        </template>
        <template v-else>
          <el-input v-model="editForm.realName" size="small" placeholder="请输入姓名" />
        </template>
      </div>
      <div class="field">
        <span class="field-label">手机号</span>
        <template v-if="!editing">
          <span class="field-value num">{{ user?.phone || '未填写' }}</span>
        </template>
        <template v-else>
          <el-input v-model="editForm.phone" size="small" placeholder="请输入手机号" />
        </template>
      </div>
      <div class="field">
        <span class="field-label">邮箱</span>
        <template v-if="!editing">
          <span class="field-value">{{ user?.email || '未填写' }}</span>
        </template>
        <template v-else>
          <el-input v-model="editForm.email" size="small" placeholder="请输入邮箱" />
        </template>
      </div>
      <div class="field">
        <span class="field-label">部门</span>
        <span class="field-value">{{ user?.deptName || '未分配' }}</span>
      </div>
      <div class="field">
        <span class="field-label">角色</span>
        <span class="field-value">{{ roleLabel }}</span>
      </div>
    </section>

    <section class="panel actions">
      <template v-if="!editing">
        <el-button type="primary" @click="startEdit">编辑信息</el-button>
        <el-button @click="handleLogout">退出登录</el-button>
      </template>
      <template v-else>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button @click="cancelEdit">取消</el-button>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { updateProfile } from '@/api'

const router = useRouter()
const auth = useAuthStore()

const user = computed(() => auth.user)
const initial = computed(() => (user.value?.realName || user.value?.username || '?').charAt(0))
const roleLabel = computed(() => (user.value?.role === 'admin' ? '管理员' : '用户'))

const editing = ref(false)
const saving = ref(false)
const editForm = reactive({
  realName: '',
  phone: '',
  email: ''
})

function startEdit() {
  editForm.realName = user.value?.realName || ''
  editForm.phone = user.value?.phone || ''
  editForm.email = user.value?.email || ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function handleSave() {
  saving.value = true
  try {
    const res = await updateProfile(editForm)
    auth.user = res.data
    editing.value = false
    ElMessage.success('保存成功')
  } catch {
    // interceptor shows error
  } finally {
    saving.value = false
  }
}

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}

onMounted(async () => {
  try {
    await auth.fetchUserInfo()
  } catch {
    // ignore
  }
})
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 680px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.identity {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 22px;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--ink);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 22px;
  flex: none;
}

.identity-main {
  flex: 1;
}

.name {
  font-size: 20px;
}

.handle {
  margin: 3px 0 0;
  color: var(--ink-3);
  font-size: 13px;
}

.fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.field {
  padding: 18px 22px;
  border-bottom: 1px solid var(--line);
  border-right: 1px solid var(--line);

  &:nth-child(2n) {
    border-right: none;
  }

  &:nth-last-child(-n + 2) {
    border-bottom: none;
  }
}

.field-label {
  display: block;
  font-size: 12px;
  color: var(--ink-3);
  margin-bottom: 4px;
}

.field-value {
  font-weight: 600;
}

.actions {
  padding: 18px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.note {
  margin: 0;
  color: var(--ink-2);
  font-size: 13px;
  max-width: 44ch;
}
</style>
