<template>
	<view class="comment-item" :class="{ 'comment-highlight': highlight }" :id="id">
		<!-- 一级评论 -->
		<view class="comment-main">
			<image class="comment-avatar" :src="avatarUrl" mode="aspectFill" @click="toUser" />
			<view class="comment-body">
				<view class="comment-header">
					<text class="comment-name">{{ comment.username }}</text>
					<text class="comment-time">{{ formatTime(comment.time) }}</text>
				</view>
				<view class="comment-content">
					<text v-if="comment.replyUsername" class="reply-tag">回复 @{{ comment.replyUsername }}：</text>
					<text>{{ comment.content }}</text>
				</view>
				<view class="comment-actions">
					<view class="action-item" @click="onLike">
						<text :class="comment.isLike ? 'icon-liked' : ''">❤</text>
						<text class="action-count" v-if="comment.likeCount > 0">{{ comment.likeCount }}</text>
					</view>
					<view class="action-item" @click="onReply">
						<text>💬</text>
						<text class="action-count" v-if="comment.twoCommentCount > 0">{{ comment.twoCommentCount }}</text>
					</view>
				</view>

				<!-- 二级评论（children） -->
				<view v-if="comment.children && comment.children.length > 0" class="sub-comments">
					<view
						v-for="child in visibleChildren"
						:key="child.id"
						class="sub-comment"
					>
						<text class="sub-name">{{ child.username }}</text>
						<text v-if="child.replyUsername" class="reply-tag"> 回复 @{{ child.replyUsername }}：</text>
						<text class="sub-content">{{ child.content }}</text>
					</view>

					<!-- 展开/收起 -->
					<view
						v-if="comment.children.length > previewCount"
						class="expand-btn"
						@click="toggleExpand"
					>
						<text>{{ expanded ? '收起所有回复' : '展开更多回复 (' + (comment.children.length - previewCount) + '条)' }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { formateTime, getFullUrl } from '@/utils/util.js'
	import { likeOrCollectionByDTO } from '@/api/likeOrCollection.js'

	export default {
		name: 'CommentItem',
		props: {
				comment: { type: Object, required: true },
				highlight: { type: Boolean, default: false },
				expandAll: { type: Boolean, default: false },
		},
		data() {
			return {
				previewCount: 2,
				expanded: false
			}
		},
		computed: {
			avatarUrl() {
				return getFullUrl(this.comment.avatar) || '/static/default-avatar.png'
			},
			visibleChildren() {
				if (this.expanded || this.expandAll || !this.comment.children) {
					return this.comment.children || []
				}
				return (this.comment.children || []).slice(0, this.previewCount)
			}
		},
		methods: {
			formatTime(t) {
				return formateTime(t)
			},
			toggleExpand() {
				this.expanded = !this.expanded
			},
			onLike() {
				const data = {
					likeOrCollectionId: this.comment.id,
					publishUid: this.comment.uid,
					type: 2 // 点赞评论
				}
				likeOrCollectionByDTO(data).then(() => {
					this.comment.isLike = !this.comment.isLike
					this.comment.likeCount += this.comment.isLike ? 1 : -1
				}).catch(() => {})
			},
			onReply() {
				this.$emit('reply', this.comment)
			}
		}
	}
</script>

<style scoped>
	.comment-item {
		padding: 20rpx 0;
	}
	.comment-main {
		display: flex;
	}
	.comment-avatar {
		width: 60rpx;
		height: 60rpx;
		border-radius: 50%;
		flex-shrink: 0;
		margin-right: 15rpx;
	}
	.comment-body {
		flex: 1;
		min-width: 0;
	}
	.comment-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 6rpx;
	}
	.comment-name {
		font-size: 26rpx;
		color: #333;
		font-weight: 500;
	}
	.comment-time {
		font-size: 20rpx;
		color: #999;
	}
	.comment-content {
		font-size: 26rpx;
		color: #333;
		line-height: 1.5;
		word-break: break-all;
	}
	.reply-tag {
		color: #1677ff;
	}
	.comment-actions {
		display: flex;
		align-items: center;
		margin-top: 10rpx;
		gap: 30rpx;
	}
	.action-item {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		color: #999;
	}
	.action-count {
		margin-left: 4rpx;
	}
	.icon-liked {
		color: #f53f3f;
	}

	/* 二级评论 */
	.sub-comments {
		margin-top: 15rpx;
		padding: 15rpx;
		background: #f8f8f8;
		border-radius: 8rpx;
	}
	.sub-comment {
		font-size: 24rpx;
		color: #666;
		line-height: 1.6;
		margin-bottom: 8rpx;
	}
	.sub-name {
		color: #1677ff;
		font-weight: 500;
	}
	.sub-content {
		color: #333;
	}
	.expand-btn { font-size: 24rpx; color: #1677ff; margin-top: 10rpx; }
.comment-highlight { background: #fffbe6; border-radius: 8rpx; padding: 20rpx 15rpx; animation: highlightFade 2s ease-out; }
@keyframes highlightFade { from { background: #fff3b0; } to { background: #fffbe6; } }
</style>
