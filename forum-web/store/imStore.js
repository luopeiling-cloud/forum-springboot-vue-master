/**
 * IM / 消息状态管理
 * 从 forum-web imStore.ts 迁移
 */

const state = {
	userList: [],       // 聊天用户列表
	message: {},        // 最新收到的 WebSocket 消息
	countMessage: {
		chatCount: 0,
		likeOrCollectionCount: 0,
		commentCount: 0,
		followCount: 0
	}
}

const imStore = {
	state,

	/** 设置聊天用户列表 */
	setUserList(data) {
		state.userList = data || []
	},

	/** 设置消息计数 */
	setCountMessage(data) {
		if (!data) return
		state.countMessage = {
			chatCount: data.chatCount || 0,
			likeOrCollectionCount: data.likeOrCollectionCount || 0,
			commentCount: data.commentCount || 0,
			followCount: data.followCount || 0
		}
		// 更新本地存储
		uni.setStorageSync('messageCounts', JSON.stringify(state.countMessage))
	},

	/** 设置最新消息 */
	setMessage(data) {
		state.message = data || {}
	},

	/** 获取总未读数 */
	getTotalUnread() {
		const cm = state.countMessage
		return (cm.chatCount || 0) + (cm.likeOrCollectionCount || 0) +
			(cm.commentCount || 0) + (cm.followCount || 0)
	}
}

export default imStore
