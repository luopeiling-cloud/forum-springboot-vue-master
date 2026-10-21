<template>
	<view class="profile-page">
		<!-- 用户信息头部 -->
		<view class="profile-header">
			<view class="header-setting" v-if="isSelf" @click="goSetting">⚙</view>
			<image :src="getFullUrl(userInfo.avatar)" mode="aspectFill" class="profile-avatar" />
			<view class="profile-name">{{ userInfo.username || '未登录' }}</view>
			<view class="profile-id" v-if="userInfo.yxId">校园号：{{ userInfo.yxId }}</view>
			<view class="profile-desc">{{ userInfo.description || '这个人什么都没有写~' }}</view>

			<view class="profile-tags">
				<view class="p-tag">校园用户</view>
			</view>

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

			<view class="profile-actions" v-if="!isSelf">
				<button class="btn-chat" @click="openChat">私信</button>
				<button class="btn-action" :class="isFollowed ? 'btn-followed' : ''" @click="onFollow">
					{{ isFollowed ? '已关注' : '+ 关注' }}
				</button>
			</view>
		</view>

		<!-- 内容 Tab -->
		<view class="content-tabs">
			<view
				v-for="(tab, idx) in contentTabs"
				:key="idx"
				class="content-tab"
				:class="{ active: activeContentTab === tab.type }"
				@click="switchContentTab(tab.type)"
			>
				{{ tab.label }}
			</view>
		</view>

		<!-- 笔记/点赞：瀑布流 -->
		<template v-if="activeContentTab !== 3">
			<view class="waterfall-container">
				<view class="waterfall-col" v-for="(col, colIdx) in columns" :key="colIdx">
					<note-card v-for="item in col" :key="item.id" :item="item"></note-card>
				</view>
			</view>
			<loading-more :loading="loading" :noMore="noMore" />
			<empty-state text="暂无内容" v-if="!loading && noteList.length === 0" />
		</template>

		<!-- 收藏：专辑列表 -->
		<template v-if="activeContentTab === 3">
			<view class="album-list-section">
				<view
					class="album-item"
					v-for="album in albumList"
					:key="album.id"
					@click="toAlbumDetail(album)"
				>
					<text class="album-icon">📁</text>
					<view class="album-info">
						<text class="album-title-text">{{ album.title }}</text>
						<text class="album-count-text">{{ album.imgCount || 0 }}张笔记</text>
					</view>
					<text class="album-arrow">></text>
				</view>
				<empty-state text="还没有专辑" v-if="albumList.length === 0 && !albumLoading" />
			</view>

			<!-- 新建专辑 -->
			<view class="add-album-area" v-if="!showAddAlbum">
				<button class="btn-add-album" @click="showAddAlbum = true">+ 新建专辑</button>
			</view>
			<view class="add-album-area" v-else>
				<input
					v-model="newAlbumName"
					placeholder="输入专辑名称"
					maxlength="20"
					class="input-album-name"
					focus
				/>
				<view class="add-album-btns">
					<button class="btn-confirm" @click="createAlbum">创建</button>
					<button class="btn-cancel" @click="showAddAlbum = false; newAlbumName = ''">取消</button>
				</view>
			</view>
		</template>
	</view>
</template>

<script>
import NoteCard from '@/components/note-card.vue'
import { requireLogin } from '@/utils/auth.js'
import LoadingMore from '@/components/loading-more.vue'
import EmptyState from '@/components/empty-state.vue'
import { getUserById, getTrendPageByUser } from '@/api/user.js'
import { followById, isFollow } from '@/api/follower.js'
import { getAlbumPageByUserId, saveAlbumByDTO } from '@/api/album.js'
import { getFullUrl } from '@/utils/util.js'
import userStore from '@/store/userStore.js'

