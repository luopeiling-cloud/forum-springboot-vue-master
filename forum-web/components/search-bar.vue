<template>
	<view class="search-bar-container">
		<!-- 搜索栏 -->
		<view class="search-bar">
			<view class="search-box">
				<text class="search-icon">🔍</text>
				<input
					class="search-input"
					v-model="keyword"
					:placeholder="placeholder"
					@input="onInput"
					@focus="onFocus"
					@confirm="onSearch"
					confirm-type="search"
				/>
				<view class="search-clear" v-if="keyword" @click="onClear">
					<text>✕</text>
				</view>
			</view>
			<button class="search-btn" @click="onSearch">搜索</button>
		</view>

		<!-- 搜索面板（输入时显示） -->
		<view class="search-panel" v-if="showPanel && keyword">
			<view
				class="panel-item"
				v-for="(item, index) in suggestions"
				:key="index"
				@click="onSelectSuggestion(item)"
			>
				<text class="panel-text" v-html="item.highlightContent || item.content || item"></text>
			</view>
			<view class="panel-empty" v-if="suggestions.length === 0">
				<text>暂无搜索建议</text>
			</view>
		</view>

		<!-- 历史/热搜面板（搜索框为空时） -->
		<view class="search-panel" v-if="showPanel && !keyword && showDefault">
			<!-- 搜索历史 -->
			<view class="panel-section" v-if="historyList.length > 0">
				<view class="section-header">
					<text class="section-title">历史记录</text>
					<text class="section-action" @click="onClearHistory">🗑</text>
				</view>
				<view class="tag-list">
					<view
						class="tag-item"
						v-for="(item, index) in historyList"
						:key="index"
						@click="onSearchKeyword(item)"
					>
						<text>{{ item }}</text>
					</view>
				</view>
			</view>

			<!-- 猜你想搜 -->
			<view class="panel-section">
				<view class="section-header">
					<text class="section-title">猜你想搜</text>
				</view>
				<view class="suggest-grid">
					<view
						class="suggest-item"
						v-for="(item, index) in recommendList"
						:key="index"
						@click="onSearchKeyword(item)"
					>
						<text>{{ item }}</text>
					</view>
				</view>
			</view>

			<!-- 热搜榜 -->
			<view class="panel-section" v-if="hotList.length > 0">
				<view class="section-header">
					<text class="section-title">热点</text>
				</view>
				<view
					class="hot-item"
					v-for="(item, index) in hotList"
					:key="index"
					@click="onSearchKeyword(item.content)"
				>
					<text class="hot-index" :class="'hot-' + (index + 1)">{{ index + 1 }}</text>
					<text class="hot-title">{{ item.content }}</text>
					<text class="hot-score">{{ item.searchCount }}</text>
				</view>
			</view>
		</view>

		<!-- 遮罩 -->
		<view class="search-mask" v-if="showPanel" @click="onClose"></view>
	</view>
</template>

<script>
	import { getRecordByKeyWord, getHotRecord } from '@/api/search.js'
	import searchStore from '@/store/searchStore.js'
	import { getRandomString } from '@/utils/util.js'

	export default {
		name: 'SearchBar',
		props: {
			placeholder: {
				type: String,
				default: '搜索校园内容...'
			}
		},
		data() {
			return {
				keyword: '',
				showPanel: false,
				showDefault: true,
				suggestions: [],
				historyList: [],
				hotList: [],
				recommendList: ['壁纸', '风景', '情侣', '头像', '动漫', '动物', '学习', '活动']
			}
		},
		mounted() {
			this.loadDefaultData()
		},
		methods: {
			loadDefaultData() {
				this.historyList = searchStore.getRecords() || []
				getHotRecord().then(res => {
					if (res && res.data) {
						this.hotList = (res.data || []).slice(0, 10)
					}
				}).catch(() => {})
			},
			onInput(e) {
				this.keyword = e.detail.value
				if (this.keyword) {
					getRecordByKeyWord(this.keyword).then(res => {
						if (res && res.data) {
							this.suggestions = res.data || []
						}
					}).catch(() => {
						this.suggestions = []
					})
				} else {
					this.suggestions = []
				}
			},
			onFocus() {
				this.showPanel = true
				this.showDefault = true
				this.loadDefaultData()
			},
			onClear() {
				this.keyword = ''
				this.suggestions = []
				this.showDefault = true
			},
			onClose() {
				this.showPanel = false
			},
			onSearch() {
				if (!this.keyword.trim()) return
				this.doSearch(this.keyword.trim())
			},
			onSearchKeyword(keyword) {
				this.keyword = keyword
				this.doSearch(keyword)
			},
			onSelectSuggestion(item) {
				const keyword = item.content || item
				this.keyword = keyword
				this.doSearch(keyword)
			},
			doSearch(keyword) {
				searchStore.setKeyword(keyword)
				searchStore.pushRecord(keyword)
				searchStore.setSeed(getRandomString(12))
				this.showPanel = false
				this.$emit('search', keyword)
			},
			onClearHistory() {
				searchStore.clearAllRecord()
				this.historyList = []
			}
		}
	}
