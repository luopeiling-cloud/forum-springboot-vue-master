<template>
	<view class="album-detail-page">
		<!-- 专辑信息头 -->
		<view class="album-header">
			<text class="album-title">📁 {{ albumInfo.title || '专辑' }}</text>
			<text class="album-meta">{{ albumInfo.imgCount || 0 }}张 · {{ albumInfo.username || '' }}</text>
		</view>

		<!-- 删除专辑 -->
		<view class="delete-bar">
			<button class="btn-delete-album" @click="onDeleteAlbum">删除专辑</button>
		</view>

		<!-- 笔记列表 -->
		<view class="note-list">
			<view class="note-item" v-for="item in noteList" :key="item.id">
				<image
					v-if="item.noteCover"
					:src="getFullUrl(item.noteCover)"
					mode="aspectFill"
					class="note-cover"
					@click="toDetail(item.id)"
				/>
				<view v-else class="note-cover-empty" @click="toDetail(item.id)">🖼</view>
				<view class="note-info" @click="toDetail(item.id)">
					<text class="note-title-text ellipsis">{{ item.title || '无标题' }}</text>
					<text class="note-author">{{ item.username }}</text>
				</view>
				<view class="note-action" @click="showNoteMenu(item)">
					<text class="note-action-icon">···</text>
				</view>
			</view>
		</view>

		<loading-more :loading="loading" :noMore="noMore" />
		<empty-state text="专辑里还没有笔记" v-if="!loading && noteList.length === 0" />

		<!-- 笔记操作弹窗（取消收藏 / 移动） -->
		<view class="action-overlay" v-if="actionVisible" @click="actionVisible = false">
			<view class="action-sheet" @click.stop>
				<view class="action-title">操作笔记</view>
				<view class="action-item warn" @click="onRemoveNote">取消收藏</view>
				<view class="action-item" @click="showMovePicker">移动到其他专辑</view>
				<view class="action-item cancel" @click="actionVisible = false">取消</view>
			</view>
		</view>

		<!-- 移动专辑选择 -->
		<view class="action-overlay" v-if="moveVisible" @click="moveVisible = false">
			<view class="action-sheet" @click.stop>
				<view class="action-title">移动到</view>
				<scroll-view class="move-list" scroll-y>
					<view
						class="action-item"
						v-for="a in moveAlbumList"
						:key="a.id"
						@click="onMoveNote(a)"
					>
						{{ a.title }}
					</view>
				</scroll-view>
				<view class="action-item cancel" @click="moveVisible = false">取消</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getAlbumById, deleteAlbumById, getNotePageByAlbumId, removeNoteFromAlbum, moveNoteToAlbum } from '@/api/album.js'
import { getAlbumPageByUserId } from '@/api/album.js'
import { getFullUrl } from '@/utils/util.js'
import userStore from '@/store/userStore.js'
import LoadingMore from '@/components/loading-more.vue'
import EmptyState from '@/components/empty-state.vue'

