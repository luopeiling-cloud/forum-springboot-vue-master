<template>
	<view class="follow-page">
		<!-- Tab 切换 -->
		<view class="follow-tabs">
			<view
				class="follow-tab"
				:class="{ active: activeTab === 1 }"
				@click="switchTab(1)"
			>
				我关注的
			</view>
			<view
				class="follow-tab"
				:class="{ active: activeTab === 2 }"
				@click="switchTab(2)"
			>
				关注我的
			</view>
		</view>

		<!-- 用户列表 -->
		<view class="user-list">
			<view
				class="user-item"
				v-for="item in userList"
				:key="item.uid"
				@click="toProfile(item.uid)"
			>
				<image :src="getFullUrl(item.avatar)" mode="aspectFill" class="user-avatar" />
				<view class="user-info">
					<text class="user-name">{{ item.username }}</text>
				</view>
				<button
					class="btn-follow"
					:class="{ 'btn-followed': item.isFollow }"
					@click.stop="toggleFollow(item)"
				>
					{{ item.isFollow ? '已关注' : '+ 关注' }}
				</button>
			</view>
		</view>

		<loading-more :loading="loading" :noMore="noMore" />
		<empty-state
			:text="activeTab === 1 ? '还没有关注任何人~' : '还没有粉丝~'"
			v-if="!loading && userList.length === 0"
		/>
	</view>
</template>

<script>
import { getFriendPage, followById } from '@/api/follower.js'
import { getFullUrl } from '@/utils/util.js'
import LoadingMore from '@/components/loading-more.vue'
import EmptyState from '@/components/empty-state.vue'

export default {
	components: { LoadingMore, EmptyState },
	data() {
		return {
			activeTab: 1,
			userList: [],
			currentPage: 1,
			pageSize: 20,
			total: 0,
			loading: false,
			noMore: false
		}
	},
	onLoad(options) {
		if (options.type) {
			this.activeTab = parseInt(options.type) || 1
		}
		this.loadList()
	},
		onReachBottom() {
			this.loadMore()
		},
	methods: {
		getFullUrl,
		switchTab(type) {
			this.activeTab = type
			this.currentPage = 1
			this.userList = []
			this.loadList()
		},
		onReachBottom() {
			this.loadMore()
		},
		async loadList() {
			this.loading = true
			this.noMore = false
			try {
				const res = await getFriendPage(this.currentPage, this.pageSize, this.activeTab)
				if (res && res.data) {
					const { records, total } = res.data
					this.total = total || 0
					this.userList = records || []
					this.noMore = this.userList.length >= this.total
				}
			} catch (e) {
				// ignore
			}
			this.loading = false
		},
		async loadMore() {
			if (this.loading || this.noMore) return
			this.loading = true
			this.currentPage++
			try {
				const res = await getFriendPage(this.currentPage, this.pageSize, this.activeTab)
				if (res && res.data) {
					const { records, total } = res.data
					this.total = total || 0
					this.userList = [...this.userList, ...(records || [])]
					this.noMore = this.userList.length >= this.total
				}
			} catch (e) {
				this.currentPage--
			}
			this.loading = false
		},
		toProfile(uid) {
			uni.navigateTo({ url: '/pages/profile/user-home?userId=' + uid })
		},
		async toggleFollow(item) {
			try {
				await followById(item.uid)
				item.isFollow = !item.isFollow
			} catch (e) {
				// ignore
			}
		}
	}
}
</script>

<style scoped>
.follow-page {
	min-height: 100vh;
	background: #f5f5f5;
}
.follow-tabs {
	display: flex;
	background: #fff;
	border-bottom: 1px solid #f5f5f5;
	position: sticky;
	top: 0;
	z-index: 10;
}
.follow-tab {
	flex: 1;
	text-align: center;
	padding: 25rpx 0;
	font-size: 28rpx;
	color: #666;
}
.follow-tab.active {
	color: #333;
	font-weight: 600;
	border-bottom: 4rpx solid #1677ff;
}

.user-list {
	background: #fff;
}
.user-item {
	display: flex;
	align-items: center;
	padding: 25rpx 30rpx;
	border-bottom: 1px solid #f5f5f5;
}
.user-avatar {
	width: 90rpx;
	height: 90rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}
.user-info {
	flex: 1;
}
.user-name {
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
}
.btn-follow {
	width: 140rpx;
	height: 56rpx;
	line-height: 56rpx;
	font-size: 24rpx;
	background: #1677ff;
	color: #fff;
	border-radius: 999rpx;
	border: none;
	padding: 0;
	text-align: center;
}
.btn-followed {
	background: #999;
}
</style>
