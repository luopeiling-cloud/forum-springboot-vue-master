<template>
	<view class="messages-page">
		<view class="msg-tabs">
			<view v-for="(tab, idx) in tabs" :key="idx" class="msg-tab" :class="{ active: activeTab === idx }" @click="switchTab(idx)">
				<text>{{ tab.label }}</text>
				<text class="tab-badge" v-if="tab.count > 0">{{ tab.count > 99 ? '99+' : tab.count }}</text>
			</view>
		</view>

		<view class="msg-content">
			<!-- 赞和收藏 / 评论 / 关注 统一事件列表 -->
			<view v-if="activeTab !== 3" class="notice-list">
				<view class="notice-item" v-for="item in eventList" :key="item.id" @click="openEvent(item)">
					<image :src="getFullUrl(item.fromAvatar)" mode="aspectFill" class="notice-avatar" />
					<view class="notice-body">
						<view class="notice-text">
							<text class="notice-name">{{ item.fromUsername }}</text>
							<text> {{ getEventText(item) }}</text>
						</view>
						<text class="notice-content" v-if="item.content">{{ item.content }}</text>
						<text class="notice-content" v-if="item.noteTitle">{{ item.noteTitle }}</text>
						<text class="notice-time">{{ formateTime(item.time) }}</text>
					</view>
					<image v-if="item.noteCover" :src="getFullUrl(item.noteCover)" mode="aspectFill" class="notice-thumb" />
				</view>
				<empty-state :text="emptyText" v-if="eventList.length === 0" />
			</view>

			<!-- 私信聊天列表 -->
			<view v-if="activeTab === 3" class="chat-list">
				<view class="chat-item" v-for="user in chatUsers" :key="user.uid || user.id" @click="openChat(user)">
					<image :src="getFullUrl(user.avatar)" mode="aspectFill" class="chat-avatar" />
					<view class="chat-info">
						<view class="chat-top">
							<text class="chat-name">{{ user.username }}</text>
							<text class="chat-time">{{ formateTime(user.timestamp) }}</text>
						</view>
						<view class="chat-bottom">
							<text class="chat-preview ellipsis">{{ user.content || '暂无消息' }}</text>
							<text class="chat-badge" v-if="user.count > 0">{{ user.count }}</text>
						</view>
					</view>
				</view>
				<empty-state text="暂无聊天消息" v-if="chatUsers.length === 0" />
			</view>
		</view>
	</view>
</template>

<script>
import EmptyState from '@/components/empty-state.vue'
import { requireLogin } from '@/utils/auth.js'
import { getChatUserList, clearMessageCount } from '@/api/im.js'
import { getEventPage, getUnreadCount, readAll } from '@/api/event.js'
import { formateTime, getFullUrl } from '@/utils/util.js'