export default {
	components: { LoadingMore, EmptyState },
	data() {
		return {
			albumId: '',
			albumInfo: {},
			noteList: [],
			currentPage: 1,
			pageSize: 20,
			total: 0,
			loading: false,
			noMore: false,

			actionVisible: false,
			currentNote: null,

			moveVisible: false,
			moveAlbumList: []
		}
	},
	onLoad(options) {
		this.albumId = options.albumId || ''
		if (this.albumId) {
			this.loadAlbumInfo()
			this.loadNoteList()
		}
	},
	onReachBottom() {
		this.loadMore()
	},
	methods: {
		getFullUrl,

		async loadAlbumInfo() {
			try {
				const res = await getAlbumById(this.albumId)
				if (res && res.data) this.albumInfo = res.data
			} catch (e) {}
		},

		async loadNoteList() {
			this.loading = true
			this.currentPage = 1
			this.noMore = false
			try {
				const res = await getNotePageByAlbumId(this.currentPage, this.pageSize, this.albumId, userStore.getUserId())
				if (res && res.data) {
					const { records, total } = res.data
					this.total = total || 0
					this.noteList = records || []
					this.noMore = this.noteList.length >= this.total
				}
			} catch (e) {}
			this.loading = false
		},

		async loadMore() {
			if (this.loading || this.noMore) return
			this.loading = true
			this.currentPage++
			try {
				const res = await getNotePageByAlbumId(this.currentPage, this.pageSize, this.albumId, userStore.getUserId())
				if (res && res.data) {
					const { records, total } = res.data
					this.total = total || 0
					this.noteList = [...this.noteList, ...(records || [])]
					this.noMore = this.noteList.length >= this.total
				}
			} catch (e) { this.currentPage-- }
			this.loading = false
		},

		/** 删除专辑 */
		onDeleteAlbum() {
			uni.showModal({
				title: '确认删除',
				content: '删除专辑会同时取消收藏其中所有笔记，确定继续？',
				success: async (res) => {
					if (res.confirm) {
						try {
							await deleteAlbumById(this.albumId)
							uni.showToast({ title: '已删除', icon: 'success' })
							setTimeout(() => uni.navigateBack(), 1000)
						} catch (e) {
							uni.showToast({ title: '删除失败', icon: 'none' })
						}
					}
				}
			})
		},

		/** 显示笔记操作菜单 */
		showNoteMenu(item) {
			this.currentNote = item
			this.actionVisible = true
		},

		/** 取消收藏 */
		async onRemoveNote() {
			this.actionVisible = false
			if (!this.currentNote) return
			try {
				await removeNoteFromAlbum(this.currentNote.id, this.albumId)
				this.noteList = this.noteList.filter(n => n.id !== this.currentNote.id)
				uni.showToast({ title: '已取消收藏', icon: 'success' })
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		},

		/** 显示移动专辑选择 */
		async showMovePicker() {
			this.actionVisible = false
			try {
				const res = await getAlbumPageByUserId(1, 50, userStore.getUserId())
				const all = (res && res.data && res.data.records) ? res.data.records : []
				this.moveAlbumList = all.filter(a => a.id !== this.albumId)
				this.moveVisible = true
			} catch (e) {
				this.moveAlbumList = []
				this.moveVisible = true
			}
		},

		/** 移动笔记 */
		async onMoveNote(toAlbum) {
			this.moveVisible = false
			if (!this.currentNote) return
			try {
				await moveNoteToAlbum(this.currentNote.id, this.albumId, toAlbum.id)
				this.noteList = this.noteList.filter(n => n.id !== this.currentNote.id)
				uni.showToast({ title: '已移动到「' + toAlbum.title + '」', icon: 'success' })
			} catch (e) {
				uni.showToast({ title: '移动失败', icon: 'none' })
			}
		},

		toDetail(noteId) {
			uni.navigateTo({ url: '/pages/discover/detail?noteId=' + noteId })
		}
	}
}
</script>

<style scoped>
.album-detail-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 30rpx; }
.album-header { background: #fff; padding: 40rpx 30rpx; text-align: center; }
.album-title { font-size: 36rpx; font-weight: 600; color: #333; }
.album-meta { display: block; font-size: 24rpx; color: #999; margin-top: 10rpx; }

.delete-bar { padding: 20rpx 30rpx; }
.btn-delete-album {
	width: 100%; height: 80rpx; line-height: 80rpx;
	background: #fff; color: #f53f3f; border: 1px solid #f53f3f;
	border-radius: 8rpx; font-size: 28rpx;
}

.note-list { background: #fff; }
.note-item {
	display: flex; align-items: center; padding: 20rpx 30rpx;
	border-bottom: 1px solid #f5f5f5;
}
.note-cover { width: 100rpx; height: 100rpx; border-radius: 8rpx; flex-shrink: 0; }
.note-cover-empty {
	width: 100rpx; height: 100rpx; border-radius: 8rpx; flex-shrink: 0;
	background: #f0f0f0; display: flex; align-items: center; justify-content: center;
	font-size: 40rpx;
}
.note-info { flex: 1; margin-left: 20rpx; }
.note-title-text { font-size: 28rpx; color: #333; display: block; }
.note-author { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.note-action { padding: 10rpx 20rpx; }
.note-action-icon { font-size: 36rpx; color: #999; letter-spacing: 4rpx; }

/* 操作弹窗 */
.action-overlay {
	position: fixed; top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(0,0,0,0.5); z-index: 200;
	display: flex; align-items: flex-end; justify-content: center;
}
.action-sheet {
	width: 100%; background: #fff;
	border-radius: 24rpx 24rpx 0 0; padding: 30rpx;
}
.action-title { font-size: 28rpx; color: #999; text-align: center; margin-bottom: 20rpx; }
.action-item {
	padding: 30rpx 0; text-align: center; font-size: 30rpx; color: #333;
	border-bottom: 1px solid #f5f5f5;
}
.action-item.warn { color: #f53f3f; }
.action-item.cancel { border: none; background: #f5f5f5; border-radius: 8rpx; margin-top: 15rpx; }
.move-list { max-height: 500rpx; }
</style>
