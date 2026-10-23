<template>
	<view class="user-avatar" :class="sizeClass" @click="onClick">
		<image
			:src="avatarUrl"
			mode="aspectFill"
			class="avatar-img"
			:class="sizeClass"
		/>
	</view>
</template>

<script>
	import { getFullUrl } from '@/utils/util.js'

	export default {
		name: 'UserAvatar',
		props: {
			src: {
				type: String,
				default: ''
			},
			size: {
				type: String,
				default: 'md' // sm | md | lg
			},
			userId: {
				type: String,
				default: ''
			}
		},
		computed: {
			avatarUrl() {
				return getFullUrl(this.src) || '/static/default-avatar.png'
			},
			sizeClass() {
				return 'avatar-' + this.size
			}
		},
		methods: {
			onClick() {
				if (this.userId) {
					uni.navigateTo({
						url: '/pages/profile/index?userId=' + this.userId
					})
				}
			}
		}
	}
</script>

<style scoped>
	.user-avatar {
		flex-shrink: 0;
		overflow: hidden;
	}
	.avatar-img {
		border-radius: 50%;
		border: 1px solid rgba(0,0,0,0.08);
		object-fit: cover;
	}
	.avatar-sm {
		width: 60rpx;
		height: 60rpx;
	}
	.avatar-md {
		width: 80rpx;
		height: 80rpx;
	}
	.avatar-lg {
		width: 120rpx;
		height: 120rpx;
	}
</style>
