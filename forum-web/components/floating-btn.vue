<template>
	<view class="floating-btns">
		<view class="float-btn" @click="onRefresh" v-if="showRefresh">
			<text class="btn-icon">⟳</text>
		</view>
		<view class="float-btn" @click="onBackTop" v-if="showBackTop">
			<text class="btn-icon">↑</text>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'FloatingBtn',
		props: {
			showRefresh: {
				type: Boolean,
				default: true
			},
			showBackTop: {
				type: Boolean,
				default: true
			}
		},
		methods: {
			onRefresh() {
				this.$emit('refresh')
			},
			onBackTop() {
				uni.pageScrollTo({
					scrollTop: 0,
					duration: 300
				})
			}
		}
	}
</script>

<style scoped>
	.floating-btns {
		position: fixed;
		right: 24rpx;
		bottom: 160rpx;
		display: flex;
		flex-direction: column;
		gap: 15rpx;
		z-index: 50;
	}
	.float-btn {
		width: 80rpx;
		height: 80rpx;
		background: #fff;
		border: 1px solid rgba(0,0,0,0.08);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.08);
	}
	.btn-icon {
		font-size: 36rpx;
		color: #666;
	}
	.float-btn:active {
		background: #f5f5f5;
	}
</style>
