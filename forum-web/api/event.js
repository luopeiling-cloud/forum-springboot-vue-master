/**
 * 通知事件 API
 */
import request from '@/utils/request'

/**
 * 分页获取通知列表
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {number} eventType - 1=点赞 2=评论 3=收藏 4=关注，不传=全部
 */
export function getEventPage(currentPage, pageSize, eventType) {
	return request.get(`/platform/event/getEventPage/${currentPage}/${pageSize}`, { eventType })
}

/**
 * 获取各类型未读计数
 */
export function getUnreadCount() {
	return request.get('/platform/event/getUnreadCount')
}

/**
 * 标记已读
 * @param {number} eventType - 不传=全部已读
 */
export function readAll(eventType) {
	return request.get('/platform/event/readAll', { eventType })
}
