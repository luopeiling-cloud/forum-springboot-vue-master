/**
 * 即时通讯 API
 * 路径前缀: /im/chat/
 */
import request from '@/utils/request'

/**
 * 获取所有聊天的未读消息数
 */
export function getCountMessage() {
	return request.get('/im/chat/getCountMessage')
}

/**
 * 获取聊天用户列表（含最后一条消息 + 未读数）
 */
export function getChatUserList() {
	return request.get('/im/chat/getChatUserList')
}

/**
 * 清除未读消息数
 * @param {string} sendUid - 发送方用户 ID
 * @param {number} type - 消息类型 (0=赞藏, 1=评论, 2=关注)
 */
export function clearMessageCount(sendUid, type) {
	return request.get('/im/chat/clearMessageCount', { sendUid, type })
}

/**
 * 获取聊天记录（分页）
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {string} acceptUid - 对方用户 ID
 */
export function getAllChatRecord(currentPage, pageSize, acceptUid) {
	return request.get(`/im/chat/getAllChatRecord/${currentPage}/${pageSize}`, { acceptUid })
}

/**
 * 发送消息
 * @param {Object} data - { sendUid, acceptUid, content, msgType, chatType }
 */
export function sendMsg(data) {
	return request.post('/im/chat/sendMsg', data)
}

/**
 * 删除聊天用户
 * @param {string} sendUid
 * @param {string} acceptUid
 */
export function deleteChatUser(sendUid, acceptUid) {
	return request.del('/im/chat/deleteChatUser', { sendUid, acceptUid })
}
