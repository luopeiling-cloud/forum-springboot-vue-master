/**
 * 鉴权工具 - 全局登录状态检查
 */

/**
 * 检查是否已登录
 * @returns {boolean}
 */
function isLoggedIn() {
	const token = uni.getStorageSync('accessToken')
	return !!token
}

/**
 * 获取当前登录用户信息
 * @returns {Object|null}
 */
function getUserInfo() {
	try {
		const info = uni.getStorageSync('userInfo')
		if (!info) return null
		return typeof info === 'string' ? JSON.parse(info) : info
	} catch (e) {
		return null
	}
}

/**
 * 要求登录 - 未登录则跳转登录页
 * @param {Object} options - { showTip: 是否提示 }
 * @returns {boolean} 是否已登录
 */
function requireLogin(options = {}) {
	if (isLoggedIn()) {
		return true
	}
	if (options.showTip !== false) {
		uni.showToast({
			title: '请先登录',
			icon: 'none',
			duration: 1500
		})
	}
	// 延迟跳转，让 toast 能显示
	setTimeout(() => {
		uni.reLaunch({
			url: '/pages/login/login'
		})
	}, options.showTip !== false ? 1000 : 0)
	return false
}

/**
 * 退出登录 - 清除所有状态并跳转登录页
 */
function logout() {
	uni.removeStorageSync('accessToken')
	uni.removeStorageSync('refreshToken')
	uni.removeStorageSync('userInfo')
	uni.removeStorageSync('messageCounts')
	uni.removeStorageSync('keyword')
	uni.removeStorageSync('seed')
	uni.removeStorageSync('historyRecords')

	// 清除 TabBar 红点
	uni.removeTabBarBadge({ index: 3 }).catch(() => {})

	// 跳转登录页
	uni.reLaunch({
		url: '/pages/login/login'
	})
}

export {
	isLoggedIn,
	getUserInfo,
	requireLogin,
	logout
}
