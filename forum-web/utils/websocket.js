/**
 * WebSocket 即时通讯管理
 * 连接后端 IM 服务，处理实时消息推送
 *
 * 消息类型：
 *   msgType=0 → 通知计数更新 (likeOrCollectionCount, commentCount, followCount)
 *   msgType=1 → 聊天消息
 *   msgType=5 → 聊天用户列表更新
 */
import { BASE_URL } from './request.js'

// 经网关（/api/im/** 路由重写为 /im/**，命中 @ServerEndpoint("/im/ws/{uid}")），本地/云端同一份配置
const WS_URL = BASE_URL.replace(/^http/, 'ws') + '/im/ws/'

class WebSocketManager {
	constructor() {
		this.socketTask = null
		this.reconnectTimer = null
		this.reconnectDelay = 3000
		this.uid = ''
		this.isManualClose = false

		// 消息回调
		this.onNotification = null   // msgType=0
		this.onChatMessage = null    // msgType=1
		this.onUserList = null       // msgType=5
	}

	/**
	 * 连接 WebSocket
	 * @param {string} uid - 当前用户 ID
	 */
	connect(uid) {
		if (!uid) return
		this.uid = uid
		this.isManualClose = false

		const url = WS_URL + uid
		console.log('[WS] 正在连接:', url)

		this.socketTask = uni.connectSocket({
			url: url,
			success: () => console.log('[WS] connectSocket 调用成功'),
			fail: (err) => console.error('[WS] connectSocket 失败:', err)
		})

		this.socketTask.onOpen(() => {
			console.log('[WS] 连接已建立')
			this.reconnectDelay = 3000 // 重置重连延迟
		})

		this.socketTask.onMessage((res) => {
			try {
				const message = JSON.parse(res.data)
				console.log('[WS] 收到消息:', message.msgType)
				this._handleMessage(message)
			} catch (e) {
				console.error('[WS] 消息解析失败:', e)
			}
		})

		this.socketTask.onClose((res) => {
			console.log('[WS] 连接关闭, code:', res.code)
			if (!this.isManualClose) {
				this._reconnect()
			}
		})

		this.socketTask.onError((err) => {
			console.error('[WS] 连接错误:', err)
		})
	}

	/**
	 * 处理收到的消息
	 */
	_handleMessage(message) {
		const { msgType, content } = message

		switch (msgType) {
			case 0:
				// 通知计数
				if (this.onNotification) {
					this.onNotification({
						likeOrCollectionCount: content.likeOrCollectionCount || 0,
						commentCount: content.commentCount || 0,
						followCount: content.followCount || 0
					})
				}
				// 更新本地存储（供 TabBar badge 使用）
				const counts = uni.getStorageSync('messageCounts') || {}
				counts.likeOrCollectionCount = content.likeOrCollectionCount || 0
				counts.commentCount = content.commentCount || 0
				counts.followCount = content.followCount || 0
				uni.setStorageSync('messageCounts', counts)
				// 更新 TabBar badge
				this.updateTabBarBadge()
				break

			case 1:
				// 聊天消息
				if (this.onChatMessage) {
					this.onChatMessage(message)
				}
				break

			case 5:
				// 用户列表
				if (this.onUserList) {
					this.onUserList(message.content)
				}
				break

			default:
				console.log('[WS] 未知消息类型:', msgType)
		}
	}

	/**
	 * 更新底部 TabBar 的未读消息 badge
	 */
	updateTabBarBadge() {
		const counts = uni.getStorageSync('messageCounts') || {}
		const total = (counts.likeOrCollectionCount || 0) +
			(counts.commentCount || 0) +
			(counts.followCount || 0) +
			(counts.chatCount || 0)

		if (total > 0) {
			uni.setTabBarBadge({
				index: 3, // 消息 Tab 索引
				text: total > 99 ? '99+' : total.toString()
			})
		} else {
			uni.removeTabBarBadge({ index: 3 })
		}
	}

	/**
	 * 重连
	 */
	_reconnect() {
		if (this.isManualClose) return
		if (this.reconnectTimer) clearTimeout(this.reconnectTimer)

		this.reconnectTimer = setTimeout(() => {
			console.log('[WS] 尝试重连...')
			this.connect(this.uid)
			// 指数退避，最大 30s
			this.reconnectDelay = Math.min(this.reconnectDelay * 1.5, 30000)
		}, this.reconnectDelay)
	}

	/**
	 * 发送消息
	 */
	send(message) {
		if (!this.socketTask) {
			console.error('[WS] 未连接，无法发送消息')
			return
		}
		this.socketTask.send({
			data: JSON.stringify(message),
			success: () => console.log('[WS] 消息已发送'),
			fail: (err) => console.error('[WS] 发送失败:', err)
		})
	}

	/**
	 * 主动关闭连接
	 */
	close() {
		this.isManualClose = true
		if (this.reconnectTimer) {
			clearTimeout(this.reconnectTimer)
			this.reconnectTimer = null
		}
		if (this.socketTask) {
			this.socketTask.close()
			this.socketTask = null
		}
	}
}

// 单例
const wsManager = new WebSocketManager()
export default wsManager
