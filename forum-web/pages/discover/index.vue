<template>
	<view class="discover-page">
		<!-- 搜索栏 -->
		<search-bar placeholder="搜索校园内容..." @search="onSearch" ref="searchBar"></search-bar>

		<!-- 分类滚动 -->
		<category-scroll
			:list="categoryList"
			:activeId="activeCategoryId"
			@select="onCategorySelect"
		></category-scroll>

		<!-- 瀑布流笔记列表 -->
		<view class="waterfall-container">
			<view class="waterfall-col" v-for="(col, colIdx) in columns" :key="colIdx">
				<note-card
					v-for="item in col"
					:key="item.id"
					:item="item"
				></note-card>
			</view>
		</view>

		<!-- 加载状态 -->
		<loading-more :loading="loading" :noMore="noMore"></loading-more>
		<empty-state text="暂无内容，去看看别的吧~" v-if="!loading && noteList.length === 0"></empty-state>

		<!-- 悬浮按钮 -->
		<floating-btn @refresh="onRefresh"></floating-btn>
	</view>
</template>

<script>
	import SearchBar from '@/components/search-bar.vue'
	import CategoryScroll from '@/components/category-scroll.vue'
	import NoteCard from '@/components/note-card.vue'
	import FloatingBtn from '@/components/floating-btn.vue'
	import LoadingMore from '@/components/loading-more.vue'
	import EmptyState from '@/components/empty-state.vue'
	import { getRecommendNotePage, getNotePageByDTO, addRecord } from '@/api/search.js'
	import { getCategoryTreeData } from '@/api/category.js'
	import searchStore from '@/store/searchStore.js'

	export default {
		components: { SearchBar, CategoryScroll, NoteCard, FloatingBtn, LoadingMore, EmptyState },
		data() {
			return {
				categoryList: [],
				activeCategoryId: '0',
				noteList: [],
				currentPage: 1,
				pageSize: 10,
				noteTotal: 0,
				loading: false,
				noMore: false,
				columnCount: 2,
				noteDTO: {
					keyword: '',
					type: 0,
					cid: '',
					cpid: ''
				}
			}
		},
		computed: {
			columns() {
				// 将 noteList 分配到 N 列
				const cols = []
				for (let i = 0; i < this.columnCount; i++) {
					cols.push([])
				}
				this.noteList.forEach((item, index) => {
					cols[index % this.columnCount].push(item)
				})
				return cols
			}
		},
		onLoad() {
			this.initData()
		},
		onShow() {
			// 从其他 tab 切回时刷新列表
			if (this._initialized) {
				this.getNoteList()
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
			initData() {
				this.getCategoryData()
				this.getNoteList()
			},
			/** 获取分类 */
			async getCategoryData() {
				try {
					const res = await getCategoryTreeData()
					if (res && res.data) {
						this.categoryList = res.data || []
					}
				} catch (e) {}
			},
			/** 获取推荐笔记列表 */
			async getNoteList() {
				this.loading = true
				this.noMore = false
				this.currentPage = 1
				this.noteList = []
				try {
					const res = await getRecommendNotePage(this.currentPage, this.pageSize)
					this.setData(res)
				} catch (e) {
					this.loading = false
				}
			},
			/** 设置数据 */
			setData(res) {
				this.loading = false
				uni.stopPullDownRefresh()
				if (res && res.data) {
					const { records, total } = res.data
					this.noteTotal = total || 0
					if (this.currentPage === 1) {
						this.noteList = records || []
					} else {
						this.noteList = [...this.noteList, ...(records || [])]
					}
					this.noMore = this.noteList.length >= this.noteTotal
				}
			},
			/** 加载更多 */
			async loadMore() {
				if (this.loading || this.noMore) return
				this.loading = true
				this.currentPage++
				try {
					let res
					if (this.noteDTO.cpid === '' && this.noteDTO.keyword === '') {
						res = await getRecommendNotePage(this.currentPage, this.pageSize)
					} else {
						res = await getNotePageByDTO(this.currentPage, this.pageSize, this.noteDTO)
					}
					this.setData(res)
				} catch (e) {
					this.loading = false
					this.currentPage--
				}
			},
			/** 搜索 */
			onSearch(keyword) {
				this.activeCategoryId = '0'
				this.noteDTO.keyword = keyword
				this.noteDTO.cpid = ''
				this.noteList = []
				this.currentPage = 1
				this.loading = true
				this.noMore = false
				getNotePageByDTO(this.currentPage, this.pageSize, this.noteDTO).then(res => {
					this.setData(res)
				}).catch(() => {
					this.loading = false
				})
				addRecord(keyword).catch(() => {})
			},
			/** 分类切换 */
			onCategorySelect({ id, title }) {
				this.activeCategoryId = id
				this.noteList = []
				this.currentPage = 1
				this.loading = true
				this.noMore = false
				if (id === '0') {
					this.noteDTO.cpid = ''
					getRecommendNotePage(this.currentPage, this.pageSize).then(res => {
						this.setData(res)
					}).catch(() => { this.loading = false })
				} else {
					this.noteDTO.cpid = id
					getNotePageByDTO(this.currentPage, this.pageSize, this.noteDTO).then(res => {
						this.setData(res)
					}).catch(() => { this.loading = false })
				}
			},
			/** 刷新 */
			onRefresh() {
				this.noteDTO.keyword = searchStore.state.keyWord || ''
				if (this.activeCategoryId === '0') {
					this.noteDTO.cpid = ''
				}
				this.getNoteList()
			}
		}
	}
</script>

<style scoped>
	.discover-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding-bottom: 20rpx;
	}
	.waterfall-container {
		display: flex;
		padding: 15rpx;
		gap: 15rpx;
	}
	.waterfall-col {
		flex: 1;
		min-width: 0;
	}
</style>
