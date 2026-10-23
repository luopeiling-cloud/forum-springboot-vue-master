/**
 * forum API 接口封装
 * 基于 uni.request，适配论坛后端接口规范
 *
 * 后端响应格式：{ code: 200, message: "success", data: T }
 * code=501 → Token 过期需刷新
 * code=401 → refreshToken 也过期，跳转登录
 */

// 后端接口根路径（唯一配置点，websocket/util/note/edit 等均从此派生）
export const BASE_URL = 'http://121.41.121.171:88/api'

// 是否正在刷新 Token
let isRefreshing = false
// 等待刷新的请求队列
let requestQueue = []

/**
 * 获取请求头（携带 accessToken + userId）
 */
function getHeader(contentType = 'application/json;charset=UTF-8') {
	const accessToken = uni.getStorageSync('accessToken') || ''
	const userInfo = uni.getStorageSync('userInfo')
	const userId = userInfo ? userInfo.id : ''
	return {
		'Content-Type': contentType,
		'accessToken': accessToken,
		'userId': userId
	}
}

/**
 * 刷新 Token
 */
function refreshTokenRequest() {
	const refreshToken = uni.getStorageSync('refreshToken') || ''
	return new Promise((resolve, reject) => {
		uni.request({
			url: BASE_URL + '/auth/auth/refreshToken',
			method: 'GET',
			data: { refreshToken },
			success: (res) => {
				if (res.data && res.data.code === 200) {
					const { accessToken } = res.data.data
					uni.setStorageSync('accessToken', accessToken)
					resolve(accessToken)
				} else {
					reject(res.data)
				}
			},
			fail: reject
		})
	})
}

/**
 * 统一错误提示
 */
function showError(msg) {
	uni.showToast({
		title: msg || '请求失败',
		icon: 'none',
		duration: 2000
	})
}

/**
 * 处理响应
 */
function handleResponse(res, resolve, reject, config, method) {
	const { code, data, message } = res.data || {}

	if (code === 200) {
		resolve(res.data) // 返回完整 { code, data, message }
	} else if (code === 501) {
		// Token 过期，尝试刷新
		if (!isRefreshing) {
			isRefreshing = true
			refreshTokenRequest().then((newToken) => {
				isRefreshing = false
				// 重放等待队列
				requestQueue.forEach(cb => cb(newToken))
				requestQueue = []
				// 重试当前请求
				config.header.accessToken = newToken
				doRequest(config, method).then(resolve).catch(reject)
			}).catch((err) => {
				isRefreshing = false
				requestQueue = []
				if (err && err.code === 401) {
					uni.removeStorageSync('accessToken')
					uni.removeStorageSync('refreshToken')
					uni.removeStorageSync('userInfo')
					showError('登录已过期，请重新登录')
					// 跳转登录页
					uni.reLaunch({ url: '/pages/login/login' })
				}
				reject(err)
			})
		} else {
			// 正在刷新中，加入等待队列
			requestQueue.push((newToken) => {
				config.header.accessToken = newToken
				doRequest(config, method).then(resolve).catch(reject)
			})
		}
	} else {
		// showError removed
		reject(res.data)
	}
}

/**
 * 执行请求
 */
function doRequest(config, method) {
	return new Promise((resolve, reject) => {
		uni.request({
			...config,
			method: method,
			success: (res) => handleResponse(res, resolve, reject, config, method),
			fail: (err) => {
				showError('网络异常，请检查连接')
				reject(err)
			}
		})
	})
}

const request = {
	/**
	 * GET 请求
	 */
	get(url, data = {}) {
		return doRequest({
			url: BASE_URL + url,
			data,
			header: getHeader()
		}, 'GET')
	},

	/**
	 * POST 请求（JSON）
	 */
	post(url, data = {}) {
		return doRequest({
			url: BASE_URL + url,
			data,
			header: getHeader('application/json;charset=UTF-8')
		}, 'POST')
	},

	/**
	 * POST 请求（FormData / multipart）
	 */
	postFormData(url, formData = {}) {
		return new Promise((resolve, reject) => {
			const accessToken = uni.getStorageSync('accessToken') || ''
			const userInfo = uni.getStorageSync('userInfo')
			const userId = userInfo ? userInfo.id : ''

			uni.uploadFile({
				url: BASE_URL + url,
				filePath: formData.filePath || '',
				name: 'uploadFiles',
				formData: formData.formData || {},
				header: {
					'accessToken': accessToken,
					'userId': userId
				},
				success: (res) => {
					try {
						const data = JSON.parse(res.data)
						if (data.code === 200) {
							resolve(data)
						} else {
							showError(data.message || '上传失败')
							reject(data)
						}
					} catch (e) {
						reject(e)
					}
				},
				fail: (err) => {
					showError('上传失败，请检查网络')
					reject(err)
				}
			})
		})
	},

	/**
	 * 上传多个文件（发布笔记用）
	 */
	uploadNote(files, noteData) {
		return new Promise((resolve, reject) => {
			const accessToken = uni.getStorageSync('accessToken') || ''
			const userInfo = uni.getStorageSync('userInfo')
			const userId = userInfo ? userInfo.id : ''

			// uni-app 一次只能上传一个文件，需逐个上传后用 formData 传递
			// 这里使用 uni.uploadFile 逐个文件上传
			const uploadPromises = files.map((filePath) => {
				return new Promise((res, rej) => {
					uni.uploadFile({
						url: BASE_URL + '/util/oss/saveBatch/1',
						filePath: filePath,
						name: 'uploadFiles',
						header: {
							'accessToken': accessToken,
							'userId': userId
						},
						success: (uploadRes) => {
							try {
								const data = JSON.parse(uploadRes.data)
								if (data.code === 200) {
									res(data.data) // 返回上传后的文件URL列表
								} else {
									rej(data)
								}
							} catch (e) {
								rej(e)
							}
						},
						fail: rej
					})
				})
			})

			Promise.all(uploadPromises).then((urls) => {
				// 所有文件上传完成，调用保存笔记接口
				// urls 是各个文件上传返回的结果数组
				resolve(urls)
			}).catch(reject)
		})
	},

	/**
	 * DELETE 请求
	 */
	del(url, data = {}) {
		return doRequest({
			url: BASE_URL + url,
			data,
			header: getHeader()
		}, 'DELETE')
	}
}

export default request
