<template>
	<view class="category-scroll">
		<scroll-view class="scroll-view" scroll-x enable-flex :show-scrollbar="false">
			<view class="scroll-content">
				<view
					class="category-item"
					:class="{ active: activeId === '0' }"
					@click="onSelect('0', '推荐')"
				>
					<text>推荐</text>
				</view>
				<view
					v-for="item in list"
					:key="item.id"
					class="category-item"
					:class="{ active: activeId === item.id }"
					@click="onSelect(item.id, item.title)"
				>
					<text>{{ item.title }}</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	export default {
		name: 'CategoryScroll',
		props: {
			list: {
				type: Array,
				default: () => []
			},
			activeId: {
				type: String,
				default: '0'
			}
		},
		methods: {
			onSelect(id, title) {
				this.$emit('select', { id, title })
			}
		}
	}
</script>

<style scoped>
	.category-scroll {
		width: 100%;
		height: 80rpx;
		background: #fff;
		border-bottom: 1px solid #f5f5f5;
	}
	.scroll-view {
		width: 100%;
		height: 100%;
		white-space: nowrap;
	}
	.scroll-content {
		display: flex;
		align-items: center;
		height: 100%;
		padding: 0 20rpx;
		width: max-content;
	}
	.category-item {
		height: 60rpx;
		line-height: 60rpx;
		padding: 0 32rpx;
		font-size: 28rpx;
		color: #666;
		border-radius: 999rpx;
		flex-shrink: 0;
	}
	.category-item.active {
		font-weight: 600;
		color: #333;
		background: rgba(0,0,0,0.03);
	}
</style>
