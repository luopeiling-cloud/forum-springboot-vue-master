<template>
	<view class="loading-more">
		<text v-if="loading" class="loading-text">加载中...</text>
		<text v-else-if="noMore" class="no-more-text">— 没有更多了 —</text>
	</view>
</template>

<script>
	export default {
		name: 'LoadingMore',
		props: {
			loading: {
				type: Boolean,
				default: false
			},
			noMore: {
				type: Boolean,
				default: false
			}
		}
	}
</script>

<style scoped>
	.loading-more {
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 30rpx 0;
	}
	.loading-text, .no-more-text {
		font-size: 24rpx;
		color: #999;
	}
</style>
