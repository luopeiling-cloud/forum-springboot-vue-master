/**
 * 评论相关 API
 * 路径前缀: /platform/comment/
 */
import request from '@/utils/request'

/**
 * 获取一级评论（含二级评论预览）
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {string} noteId
 */
export function getCommentPageWithCommentByNoteId(currentPage, pageSize, noteId) {
	return request.get(
		`/platform/comment/getCommentPageWithCommentByNoteId/${currentPage}/${pageSize}`,
		{ noteId }
	)
}

/**
 * 获取二级评论（回复列表）
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {string} oneCommentId - 一级评论 ID
 */
export function getTwoCommentPageByOneCommentId(currentPage, pageSize, oneCommentId) {
	return request.get(
		`/platform/comment/getTwoCommentPageByOneCommentId/${currentPage}/${pageSize}`,
		{ oneCommentId }
	)
}

/**
 * 发布评论
 * @param {Object} data - CommentDTO { nid, noteUid, pid, replyId, replyUid, level, content }
 */
export function saveCommentByDTO(data) {
	return request.post('/platform/comment/saveCommentByDTO', data)
}

/**
 * 同步评论（页面离开时批量同步）
 * @param {Array<string>} commentIds
 */
export function syncCommentByIds(commentIds) {
	return request.post('/platform/comment/syncCommentByIds', commentIds)
}

/**
 * 获取评论通知列表
 * @param {number} currentPage
 * @param {number} pageSize
 */
export function getNoticeComment(currentPage, pageSize) {
	return request.get(`/platform/comment/getNoticeComment/${currentPage}/${pageSize}`)
}
