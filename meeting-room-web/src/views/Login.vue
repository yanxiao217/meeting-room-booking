<template>
  <div class="login">
    <section class="aside">
      <div class="brand">
        <span class="brand-mark" aria-hidden="true"></span>
        <span class="brand-name">会议室预约</span>
      </div>

      <div class="pitch">
        <h1>先看哪间空着，<br />再决定在哪开会。</h1>
        <p>每层楼的会议室、每个时段，一屏看清。选中空格就能订，不用再在群里问谁占了哪间。</p>
      </div>

      <div class="mini" aria-hidden="true">
        <div class="mini-row" v-for="row in MINI" :key="row.name">
          <span class="mini-name">{{ row.name }}</span>
          <span class="mini-track">
            <span
              v-for="(b, i) in row.blocks"
              :key="i"
              class="mini-block"
              :class="{ mine: b.mine }"
              :style="{ left: `${b.left}%`, width: `${b.width}%` }"
            ></span>
          </span>
        </div>
      </div>
    </section>

    <section class="form-side">
      <div class="form-card">
        <div class="switch">
          <button type="button" :class="{ on: mode === 'login' }" @click="mode = 'login'">登录</button>
          <button type="button" :class="{ on: mode === 'register' }" @click="mode = 'register'">注册</button>
        </div>

        <h2 class="form-title">{{ mode === 'login' ? '登录你的账号' : '创建新账号' }}</h2>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="用户名" autocomplete="username" />
          </el-form-item>

          <template v-if="mode === 'register'">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="用于会议邀请和通知" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="选填" />
            </el-form-item>
          </template>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="密码" autocomplete="current-password" />
          </el-form-item>

          <el-button class="submit el-button--marker" :loading="submitting" @click="submit">
            {{ mode === 'login' ? '登录' : '注册并登录' }}
          </el-button>
        </el-form>

        <p class="foot-note">
          {{ mode === 'login' ? '还没有账号？' : '已经有账号了？' }}
          <button type="button" class="link" @click="mode = mode === 'login' ? 'register' : 'login'">
            {{ mode === 'login' ? '去注册' : '去登录' }}
          </button>
        </p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const MINI = [
  { name: '创新厅', blocks: [{ left: 6, width: 14 }, { left: 46, width: 20, mine: true }] },
  { name: '洽谈室 A', blocks: [{ left: 22, width: 10 }, { left: 62, width: 12 }] },
  { name: '培训室', blocks: [{ left: 70, width: 24 }] },
  { name: '头脑风暴室', blocks: [{ left: 12, width: 18, mine: true }] }
]

const mode = ref<'login' | 'register'>('login')
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: ''
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名 3–50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码 6–20 个字符', trigger: 'blur' }
  ]
})

watch(mode, () => {
  formRef.value?.clearValidate()
})

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (mode.value === 'register') {
        await auth.register({
          username: form.username,
          password: form.password,
          realName: form.realName || undefined,
          phone: form.phone || undefined
        })
      }
      await auth.login(form.username, form.password)
      ElMessage.success('登录成功')
      const redirect = (route.query.redirect as string) || '/board'
      router.push(redirect)
    } catch {
      // interceptor surfaces the reason
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
}

/* ---------- left ---------- */
.aside {
  position: relative;
  background: var(--ink);
  color: #eceee9;
  padding: 40px 48px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
    background-size: 34px 34px;
    pointer-events: none;
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
}

.brand-mark {
  width: 15px;
  height: 15px;
  background: var(--marker);
}

.brand-name {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 16px;
  color: #fff;
}

.pitch {
  margin-top: auto;
  margin-bottom: 32px;
  position: relative;

  h1 {
    color: #fff;
    font-size: clamp(28px, 3.4vw, 44px);
    line-height: 1.08;
    letter-spacing: -0.03em;
  }

  p {
    margin: 18px 0 0;
    max-width: 42ch;
    color: #a9b0aa;
    font-size: 15px;
    line-height: 1.6;
  }
}

.mini {
  position: relative;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
  padding-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-row {
  display: grid;
  grid-template-columns: 88px 1fr;
  align-items: center;
  gap: 12px;
}

.mini-name {
  font-size: 12px;
  color: #a9b0aa;
}

.mini-track {
  position: relative;
  height: 14px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.06);
}

.mini-block {
  position: absolute;
  top: 0;
  bottom: 0;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.22);

  &.mine {
    background: var(--marker);
  }
}

/* ---------- right ---------- */
.form-side {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: var(--paper);
}

.form-card {
  width: 100%;
  max-width: 360px;
}

.switch {
  display: inline-flex;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-bottom: 24px;

  button {
    border: none;
    background: var(--surface);
    font: inherit;
    font-weight: 500;
    padding: 6px 18px;
    cursor: pointer;
    color: var(--ink-2);

    &.on {
      background: var(--ink);
      color: #fff;
    }
  }
}

.form-title {
  font-size: 22px;
  margin-bottom: 22px;
}

.submit {
  width: 100%;
  height: 40px;
  margin-top: 4px;
}

.foot-note {
  margin-top: 18px;
  font-size: 13px;
  color: var(--ink-2);
}

.link {
  border: none;
  background: none;
  font: inherit;
  color: var(--ink);
  text-decoration: underline;
  cursor: pointer;
  padding: 0;
}

@media (max-width: 860px) {
  .login {
    grid-template-columns: 1fr;
  }

  .aside {
    padding: 28px 24px;
  }

  .pitch {
    margin-top: 40px;
  }

  .mini {
    display: none;
  }
}
</style>
