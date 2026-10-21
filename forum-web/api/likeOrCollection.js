/**
 * 点赞/收藏 API
 * 路径前缀: /platform/likeOrCollection/
 */
import request from '@/utils/request'

/**
 * 点赞或收藏（toggle）
 * @param {Object} data - { likeOrCollectionId, publishUid, type }
 *   type: 1=点赞笔记, 2=点赞评论, 3=收藏笔记, 4=收藏专辑
 */
export function likeOrCollectionByDTO(data) {
	return request.post('/platform/likeOrCollection/likeOrCollectionByDTO', data)
}

/**
 * 是否已点赞或收藏
 * @param {Object} data - { likeOrCollectionId, type }
 */
export function isLikeOrCollection(data) {
	return request.post('/platform/likeOrCollection/isLikeOrCollection', data)
}

/**
 * 获取点赞/收藏通知
 * @param {number} currentPage
 * @param {number} pageSize
 */
export function getNoticeLikeOrCollection(currentPage, pageSize) {
	return request.get(`/platform/likeOrCollection/getNoticeLikeOrCollection/${currentPage}/${pageSize}`)
}
