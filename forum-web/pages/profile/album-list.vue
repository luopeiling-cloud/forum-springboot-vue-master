<template>
	<view class="album-list-page">
		<view class="album-items">
			<view
				class="album-item"
				v-for="album in albumList"
				:key="album.id"
				@click="toAlbumDetail(album)"
			>
				<text class="album-icon">📁</text>
				<view class="album-info">
					<text class="album-title">{{ album.title }}</text>
					<text class="album-count">{{ album.imgCount || 0 }}张笔记</text>
				</view>
				<text class="album-arrow">></text>
			</view>
		</view>
		<empty-state text="还没有专辑，去收藏笔记时创建吧~" v-if="albumList.length === 0" />
	</view>
</template>

<script>
import { getAlbumPageByUserId } from '@/api/album.js'
import EmptyState from '@/components/empty-state.vue'
import userStore from '@/store/userStore.js'

export default {
	components: { EmptyState },
	data() {
		return { albumList: [] }
	},
	onShow() {
		this.loadAlbums()
	},
	methods: {
		async loadAlbums() {
			try {
				const res = await getAlbumPageByUserId(1, 50, userStore.getUserId())
				this.albumList = (res && res.data && res.data.records) ? res.data.records : []
			} catch (e) {
				this.albumList = []
			}
		},
		toAlbumDetail(album) {
			uni.navigateTo({ url: '/pages/profile/album-detail?albumId=' + album.id })
		}
	}
}
</script>

<style scoped>
.album-list-page { min-height: 100vh; background: #f5f5f5; }
.album-item {
	display: flex; align-items: center; padding: 30rpx;
	background: #fff; border-bottom: 1px solid #f5f5f5;
}
.album-icon { font-size: 44rpx; margin-right: 20rpx; }
.album-info { flex: 1; }
.album-title { font-size: 30rpx; color: #333; font-weight: 500; }
.album-count { display: block; font-size: 24rpx; color: #999; margin-top: 8rpx; }
.album-arrow { font-size: 30rpx; color: #ccc; }
</style>
