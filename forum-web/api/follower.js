/**
 * 关注/粉丝 API
 * 路径前缀: /platform/follower/
 */
import request from '@/utils/request'

/**
 * 获取关注用户的动态流
 * @param {number} currentPage
 * @param {number} pageSize
 */
export function getFollowTrendPage(currentPage, pageSize) {
	return request.get(`/platform/follower/getFollowTrendPage/${currentPage}/${pageSize}`)
}

/**
 * 关注/取关用户（toggle）
 * @param {string} followerId - 被关注的用户 ID
 */
export function followById(followerId) {
	return request.get('/platform/follower/followById', { followerId })
}

/**
 * 是否已关注某用户
 * @param {string} followerId
 */
export function isFollow(followerId) {
	return request.get('/platform/follower/isFollow', { followerId })
}

/**
 * 获取关注/粉丝列表
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {number} type - 1=我关注的, 2=关注我的(粉丝)
 */
export function getFriendPage(currentPage, pageSize, type) {
	return request.get(`/platform/follower/getFriendPage/${currentPage}/${pageSize}`, { type })
}

/**
 * 获取新增粉丝通知
 * @param {number} currentPage
 * @param {number} pageSize
 */
export function getNoticeFollower(currentPage, pageSize) {
	return request.get(`/platform/follower/getNoticeFollower/${currentPage}/${pageSize}`)
}
