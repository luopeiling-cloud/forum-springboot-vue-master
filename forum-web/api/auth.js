/**
 * 认证相关 API
 * 路径前缀: /auth/auth/
 */
import request from '@/utils/request'

/**
 * 账号密码登录
 * @param {Object} data - { username, password }
 * @returns {Promise} { accessToken, refreshToken, userInfo }
 */
export function login(data) {
	return request.post('/auth/auth/login', data)
}

/**
 * 手机/邮箱验证码登录
 * @param {Object} data - { phone, email, code }
 */
export function loginByCode(data) {
	return request.post('/auth/auth/loginByCode', data)
}

/**
 * 用户注册
 * @param {Object} data - { username, password, phone, email }
 */
export function register(data) {
	return request.post('/auth/auth/register', data)
}

/**
 * 检查用户是否已注册
 * @param {Object} data - { username | phone | email }
 */
export function isRegister(data) {
	return request.post('/auth/auth/isRegister', data)
}

/**
 * 根据 Token 获取用户信息
 * @param {string} accessToken
 */
export function getUserInfoByToken(accessToken) {
	return request.get('/auth/auth/getUserInfoByToken', { accessToken })
}

/**
 * 刷新 Token
 * @param {string} refreshToken
 */
export function refreshToken(refreshToken) {
	return request.get('/auth/auth/refreshToken', { refreshToken })
}

/**
 * 退出登录
 * @param {string} userId
 */
export function loginOut(userId) {
	return request.get('/auth/auth/loginOut', { userId })
}

/**
 * 修改密码
 * @param {Object} data
 */
export function updatePassword(data) {
	return request.post('/auth/auth/updatePassword', data)
}
