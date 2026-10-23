<template>
	<view class="chat-page">
		<!-- 消息列表 -->
		<scroll-view
			class="chat-scroll"
			scroll-y
			:scroll-into-view="scrollToId"
			@scrolltoupper="loadHistory"
		>
			<view class="chat-list">
				<chat-message
					v-for="(msg, idx) in messageList"
					:key="idx"
					:message="msg"
					:isMe="msg.sendUid === myUserId"
					:avatarUrl="msg.sendUid === myUserId ? myAvatar : otherAvatar"
				/>
			</view>
			<view id="msg-bottom"></view>
		</scroll-view>

		<!-- 发送栏 -->
		<view class="chat-input-bar safe-bottom">
			<input
				class="chat-input"
				v-model="inputText"
				placeholder="输入消息..."
				confirm-type="send"
				@confirm="sendMessage"
			/>
			<button class="chat-send-btn" @click="sendMessage">发送</button>
		</view>
	</view>
</template>

<script>
	import ChatMessage from '@/components/chat-message.vue'
import { requireLogin } from '@/utils/auth.js'
	import { getAllChatRecord, sendMsg } from '@/api/im.js'
	import { getUserById } from '@/api/user.js'
	import userStore from '@/store/userStore.js'
	import imStore from '@/store/imStore.js'
	import wsManager from '@/utils/websocket.js'

	export default {
		components: { ChatMessage },
		data() {
			return {
				acceptUserId: '',
				otherUser: {},
				messageList: [],
				inputText: '',
				currentPage: 1,
				pageSize: 20,
				total: 0,
				scrollToId: '',
				myUserId: '',
				myAvatar: ''
			}
		},
		computed: {
			otherAvatar() {
				return this.otherUser.avatar || ''
			}
		},
		onLoad(options) {
			if (!requireLogin()) return
			this.acceptUserId = options.userId || ''
			this.myUserId = userStore.getUserId()
			const myInfo = userStore.getUserInfo()
			if (myInfo) {
				this.myAvatar = myInfo.avatar || ''
			}
			if (this.acceptUserId) {
				this.loadOtherUser()
				this.loadMessages()
			}
			// 监听 WebSocket 消息
			wsManager.onChatMessage = (msg) => {
				if (msg.sendUid === this.acceptUserId || msg.acceptUid === this.acceptUserId) {
					this.messageList.push(msg)
					this.$nextTick(() => {
						this.scrollToBottom()
					})
				}
			}
		},
		onUnload() {
			wsManager.onChatMessage = null
		},
		methods: {
			async loadOtherUser() {
				try {
					const res = await getUserById(this.acceptUserId)
					if (res && res.data) {
						this.otherUser = res.data
					}
				} catch (e) {}
			},
			async loadMessages() {
				try {
					const res = await getAllChatRecord(this.currentPage, this.pageSize, this.acceptUserId)
					if (res && res.data) {
						const { records, total } = res.data
						this.total = total || 0
						// 倒序排列（最新在底部）
						this.messageList = (records || []).reverse()
						this.$nextTick(() => {
								this.scrollToBottom()
						})
					}
				} catch (e) {}
			},
			async loadHistory() {
				if (this.messageList.length >= this.total) return
				this.currentPage++
				try {
					const res = await getAllChatRecord(this.currentPage, this.pageSize, this.acceptUserId)
					if (res && res.data) {
						const { records } = res.data
						this.messageList = [...(records || []).reverse(), ...this.messageList]
					}
				} catch (e) {
					this.currentPage--
				}
			},
			async sendMessage() {
				const text = this.inputText.trim()
				if (!text) return
				const msg = {
					sendUid: this.myUserId,
					acceptUid: this.acceptUserId,
					content: text,
					msgType: 1,
					chatType: 0
				}
				try {
					await sendMsg(msg)
					this.messageList.push(msg)
					this.inputText = ''
					this.$nextTick(() => this.scrollToBottom())
				} catch (e) {}
			},
			scrollToBottom() {
				this.scrollToId = 'msg-bottom'
			}
		}
	}
</script>

<style scoped>
	.chat-page {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background: #f5f5f5;
	}
	.chat-scroll {
		flex: 1;
		overflow-y: auto;
	}
	.chat-list {
		padding-bottom: 20rpx;
	}
	.chat-input-bar {
		display: flex;
		align-items: center;
		padding: 15rpx 20rpx;
		background: #fff;
		border-top: 1px solid #eee;
	}
	.chat-input {
		flex: 1;
		height: 70rpx;
		line-height: 70rpx;
		padding: 0 20rpx;
		border: 1px solid #eee;
		border-radius: 35rpx;
		font-size: 28rpx;
		background: #fff;
	}
	.chat-send-btn {
		width: 120rpx;
		height: 70rpx;
		line-height: 70rpx;
		background: #007aff;
		color: #fff;
		border-radius: 35rpx;
		font-size: 26rpx;
		margin-left: 15rpx;
		border: none;
		padding: 0;
		text-align: center;
	}
</style>
