<template>
	<view class="chat-message" :class="isMe ? 'msg-me' : 'msg-other'">
		<view class="msg-avatar" v-if="!isMe">
			<image :src="displayAvatar" mode="aspectFill" class="avatar-img" />
		</view>
		<view class="msg-bubble" :class="isMe ? 'bubble-me' : 'bubble-other'">
			<text>{{ message.content }}</text>
		</view>
		<view class="msg-avatar" v-if="isMe">
			<image :src="displayAvatar" mode="aspectFill" class="avatar-img" />
		</view>
	</view>
</template>

<script>
	import { getFullUrl } from '@/utils/util.js'

	export default {
		name: 'ChatMessage',
		props: {
			message: {
				type: Object,
				required: true
			},
			isMe: {
				type: Boolean,
				default: false
			},
			avatarUrl: {
				type: String,
				default: ''
			}
		},
		computed: {
			displayAvatar() {
				return getFullUrl(this.avatarUrl) || '/static/default-avatar.png'
			}
		}
	}
</script>

<style scoped>
	.chat-message {
		display: flex;
		align-items: flex-start;
		margin: 20rpx;
	}
	.msg-me {
		justify-content: flex-end;
	}
	.msg-other {
		justify-content: flex-start;
	}
	.avatar-img {
		width: 70rpx;
		height: 70rpx;
		border-radius: 50%;
		flex-shrink: 0;
	}
	.msg-bubble {
		max-width: 60%;
		padding: 15rpx 20rpx;
		border-radius: 12rpx;
		font-size: 28rpx;
		line-height: 1.5;
		word-break: break-all;
		margin: 0 15rpx;
	}
	.bubble-me {
		background: #007aff;
		color: #fff;
	}
	.bubble-other {
		background: #fff;
		color: #333;
		border: 1px solid #f0f0f0;
	}
</style>
