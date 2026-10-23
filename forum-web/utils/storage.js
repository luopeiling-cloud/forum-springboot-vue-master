/**
 * 本地存储封装
 * uni-app 使用 uni.setStorageSync / uni.getStorageSync
 */

const storage = {
	/**
	 * 存储数据
	 * @param {string} key
	 * @param {*} value
	 */
	set(key, value) {
		try {
			uni.setStorageSync(key, JSON.stringify(value))
		} catch (e) {
			console.error('storage.set error:', e)
		}
	},

	/**
	 * 读取数据
	 * @param {string} key
	 * @returns {*} 解析后的数据，不存在返回 null
	 */
	get(key) {
		try {
			const value = uni.getStorageSync(key)
			if (value && value !== 'undefined' && value !== 'null') {
				return JSON.parse(value)
			}
			return null
		} catch (e) {
			console.error('storage.get error:', e)
			return null
		}
	},

	/**
	 * 删除数据
	 * @param {string} key
	 */
	remove(key) {
		try {
			uni.removeStorageSync(key)
		} catch (e) {
			console.error('storage.remove error:', e)
		}
	},

	/**
	 * 清空所有数据
	 */
	clear() {
		try {
			uni.clearStorageSync()
		} catch (e) {
			console.error('storage.clear error:', e)
		}
	}
}

export default storage
