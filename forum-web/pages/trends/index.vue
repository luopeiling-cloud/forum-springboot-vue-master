<template>
	<view class="trends-page">
		<view class="trends-list">
			<trend-item
				v-for="item in trendList"
				:key="item.nid"
				:item="item"
			></trend-item>
		</view>
		<loading-more :loading="loading" :noMore="noMore" />
		<empty-state text="关注你感兴趣的人，查看最新动态~" v-if="!loading && trendList.length === 0" />
		<floating-btn @refresh="onRefresh" />
	</view>
</template>

<script>
	import TrendItem from '@/components/trend-item.vue'
	import FloatingBtn from '@/components/floating-btn.vue'
	import LoadingMore from '@/components/loading-more.vue'
	import EmptyState from '@/components/empty-state.vue'
	import { getFollowTrendPage } from '@/api/follower.js'
	import { formateTime } from '@/utils/util.js'

	export default {
		components: { TrendItem, FloatingBtn, LoadingMore, EmptyState },
		data() {
			return {
				trendList: [],
				currentPage: 1,
				pageSize: 10,
				total: 0,
				loading: false,
				noMore: false
			}
		},
		onLoad() {
			this.getTrends()
		},
		onShow() {
			// 从其他 tab 切回时刷新列表
			if (this._initialized) {
				this.getTrends()
			}
			this._initialized = true
		},
		onPullDownRefresh() {
			this.onRefresh()
		},
		onReachBottom() {
			this.loadMore()
		},
		methods: {
			async getTrends() {
				this.loading = true
				this.currentPage = 1
				this.trendList = []
				this.noMore = false
				try {
					const res = await getFollowTrendPage(this.currentPage, this.pageSize)
					this.setData(res)
				} catch (e) {
					this.loading = false
				}
			},
			setData(res) {
				this.loading = false
				uni.stopPullDownRefresh()
				if (res && res.data) {
					const { records, total } = res.data
					this.total = total || 0
					const items = (records || []).map(item => ({
						...item,
						time: formateTime(item.time)
					}))
					if (this.currentPage === 1) {
						this.trendList = items
					} else {
						this.trendList = [...this.trendList, ...items]
					}
					this.noMore = this.trendList.length >= this.total
				}
			},
			async loadMore() {
				if (this.loading || this.noMore) return
				this.loading = true
				this.currentPage++
				try {
					const res = await getFollowTrendPage(this.currentPage, this.pageSize)
					this.setData(res)
				} catch (e) {
					this.loading = false
					this.currentPage--
				}
			},
			onRefresh() {
				this.getTrends()
			}
		}
	}
</script>

<style scoped>
	.trends-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding-bottom: 20rpx;
	}
</style>
