import { authApi, type UserInfo } from '@/api/auth'
import router from '@/router/router'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const user = ref<UserInfo | null>(null)
    const accessToken = ref<string | null>(null)
    const refreshToken = ref<string | null>(null)
    const isLoading = ref(false)

    const isLoggedIn = computed(() => !!user.value)
    const userName = computed(() => user.value?.userName || '')
    const isAdmin = computed(() => user.value?.userRole === 'admin')

    const login = async (email: string, password: string): Promise<boolean> => {
      isLoading.value = true

      try {
        const response = await authApi.login(email, password)

        // 토큰을 먼저 저장해야 getMe() 호출 시 인증이 됨
        accessToken.value = response.accessToken
        refreshToken.value = response.refreshToken

        const userInfo = await authApi.getMe()

        user.value = userInfo

        return true
      } catch (err) {
        console.error('Login failed')
        return false
      } finally {
        isLoading.value = false
      }
    }

    const logout = async () => {
      try {
        await authApi.logout()
      } catch (err) {
        console.error('Logout failed')
      } finally {
        accessToken.value = null
        refreshToken.value = null

        user.value = null
        router.replace('/login')
      }
    }

    const getProfile = async () => {
      const response = await authApi.getMe()
      user.value = response
    }

    const updateProfile = async (updates: Partial<UserInfo>) => {
      const { userName, userTel } = updates
      await authApi.updateMe(userName, userTel)
      if (user.value) {
        user.value = { ...user.value, ...updates }
      }
    }

    const join = async (userEmail: string, userPw: string, userName: string, userTel?: string) => {
      isLoading.value = true

      try {
        await authApi.register(userEmail, userPw, userName, userTel)
        return true
      } catch (err) {
        console.error('Registration failed')
        return false
      } finally {
        isLoading.value = false
      }
    }

    // useAuthStore.ts 내 refresh 함수
    const refresh = async () => {
      isLoading.value = true
      try {
        const response = await authApi.refreshToken(refreshToken.value)

        // 리프레쉬 발급 후 at, rt 재설정
        accessToken.value = response.newAccessToken
        refreshToken.value = response.newRefreshToken

        return true
      } catch (err) {
        logout() // 리프레시 실패 시 로그아웃
        console.error('Refresh token expired')
        return false
      } finally {
        isLoading.value = false
      }
    }

    return {
      user,
      accessToken,
      refreshToken,
      isLoading,
      isLoggedIn,
      userName,
      login,
      logout,
      getProfile,
      updateProfile,
      join,
      refresh,
      isAdmin,
    }
  },
  {
    persist: {
      key: 'auth',
      storage: localStorage,
      pick: ['accessToken', 'refreshToken', 'user'],
    },
  },
)