export default {
	components: { NoteCard, LoadingMore, EmptyState },
	data() {
		return {
			userId: '',
			userInfo: {},
			isSelf: false,
			isFollowed: false,
			activeContentTab: 1,
			contentTabs: [
				{ label: '笔记', type: 1 },
				{ label: '点赞', type: 2 },
				{ label: '收藏', type: 3 }
			],
			// 笔记/点赞
			noteList: [],
			currentPage: 1,
			pageSize: 10,
			total: 0,
			loading: false,
			noMore: false,
			columnCount: 2,
			// 专辑
			albumList: [],
			albumLoading: false,
			showAddAlbum: false,
			newAlbumName: ''
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
		if (!options.userId || options.userId === userStore.getUserId()) {
			if (!requireLogin()) return
		}
		this.userId = options.userId || userStore.getUserId()
		this.isSelf = this.userId === userStore.getUserId()
	},
	onShow() {
		if (this.userId) {
			this.loadUserInfo()
			if (this.activeContentTab === 3) {
				this.loadAlbums()
			} else {
				this.loadNoteList()
			}
			if (!this.isSelf) {
				this.checkFollow()
			}
		}
	},
	onReachBottom() {
		if (this.activeContentTab !== 3) {
			this.loadMore()
		}
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
				const res = await getTrendPageByUser(this.currentPage, this.pageSize, this.userId, this.activeContentTab)
				this.setData(res)
			} catch (e) {
				this.loading = false
			}
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
				const res = await getTrendPageByUser(this.currentPage, this.pageSize, this.userId, this.activeContentTab)
				this.setData(res)
			} catch (e) {
				this.loading = false
				this.currentPage--
			}
		},

		switchContentTab(type) {
			this.activeContentTab = type
			if (type === 3) {
				this.loadAlbums()
			} else {
				this.loadNoteList()
			}
		},

		/** 专辑相关 */
		async loadAlbums() {
			this.albumLoading = true
			try {
				const res = await getAlbumPageByUserId(1, 50, this.userId)
				this.albumList = (res && res.data && res.data.records) ? res.data.records : []
			} catch (e) {
				this.albumList = []
			}
			this.albumLoading = false
		},

		async createAlbum() {
			const name = this.newAlbumName.trim()
			if (!name) {
				uni.showToast({ title: '请输入专辑名称', icon: 'none' })
				return
			}
			try {
				await saveAlbumByDTO({ title: name, uid: this.userId })
				this.showAddAlbum = false
				this.newAlbumName = ''
				uni.showToast({ title: '已创建', icon: 'success' })
				this.loadAlbums()
			} catch (e) {
				uni.showToast({ title: '创建失败', icon: 'none' })
			}
		},

		toAlbumDetail(album) {
			uni.navigateTo({ url: '/pages/profile/album-detail?albumId=' + album.id })
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
		},

		goSetting() {
			uni.navigateTo({ url: '/pages/profile/setting' })
		}
	}
}
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 30rpx; }
.profile-header { background: #fff; padding: 60rpx 40rpx 30rpx; text-align: center; position: relative; }
.header-setting { position: absolute; top: 30rpx; right: 30rpx; font-size: 44rpx; line-height: 1; color: #666; padding: 10rpx; }
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

.content-tabs { display: flex; background: #fff; border-bottom: 1px solid #f5f5f5; margin: 20rpx 0; justify-content: center; }
.content-tab { padding: 20rpx 40rpx; font-size: 28rpx; color: #666; }
.content-tab.active { color: #333; font-weight: 600; border-bottom: 4rpx solid #1677ff; }

/* 瀑布流 */
.waterfall-container { display: flex; padding: 0 15rpx; gap: 15rpx; }
.waterfall-col { flex: 1; min-width: 0; }

/* 专辑列表 */
.album-list-section { background: #fff; }
.album-item { display: flex; align-items: center; padding: 30rpx; border-bottom: 1px solid #f5f5f5; }
.album-icon { font-size: 44rpx; margin-right: 20rpx; }
.album-info { flex: 1; }
.album-title-text { font-size: 30rpx; color: #333; font-weight: 500; }
.album-count-text { display: block; font-size: 24rpx; color: #999; margin-top: 8rpx; }
.album-arrow { font-size: 30rpx; color: #ccc; }

/* 新建专辑 */
.add-album-area { padding: 30rpx; background: #fff; margin-top: 2rpx; }
.btn-add-album { width: 100%; height: 80rpx; line-height: 80rpx; background: #1677ff; color: #fff; border-radius: 8rpx; font-size: 28rpx; border: none; }
.input-album-name { height: 80rpx; line-height: 80rpx; padding: 0 20rpx; border: 1px solid #e5e5e5; border-radius: 8rpx; font-size: 28rpx; margin-bottom: 20rpx; }
.add-album-btns { display: flex; gap: 20rpx; }
.btn-confirm { flex: 1; height: 80rpx; line-height: 80rpx; background: #1677ff; color: #fff; border-radius: 8rpx; font-size: 28rpx; border: none; }
.btn-cancel { flex: 1; height: 80rpx; line-height: 80rpx; background: #f5f5f5; color: #666; border-radius: 8rpx; font-size: 28rpx; border: none; }
</style>
