<template>
	<view class="note-card" @click="toDetail">
		<!-- 封面图 -->
		<image
			class="note-cover"
			:src="coverUrl"
			mode="widthFix"
			lazy-load
			@load="onImgLoad"
			@error="onImgError"
		/>
		<!-- 信息区 -->
		<view class="note-info">
			<view class="note-title ellipsis-2">{{ item.title }}</view>
			<view class="note-footer">
				<view class="author-info" @click.stop="toUser">
					<image class="author-avatar" :src="avatarUrl" mode="aspectFill" />
					<text class="author-name ellipsis">{{ item.username }}</text>
				</view>
				<view class="like-info">
					<text class="like-icon">❤</text>
					<text class="like-count">{{ formatCount(item.likeCount) }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { getFullUrl, formatCount } from '@/utils/util.js'

	export default {
		name: 'NoteCard',
		props: {
			item: {
				type: Object,
				required: true
			}
		},
		computed: {
			coverUrl() {
				return getFullUrl(this.item.noteCover) || '/static/default-cover.png'
			},
			avatarUrl() {
				return getFullUrl(this.item.avatar) || '/static/default-avatar.png'
			}
		},
		methods: {
			formatCount,
			toDetail() {
				uni.navigateTo({
					url: '/pages/discover/detail?noteId=' + this.item.id
				})
			},
			toUser() {
				const uid = this.item.uid || this.item.userId
				if (uid) {
					uni.navigateTo({ url: '/pages/profile/user-home?userId=' + uid })
				}
			},
			onImgLoad() {
				this.item.isLoading = true
			},
			onImgError() {
				this.item.noteCover = ''
			}
		}
	}
</script>

<style scoped>
	.note-card {
		background: #fff;
		border-radius: 15rpx;
		overflow: hidden;
		margin-bottom: 15rpx;
		box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
		break-inside: avoid;
	}
	.note-cover {
		width: 100%;
		display: block;
		border-radius: 15rpx 15rpx 0 0;
	}
	.note-info {
		padding: 15rpx 20rpx;
	}
	.note-title {
		font-size: 28rpx;
		font-weight: 500;
		color: #333;
		line-height: 1.4;
		margin-bottom: 8rpx;
	}
	.note-footer {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 40rpx;
	}
	.author-info {
		display: flex;
		align-items: center;
		flex: 1;
		min-width: 0;
	}
	.author-avatar {
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		border: 1px solid rgba(0,0,0,0.08);
		margin-right: 6rpx;
		flex-shrink: 0;
	}
	.author-name {
		font-size: 24rpx;
		color: #999;
	}
	.like-info {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		color: #999;
		flex-shrink: 0;
	}
	.like-icon {
		margin-right: 4rpx;
	}
	.like-count {
		color: #999;
	}
</style>