</script>

<style scoped>
	.search-bar-container {
		position: relative;
		z-index: 100;
	}
	.search-bar {
		display: flex;
		align-items: center;
		padding: 15rpx 20rpx;
		background: #fff;
	}
	.search-box {
		flex: 1;
		position: relative;
		display: flex;
		align-items: center;
		background: #f0f0f0;
		border-radius: 35rpx;
		padding: 0 10rpx;
	}
	.search-icon {
		font-size: 30rpx;
		margin-left: 15rpx;
		margin-right: 10rpx;
	}
	.search-input {
		flex: 1;
		height: 70rpx;
		line-height: 70rpx;
		font-size: 28rpx;
	}
	.search-clear {
		padding: 0 15rpx;
		font-size: 24rpx;
		color: #999;
	}
	.search-btn {
		width: 120rpx;
		height: 70rpx;
		line-height: 70rpx;
		background: #1677ff;
		color: #fff;
		border-radius: 35rpx;
		font-size: 26rpx;
		margin-left: 15rpx;
		border: none;
		padding: 0;
		text-align: center;
	}

	/* 搜索面板 */
	.search-panel {
		position: absolute;
		top: 100%;
		left: 0;
		right: 0;
		background: #fff;
		max-height: 600rpx;
		overflow-y: auto;
		border-radius: 0 0 12rpx 12rpx;
		box-shadow: 0 4rpx 32rpx rgba(0,0,0,0.08);
		padding: 10rpx;
		z-index: 99;
	}
	.panel-item {
		padding: 20rpx 25rpx;
		font-size: 28rpx;
		color: #333;
		border-radius: 8rpx;
	}
	.panel-item:active {
		background: #f8f8f8;
	}
	.panel-empty {
		padding: 60rpx;
		text-align: center;
		color: #999;
		font-size: 26rpx;
	}

	/* 面板子区域 */
	.panel-section {
		padding: 10rpx;
	}
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 10rpx 15rpx;
	}
	.section-title {
		font-size: 24rpx;
		color: #999;
	}
	.section-action {
		font-size: 28rpx;
	}

	/* 标签列表 */
	.tag-list {
		display: flex;
		flex-wrap: wrap;
		padding: 0 10rpx;
		gap: 15rpx;
	}
	.tag-item {
		height: 60rpx;
		line-height: 60rpx;
		padding: 0 24rpx;
		background: rgba(0,0,0,0.03);
		border-radius: 999rpx;
		font-size: 26rpx;
		color: #333;
	}

	/* 推荐搜索 */
	.suggest-grid {
		display: flex;
		flex-wrap: wrap;
		padding: 0 10rpx;
	}
	.suggest-item {
		width: 50%;
		padding: 15rpx 15rpx;
		font-size: 28rpx;
		color: #333;
		border-radius: 8rpx;
	}

	/* 热搜 */
	.hot-item {
		display: flex;
		align-items: center;
		padding: 15rpx 25rpx;
	}
	.hot-index {
		width: 40rpx;
		font-weight: 600;
		font-size: 28rpx;
		color: #999;
	}
	.hot-1 { color: #f53f3f; }
	.hot-2 { color: #ff9900; }
	.hot-3 { color: #ff24a4; }
	.hot-title {
		flex: 1;
		font-size: 28rpx;
		color: #333;
		margin: 0 15rpx;
	}
	.hot-score {
		font-size: 22rpx;
		color: #ccc;
	}

	/* 遮罩 */
	.search-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0,0,0,0.3);
		z-index: 98;
	}
</style>
