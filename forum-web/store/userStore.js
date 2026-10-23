/**
 * 用户状态管理
 * 从 forum-web userStore.ts 迁移
 */

// #ifdef VUE3
import { reactive, computed } from 'vue'
// #endif

import { BASE_URL } from '@/utils/request.js'

// 使用全局响应式对象（兼容 Vue2/Vue3）
const state = {
	token: '',
	userInfo: null
}

// 初始化：从本地存储恢复状态
function initFromStorage() {
	const accessToken = uni.getStorageSync('accessToken')
	const userInfo = uni.getStorageSync('userInfo')
	if (accessToken) state.token = accessToken
	if (userInfo) {
		try {
			state.userInfo = typeof userInfo === 'string' ? JSON.parse(userInfo) : userInfo
		} catch (e) {
			state.userInfo = userInfo
		}
	}
}

const userStore = {
	state,

	/** 初始化 */
	init() {
		initFromStorage()
	},

	/** 获取 Token */
	getToken() {
		return uni.getStorageSync('accessToken') || ''
	},

	/** 获取用户信息 */
	getUserInfo() {
		const info = uni.getStorageSync('userInfo')
		if (!info) return null
		try {
			return typeof info === 'string' ? JSON.parse(info) : info
		} catch (e) {
			return info
		}
	},

	/** 设置用户信息 */
	setUserInfo(data) {
		if (!data) return
		state.userInfo = data
		uni.setStorageSync('userInfo', JSON.stringify(data))
	},

	/** 登录成功：保存 Token + 用户信息 */
	loginSuccess(data) {
		const { accessToken, refreshToken, userInfo } = data
		if (accessToken) {
			state.token = accessToken
			uni.setStorageSync('accessToken', accessToken)
		}
		if (refreshToken) {
			uni.setStorageSync('refreshToken', refreshToken)
		}
		if (userInfo) {
			this.setUserInfo(userInfo)
		}
	},

	/** 是否已登录 */
	isLoggedIn() {
		return !!this.getToken()
	},

	/** 获取当前用户 ID */
	getUserId() {
		const info = this.getUserInfo()
		return info ? info.id : ''
	},

	/** 刷新 Token */
	async refreshToken() {
		const refreshToken = uni.getStorageSync('refreshToken') || ''
		return new Promise((resolve, reject) => {
			uni.request({
				url: BASE_URL + '/auth/auth/refreshToken',
				method: 'GET',
				data: { refreshToken },
				success: (res) => {
					if (res.data && res.data.code === 200) {
						const { accessToken } = res.data.data
						state.token = accessToken
						uni.setStorageSync('accessToken', accessToken)
						resolve(res.data.data)
					} else {
						reject(res.data)
					}
				},
				fail: reject
			})
		})
	},

	/** 退出登录 */
	loginOut() {
		state.token = ''
		state.userInfo = null
		uni.removeStorageSync('accessToken')
		uni.removeStorageSync('refreshToken')
		uni.removeStorageSync('userInfo')
		uni.removeStorageSync('messageCounts')
		uni.removeTabBarBadge({ index: 3 })
	}
}

// 启动时初始化
initFromStorage()

export default userStore
