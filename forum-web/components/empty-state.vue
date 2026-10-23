<template>
	<view class="empty-state">
		<image class="empty-img" src="/static/empty.png" mode="aspectFit" v-if="showImage" />
		<text class="empty-text">{{ text }}</text>
		<slot></slot>
	</view>
</template>

<script>
	export default {
		name: 'EmptyState',
		props: {
			text: {
				type: String,
				default: '暂无数据'
			},
			showImage: {
				type: Boolean,
				default: false
			}
		}
	}
</script>

<style scoped>
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 200rpx 0;
	}
	.empty-img {
		width: 200rpx;
		height: 200rpx;
		margin-bottom: 30rpx;
	}
	.empty-text {
		font-size: 28rpx;
		color: #999;
	}
</style>
