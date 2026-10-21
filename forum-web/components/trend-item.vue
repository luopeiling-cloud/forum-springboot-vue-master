<template>
	<view class="trend-item">
		<!-- 用户头像 -->
		<view class="trend-avatar" @click="toUser">
			<image :src="avatarUrl" mode="aspectFill" class="avatar-img" />
		</view>
		<!-- 内容区 -->
		<view class="trend-main">
			<view class="trend-header">
				<text class="trend-username">{{ item.username }}</text>
				<text class="trend-time">{{ formateTime(item.time) }}</text>
			</view>
			<view class="trend-content" @click="toDetail">
				<text>{{ item.content }}</text>
			</view>
			<!-- 图片网格 -->
			<view class="trend-imgs" v-if="item.imgUrls && item.imgUrls.length > 0" @click="toDetail">
				<image
					v-for="(url, idx) in item.imgUrls.slice(0, 4)"
					:key="idx"
					:src="getFullUrl(url)"
					mode="aspectFill"
					class="trend-img-item"
					lazy-load
				/>
			</view>
			<!-- 操作栏 -->
			<view class="trend-actions">
				<view class="action-btn" @click="onLike">
					<text :class="item.isLike ? 'icon-active' : ''">{{ item.isLike ? '❤️' : '🤍' }}</text>
					<text class="action-num" v-if="item.likeCount > 0">{{ item.likeCount }}</text>
				</view>
				<view class="action-btn" @click="toDetail">
					<text>💬</text>
					<text class="action-num" v-if="item.commentCount > 0">{{ item.commentCount }}</text>
				</view>
				<view class="action-btn">
					<text>···</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { formateTime, getFullUrl } from '@/utils/util.js'
	import { likeOrCollectionByDTO } from '@/api/likeOrCollection.js'

	export default {
		name: 'TrendItem',
		props: {
			item: {
				type: Object,
				required: true
			}
		},
		computed: {
			avatarUrl() {
				return getFullUrl(this.item.avatar) || '/static/default-avatar.png'
			}
		},
		methods: {
			formateTime,
			getFullUrl,
			toUser() {
				uni.navigateTo({ url: '/pages/profile/user-home?userId=' + this.item.uid })
			},
			toDetail() {
				uni.navigateTo({ url: '/pages/discover/detail?noteId=' + this.item.nid })
			},
			onLike() {
				const data = {
					likeOrCollectionId: this.item.nid,
					publishUid: this.item.uid,
					type: 1
				}
				const val = this.item.isLike ? -1 : 1
				likeOrCollectionByDTO(data).then(() => {
					this.item.isLike = !this.item.isLike
					this.item.likeCount += val
				}).catch(() => {})
			}
		}
	}
</script>

<style scoped>
	.trend-item {
		display: flex;
		padding: 30rpx 20rpx;
		background: #fff;
		border-bottom: 1px solid #f5f5f5;
	}
	.trend-avatar {
		flex-shrink: 0;
		margin-right: 20rpx;
	}
	.avatar-img {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		border: 1px solid rgba(0,0,0,0.08);
	}
	.trend-main {
		flex: 1;
		min-width: 0;
	}
	.trend-header {
		display: flex;
		align-items: center;
		margin-bottom: 8rpx;
	}
	.trend-username {
		font-size: 30rpx;
		font-weight: 600;
		color: #333;
		margin-right: 15rpx;
	}
	.trend-time {
		font-size: 22rpx;
		color: #999;
	}
	.trend-content {
		font-size: 28rpx;
		color: #333;
		line-height: 1.5;
		margin-bottom: 15rpx;
	}
	.trend-imgs {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-bottom: 15rpx;
	}
	.trend-img-item {
		width: calc(25% - 7.5rpx);
		height: 230rpx;
		border-radius: 8rpx;
		background: #f0f0f0;
	}
	.trend-actions {
		display: flex;
		align-items: center;
		gap: 40rpx;
	}
	.action-btn {
		display: flex;
		align-items: center;
		font-size: 28rpx;
	}
	.action-num {
		font-size: 24rpx;
		color: #999;
		margin-left: 6rpx;
	}
	.icon-active {
		/* 点赞高亮 */
	}
</style>
