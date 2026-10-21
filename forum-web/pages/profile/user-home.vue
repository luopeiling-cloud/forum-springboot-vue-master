<template>
	<view class="user-home-page">
		<!-- 用户信息头部 -->
		<view class="profile-header">
			<image :src="getFullUrl(userInfo.avatar)" mode="aspectFill" class="profile-avatar" />
			<view class="profile-name">{{ userInfo.username || '未登录' }}</view>
			<view class="profile-id" v-if="userInfo.yxId">校园号：{{ userInfo.yxId }}</view>
			<view class="profile-desc">{{ userInfo.description || '这个人什么都没有写~' }}</view>
			<view class="profile-tags"><view class="p-tag">校园用户</view></view>
			<view class="profile-stats">
				<view class="stat-item">
					<text class="stat-num">{{ userInfo.trendCount || 0 }}</text>
					<text class="stat-label">作品</text>
				</view>
				<view class="stat-item" @click="toFollowList">
					<text class="stat-num">{{ userInfo.followerCount || 0 }}</text>
					<text class="stat-label">关注</text>
				</view>
				<view class="stat-item" @click="toFanList">
					<text class="stat-num">{{ userInfo.fanCount || 0 }}</text>
					<text class="stat-label">粉丝</text>
				</view>
			</view>
			<view class="profile-actions">
				<button class="btn-chat" @click="openChat">私信</button>
				<button class="btn-action" :class="isFollowed ? 'btn-followed' : ''" @click="onFollow">
					{{ isFollowed ? '已关注' : '+ 关注' }}
				</button>
			</view>
		</view>

		<view class="waterfall-container">
			<view class="waterfall-col" v-for="(col, colIdx) in columns" :key="colIdx">
				<note-card v-for="item in col" :key="item.id" :item="item"></note-card>
			</view>
		</view>
		<loading-more :loading="loading" :noMore="noMore" />
		<empty-state text="暂无内容" v-if="!loading && noteList.length === 0" />
	</view>
</template>

<script>
import NoteCard from '@/components/note-card.vue'
import LoadingMore from '@/components/loading-more.vue'
import EmptyState from '@/components/empty-state.vue'
import { getUserById, getTrendPageByUser } from '@/api/user.js'
import { followById, isFollow } from '@/api/follower.js'
import { getFullUrl } from '@/utils/util.js'

export default {
	components: { NoteCard, LoadingMore, EmptyState },
	data() {
		return {
			userId: '',
			userInfo: {},
			isFollowed: false,
			noteList: [],
			currentPage: 1,
			pageSize: 10,
			total: 0,
			loading: false,
			noMore: false
		}
	},
	computed: {
		columns() {
			const cols = [[], []]
			this.noteList.forEach((item, idx) => cols[idx % 2].push(item))
			return cols
		}
	},
	onLoad(options) {
		this.userId = options.userId || ''
	},
	onShow() {
		if (this.userId) {
			this.loadUserInfo()
			this.loadNoteList()
			this.checkFollow()
		}
	},
	onReachBottom() {
		this.loadMore()
	},
	methods: {
		getFullUrl,

		async loadUserInfo() {
			try {
				const res = await getUserById(this.userId)
				if (res && res.data) this.userInfo = res.data
			} catch (e) {}
		},

		async loadNoteList() {
			this.loading = true
			this.currentPage = 1
			this.noteList = []
			this.noMore = false
			try {
				const res = await getTrendPageByUser(this.currentPage, this.pageSize, this.userId, 1)
				this.setData(res)
			} catch (e) { this.loading = false }
		},

		setData(res) {
			this.loading = false
			if (res && res.data) {
				const { records, total } = res.data
				this.total = total || 0
				this.noteList = this.currentPage === 1 ? (records || []) : [...this.noteList, ...(records || [])]
				this.noMore = this.noteList.length >= this.total
			}
		},

		async loadMore() {
			if (this.loading || this.noMore) return
			this.loading = true
			this.currentPage++
			try {
				const res = await getTrendPageByUser(this.currentPage, this.pageSize, this.userId, 1)
				this.setData(res)
			} catch (e) { this.loading = false; this.currentPage-- }
		},

		async checkFollow() {
			try {
				const res = await isFollow(this.userId)
				this.isFollowed = !!(res && res.data)
			} catch (e) {}
		},

		async onFollow() {
			try {
				await followById(this.userId)
				this.isFollowed = !this.isFollowed
			} catch (e) {}
		},

		openChat() {
			uni.navigateTo({ url: '/pages/messages/chat?userId=' + this.userId })
		},

		toFollowList() {
			uni.navigateTo({ url: '/pages/profile/follow-list?type=1' })
		},

		toFanList() {
			uni.navigateTo({ url: '/pages/profile/follow-list?type=2' })
		}
	}
}
</script>

<style scoped>
.user-home-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 30rpx; }
.profile-header { background: #fff; padding: 60rpx 40rpx 30rpx; text-align: center; }
.profile-avatar { width: 160rpx; height: 160rpx; border-radius: 50%; margin-bottom: 20rpx; }
.profile-name { font-size: 40rpx; font-weight: 600; color: #333; margin-bottom: 10rpx; }
.profile-id { font-size: 24rpx; color: #999; margin-bottom: 15rpx; }
.profile-desc { font-size: 28rpx; color: #666; margin-bottom: 20rpx; }
.profile-tags { display: flex; justify-content: center; gap: 15rpx; margin-bottom: 30rpx; }
.p-tag { padding: 8rpx 24rpx; background: rgba(0,0,0,0.03); border-radius: 999rpx; font-size: 24rpx; color: #666; }
.profile-stats { display: flex; justify-content: center; gap: 60rpx; margin-bottom: 20rpx; }
.stat-item { text-align: center; }
.stat-num { font-size: 30rpx; font-weight: 600; color: #333; display: block; }
.stat-label { font-size: 24rpx; color: #999; }
.profile-actions { display: flex; justify-content: center; gap: 20rpx; margin-top: 20rpx; }
.btn-chat, .btn-action { width: 200rpx; height: 64rpx; line-height: 64rpx; font-size: 26rpx; border-radius: 999rpx; border: none; padding: 0; }
.btn-chat { background: #fff; color: #333; border: 1px solid #e5e5e5; }
.btn-action { background: #f53f3f; color: #fff; }
.btn-followed { background: #999; }
.waterfall-container { display: flex; padding: 0 15rpx; gap: 15rpx; margin-top: 20rpx; }
.waterfall-col { flex: 1; min-width: 0; }
</style>
