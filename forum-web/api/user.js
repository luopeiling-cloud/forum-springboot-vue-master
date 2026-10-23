/**
 * 用户相关 API
 * 路径前缀: /platform/user/
 */
import request from '@/utils/request'

/**
 * 获取用户的作品/点赞/收藏列表
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {string} userId
 * @param {number} type - 1=笔记, 2=点赞, 3=收藏
 */
export function getTrendPageByUser(currentPage, pageSize, userId, type) {
	return request.get(
		`/platform/user/getTrendPageByUser/${currentPage}/${pageSize}`,
		{ userId, type }
	)
}

/**
 * 根据用户 ID 获取用户信息
 * @param {string} userId
 */
export function getUserById(userId) {
	return request.get('/platform/user/getUserById', { userId })
}

/**
 * 更新用户信息
 * @param {Object} data - User 对象
 */
export function updateUser(data) {
	return request.post('/platform/user/updateUser', data)
}

/**
 * 根据关键词搜索用户
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {string} keyword
 */
export function getUserPageByKeyword(currentPage, pageSize, keyword) {
	return request.get(
		`/platform/user/getUserPageByKeyword/${currentPage}/${pageSize}`,
		{ keyword }
	)
}