export default {
	components: { EmptyState },
	data() {
		return {
			activeTab: 3,
			chatUsers: [],
			eventList: [],
			unread: { likeCount: 0, commentCount: 0, collectionCount: 0, followCount: 0 }
		}
	},
	computed: {
		tabs() {
			const u = this.unread
			const chatTotal = this.chatUsers.reduce((s, u) => s + (u.count || 0), 0)
			return [
				{ label: '赞和收藏', count: (u.likeCount || 0) + (u.collectionCount || 0) },
				{ label: '评论和@', count: u.commentCount || 0 },
				{ label: '新增关注', count: u.followCount || 0 },
				{ label: '我的消息', count: chatTotal }
			]
		},
		emptyText() {
			const map = { 0: '暂无赞藏通知', 1: '暂无评论通知', 2: '暂无新增关注' }
			return map[this.activeTab] || '暂无通知'
		}
	},
	onShow() {
		if (!requireLogin()) return
		this.fetchUnread()
		if (this.activeTab === 3) this.loadChatUsers()
		else this.loadEvents()
	},
	methods: {
		formateTime, getFullUrl,

		switchTab(idx) {
			this.activeTab = idx
			if (idx === 3) this.loadChatUsers()
			else {
				this.loadEvents()
				this.markRead(idx)
			}
		},

		async fetchUnread() {
			try {
				const res = await getUnreadCount()
				if (res && res.data) this.unread = res.data
			} catch (e) {}
		},

		async markRead(idx) {
			const typeMap = { 0: 1, 1: 2, 2: 4 }
			// 赞和收藏 tab(idx=0): 标记 type=1(赞)+3(收藏) 已读
			if (idx === 0) {
				readAll(1).catch(() => {})
				readAll(3).catch(() => {})
				this.unread.likeCount = 0
				this.unread.collectionCount = 0
			} else if (typeMap[idx]) {
				readAll(typeMap[idx]).catch(() => {})
				if (idx === 1) this.unread.commentCount = 0
				if (idx === 2) this.unread.followCount = 0
			}
		},

		async loadEvents() {
			let eventType = null
			if (this.activeTab === 0) eventType = 1 // 查 type=1+3（后端合并）
			else if (this.activeTab === 1) eventType = 2
			else if (this.activeTab === 2) eventType = 4
			try {
				const res = await getEventPage(1, 50, eventType)
				this.eventList = (res && res.data && res.data.records) ? res.data.records : []
			} catch (e) { this.eventList = [] }
		},

		async loadChatUsers() {
			try {
				const res = await getChatUserList()
				this.chatUsers = (res && res.data) ? res.data : []
			} catch (e) {}
		},

		getEventText(item) {
			if (item.eventType === 2) {
				return item.targetType === 2 ? '回复了你的评论' : '评论了你的笔记'
			}
			const m = { 1: '赞了你的笔记', 3: '收藏了你的笔记', 4: '关注了你' }
			return m[item.eventType] || '互动了你'
		},

		openEvent(item) {
			if (!item.url) return
			uni.navigateTo({ url: item.url })
		},

		openChat(user) {
			const uid = user.uid || user.id
			clearMessageCount(uid, 3).catch(() => {})
			uni.navigateTo({ url: '/pages/messages/chat?userId=' + uid })
		}
	}
}
</script>

<style scoped>
.messages-page { min-height: 100vh; background: #f5f5f5; }
.msg-tabs { display: flex; background: #fff; border-bottom: 1px solid #f5f5f5; position: sticky; top: 0; z-index: 10; }
.msg-tab { flex: 1; text-align: center; padding: 25rpx 0; font-size: 28rpx; color: #666; position: relative; }
.msg-tab.active { color: #333; font-weight: 600; border-bottom: 4rpx solid #1677ff; }
.tab-badge { position: absolute; top: 10rpx; right: 10rpx; background: #f53f3f; color: #fff; font-size: 20rpx; padding: 2rpx 10rpx; border-radius: 999rpx; }
.chat-item { display: flex; align-items: center; padding: 25rpx; background: #fff; border-bottom: 1px solid #f5f5f5; }
.chat-avatar { width: 90rpx; height: 90rpx; border-radius: 50%; margin-right: 20rpx; }
.chat-info { flex: 1; }
.chat-top, .chat-bottom { display: flex; justify-content: space-between; align-items: center; }
.chat-name { font-size: 30rpx; color: #333; font-weight: 500; }
.chat-time { font-size: 22rpx; color: #999; }
.chat-preview { font-size: 26rpx; color: #999; flex: 1; margin-top: 8rpx; }
.chat-badge { background: #f53f3f; color: #fff; font-size: 22rpx; padding: 4rpx 14rpx; border-radius: 999rpx; margin-left: 15rpx; }
.notice-item { display: flex; align-items: center; padding: 25rpx; background: #fff; border-bottom: 1px solid #f5f5f5; }
.notice-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; margin-right: 20rpx; }
.notice-body { flex: 1; }
.notice-text { font-size: 26rpx; color: #333; margin-bottom: 8rpx; }
.notice-name { color: #333; font-weight: 500; }
.notice-content { font-size: 26rpx; color: #999; display: block; margin-bottom: 4rpx; }
.notice-time { font-size: 22rpx; color: #ccc; }
.notice-thumb { width: 80rpx; height: 80rpx; border-radius: 8rpx; margin-left: 15rpx; flex-shrink: 0; }
.btn-follow-sm { width: 120rpx; height: 56rpx; line-height: 56rpx; font-size: 24rpx; background: #1677ff; color: #fff; border-radius: 999rpx; border: none; padding: 0; text-align: center; }
</style>
