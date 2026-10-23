<template>
	<view class="detail-page">
		<swiper v-if="noteInfo.imgList && noteInfo.imgList.length > 0" class="img-swiper" :autoplay="false" indicator-dots>
			<swiper-item v-for="(url, index) in noteInfo.imgList" :key="index">
				<image :src="getFullUrl(url)" mode="aspectFit" class="swiper-img" @click="previewImg(index)" />
			</swiper-item>
		</swiper>
		<view v-else class="no-img"><text class="no-img-text">暂无图片</text></view>

		<view class="author-bar">
			<view class="author-info" @click="toUser">
				<image :src="getFullUrl(noteInfo.avatar)" mode="aspectFill" class="author-avatar" />
				<text class="author-name">{{ noteInfo.username }}</text>
			</view>
			<view class="follow-btn" v-if="currentUserId !== noteInfo.uid">
				<button class="btn-follow" :class="noteInfo.isFollow ? 'btn-followed' : ''" @click="onFollow">
					{{ noteInfo.isFollow ? '已关注' : '+ 关注' }}
				</button>
			</view>
		</view>

		<view class="note-body card">
			<view class="note-title">{{ noteInfo.title }}</view>
			<view class="note-content"><text>{{ noteInfo.content }}</text></view>
			<view class="note-tags" v-if="noteInfo.tagList && noteInfo.tagList.length > 0">
				<text class="tag" v-for="(tag, idx) in noteInfo.tagList" :key="idx">#{{ tag.title || tag }}</text>
			</view>
			<view class="note-time">{{ noteInfo.time }}</view>
		</view>

		<view class="comment-section card">
			<view class="comment-count">共 {{ noteInfo.commentCount || 0 }} 条评论</view>
			<view class="comment-list">
				<view v-for="comment in commentList" :key="comment.id" class="comment-w" :id="'comment-' + comment.id">
					<comment-item :comment="comment" :highlight="highlightCommentId === comment.id" :expandAll="comment._expandAll || false" @reply="onReply"></comment-item>
				</view>
			</view>
			<loading-more :loading="commentLoading" :noMore="commentNoMore" />
			<empty-state text="暂无评论，来抢沙发吧~" v-if="!commentLoading && commentList.length === 0" />
		</view>

		<view class="detail-footer safe-bottom">
			<view class="footer-actions">
				<view class="footer-btn" @click="onLike">
					<text :class="noteInfo.isLike ? 'action-active' : ''">{{ noteInfo.isLike ? '❤️' : '🤍' }}</text>
					<text class="action-num">{{ noteInfo.likeCount || 0 }}</text>
				</view>
				<view class="footer-btn" @click="onCollect">
					<text :class="noteInfo.isCollection ? 'action-active' : ''">{{ noteInfo.isCollection ? '⭐' : '☆' }}</text>
					<text class="action-num">{{ noteInfo.collectionCount || 0 }}</text>
				</view>
				<view class="footer-btn" @click="focusComment">
					<text>💬</text>
					<text class="action-num">{{ noteInfo.commentCount || 0 }}</text>
				</view>
			</view>
			<comment-input ref="commentInput" :replyInfo="replyInfo" @send="onSendComment" @clear="onClearReply"></comment-input>
		</view>

		<view class="album-overlay" v-if="albumPickerVisible" @click="closeAlbumPicker">
			<view class="album-sheet" @click.stop>
				<view class="album-sheet-title">收藏到专辑</view>
				<scroll-view class="album-list" scroll-y v-if="!showAddAlbum">
					<view class="album-item" v-for="album in albumList" :key="album.id" @click="selectAlbum(album)">
						<text class="album-icon">📁</text>
						<view class="album-name">{{ album.title }}</view>
						<text class="album-count">{{ album.imgCount || 0 }}张</text>
					</view>
					<empty-state text="还没有专辑，创建一个吧" v-if="albumList.length === 0" />
				</scroll-view>
				<view class="album-add-row" v-if="!showAddAlbum">
					<button class="btn-add-album" @click="showAddAlbum = true">+ 新建专辑</button>
				</view>
				<view class="album-add-input" v-else>
					<input v-model="newAlbumName" placeholder="输入专辑名称" maxlength="20" class="input-album-name" focus />
					<view class="add-album-btns">
						<button class="btn-confirm" @click="createAlbum">创建并收藏</button>
						<button class="btn-cancel-text" @click="showAddAlbum = false">取消</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import CommentItem from '@/components/comment-item.vue'
import CommentInput from '@/components/comment-input.vue'
import LoadingMore from '@/components/loading-more.vue'
import EmptyState from '@/components/empty-state.vue'
import { getNoteById } from '@/api/note.js'
import { getCommentPageWithCommentByNoteId, saveCommentByDTO, syncCommentByIds } from '@/api/comment.js'
import { likeOrCollectionByDTO } from '@/api/likeOrCollection.js'
import { followById } from '@/api/follower.js'
import { getAlbumPageByUserId, saveAlbumByDTO } from '@/api/album.js'
import { formateTime, getFullUrl } from '@/utils/util.js'
import userStore from '@/store/userStore.js'

export default {
	components: { CommentItem, CommentInput, LoadingMore, EmptyState },
	data() {
		return {
			noteInfo: { id: '', title: '', content: '', uid: '', username: '', avatar: '', imgList: [], tagList: [], time: '', likeCount: 0, collectionCount: 0, commentCount: 0, isLike: false, isCollection: false, isFollow: false },
			commentList: [], commentCurrentPage: 1, commentPageSize: 10, commentTotal: 0,
			commentLoading: false, commentNoMore: false,
			replyInfo: null, commentIds: [], noteId: '', highlightCommentId: '',
			albumPickerVisible: false, albumList: [], showAddAlbum: false, newAlbumName: ''
		}
	},
	computed: {
		currentUserId() { return userStore.getUserId() }
	},
	onLoad(options) {
		this.noteId = options.noteId || ''
		this.highlightCommentId = options.commentId || ''
		if (this.noteId) { this.loadNoteDetail(); this.loadComments() }
	},
	onUnload() {
		if (this.commentIds.length > 0) { syncCommentByIds(this.commentIds).catch(() => {}) }
	},
	methods: {
		formateTime, getFullUrl,

		async loadNoteDetail() {
			try {
				const res = await getNoteById(this.noteId)
				if (res && res.data) {
					const data = res.data
					this.noteInfo = { ...data, imgList: data.urls ? JSON.parse(data.urls) : [], time: formateTime(data.time) }
				}
			} catch (e) {}
		},

		async loadComments() {
			this.commentLoading = true
			try {
				const res = await getCommentPageWithCommentByNoteId(this.commentCurrentPage, this.commentPageSize, this.noteId)
				if (res && res.data) {
					const { records, total } = res.data
					this.commentTotal = total || 0
					this.commentList = records || []
					this.commentNoMore = this.commentList.length >= this.commentTotal
				}
			} catch (e) {}
			this.commentLoading = false
			this.scrollToComment()
		},

		onReply(comment) { this.replyInfo = comment },
		onClearReply() { this.replyInfo = null },

		async onSendComment(content) {
			const commentDTO = { nid: this.noteId, noteUid: this.noteInfo.uid, pid: '0', replyId: '0', replyUid: this.noteInfo.uid, level: 1, content }
			if (this.replyInfo) {
				commentDTO.pid = this.replyInfo.pid === '0' ? this.replyInfo.id : this.replyInfo.pid
				commentDTO.replyId = this.replyInfo.id
				commentDTO.replyUid = this.replyInfo.uid
				commentDTO.level = 2
			}
			try {
				const res = await saveCommentByDTO(commentDTO)
				if (res && res.data) {
					this.commentIds.push(res.data.id)
					await syncCommentByIds([res.data.id])
					this.commentIds = this.commentIds.filter(id => id !== res.data.id)
					this.noteInfo.commentCount++
					this.replyInfo = null
					uni.showToast({ title: '评论成功', icon: 'success' })
					this.commentCurrentPage = 1
					this.loadComments()
				}
			} catch (e) {}
		},

		async onLike() {
			const data = { likeOrCollectionId: this.noteInfo.id, publishUid: this.noteInfo.uid, type: 1 }
			try { await likeOrCollectionByDTO(data); this.noteInfo.isLike = !this.noteInfo.isLike; this.noteInfo.likeCount += this.noteInfo.isLike ? 1 : -1 } catch (e) {}
		},

		async onCollect() {
			if (this.noteInfo.isCollection) {
				const data = { likeOrCollectionId: this.noteInfo.id, publishUid: this.noteInfo.uid, type: 3 }
				try { await likeOrCollectionByDTO(data); this.noteInfo.isCollection = false; this.noteInfo.collectionCount = Math.max(0, this.noteInfo.collectionCount - 1) } catch (e) {}
				return
			}
			this.albumPickerVisible = true; this.showAddAlbum = false; this.newAlbumName = ''; this.loadAlbumList()
		},

		async loadAlbumList() {
			try { const res = await getAlbumPageByUserId(1, 50, this.currentUserId); this.albumList = (res && res.data && res.data.records) ? res.data.records : [] } catch (e) { this.albumList = [] }
		},

		async selectAlbum(album) {
			const data = { likeOrCollectionId: this.noteInfo.id, publishUid: this.noteInfo.uid, type: 3, albumId: album.id }
			try { await likeOrCollectionByDTO(data); this.noteInfo.isCollection = true; this.noteInfo.collectionCount++; this.albumPickerVisible = false; uni.showToast({ title: '已收藏到「' + album.title + '」', icon: 'success' }) } catch (e) { uni.showToast({ title: '收藏失败', icon: 'none' }) }
		},

		async createAlbum() {
			const name = this.newAlbumName.trim()
			if (!name) { uni.showToast({ title: '请输入专辑名称', icon: 'none' }); return }
			try { await saveAlbumByDTO({ title: name, uid: this.currentUserId }); this.showAddAlbum = false; this.newAlbumName = ''; uni.showToast({ title: '专辑已创建', icon: 'success' }); await this.loadAlbumList() } catch (e) { uni.showToast({ title: '创建失败', icon: 'none' }) }
		},

		closeAlbumPicker() { this.albumPickerVisible = false; this.showAddAlbum = false },

		async onFollow() {
			try { await followById(this.noteInfo.uid); this.noteInfo.isFollow = !this.noteInfo.isFollow } catch (e) {}
		},

		scrollToComment() {
			if (!this.highlightCommentId) return
			const targetId = this.highlightCommentId
			// 子评论可能在 parent.children 里，先展开父评论
			let parentIdx = this.commentList.findIndex(c => c.id === targetId)
			if (parentIdx < 0) {
				for (let i = 0; i < this.commentList.length; i++) {
					const children = this.commentList[i].children
					if (children && children.some(c => c.id === targetId)) {
						this.$set(this.commentList[i], '_expandAll', true)
						parentIdx = i
						break
					}
				}
			}
			if (parentIdx < 0) return

			let tries = 0
			const tryScroll = () => {
				uni.createSelectorQuery().selectAll('.comment-w').boundingClientRect(rects => {
					if (rects && rects[parentIdx]) {
						uni.pageScrollTo({ scrollTop: rects[parentIdx].top - 200, duration: 300 })
					} else if (++tries < 10) {
						setTimeout(tryScroll, 300)
					}
				}).exec()
			}
			this.$nextTick(() => setTimeout(tryScroll, 400))
		},

		focusComment() {},
		toUser() { uni.navigateTo({ url: '/pages/profile/index?userId=' + this.noteInfo.uid }) },
		previewImg(idx) { uni.previewImage({ current: idx, urls: this.noteInfo.imgList.map(getFullUrl) }) }
	}
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 200rpx; }
.img-swiper { width: 100%; height: 750rpx; background: #000; }
.swiper-img { width: 100%; height: 100%; }
.no-img { height: 400rpx; display: flex; align-items: center; justify-content: center; background: #f0f0f0; }
.no-img-text { color: #999; font-size: 28rpx; }
.author-bar { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 30rpx; background: #fff; border-bottom: 1px solid #f5f5f5; }
.author-info { display: flex; align-items: center; }
.author-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; }
.author-name { font-size: 30rpx; font-weight: 600; margin-left: 20rpx; color: #333; }
.btn-follow { width: 140rpx; height: 60rpx; line-height: 60rpx; font-size: 26rpx; background: #f53f3f; color: #fff; border-radius: 999rpx; border: none; padding: 0; text-align: center; }
.btn-followed { background: #999; }
.note-body { margin-bottom: 15rpx; }
.note-title { font-size: 36rpx; font-weight: 600; color: #333; line-height: 1.4; margin-bottom: 15rpx; }
.note-content { font-size: 30rpx; color: #333; line-height: 1.6; margin-bottom: 20rpx; white-space: pre-wrap; }
.note-tags { display: flex; flex-wrap: wrap; gap: 10rpx; margin-bottom: 15rpx; }
.tag { color: #1677ff; font-size: 26rpx; margin-right: 15rpx; }
.note-time { font-size: 24rpx; color: #999; }
.comment-section { margin-bottom: 15rpx; }
.comment-count { font-size: 30rpx; font-weight: 600; color: #333; margin-bottom: 20rpx; }
.detail-footer { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; z-index: 50; }
.footer-actions { display: flex; align-items: center; justify-content: space-around; padding: 15rpx 0 0; }
.footer-btn { display: flex; align-items: center; font-size: 36rpx; }
.action-num { font-size: 24rpx; color: #999; margin-left: 8rpx; }
.action-active { color: #f53f3f; }
.album-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 200; display: flex; align-items: flex-end; justify-content: center; }
.album-sheet { width: 100%; max-height: 70vh; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 30rpx; display: flex; flex-direction: column; }
.album-sheet-title { font-size: 32rpx; font-weight: 600; color: #333; text-align: center; margin-bottom: 20rpx; }
.album-list { max-height: 500rpx; }
.album-item { display: flex; align-items: center; padding: 25rpx 0; border-bottom: 1px solid #f5f5f5; }
.album-icon { font-size: 40rpx; margin-right: 20rpx; }
.album-name { flex: 1; font-size: 28rpx; color: #333; }
.album-count { font-size: 24rpx; color: #999; }
.album-add-row { padding: 30rpx 0 10rpx; }
.btn-add-album { width: 100%; height: 80rpx; line-height: 80rpx; background: #1677ff; color: #fff; border-radius: 8rpx; font-size: 28rpx; border: none; text-align: center; }
.album-add-input { padding: 20rpx 0; }
.input-album-name { height: 80rpx; line-height: 80rpx; padding: 0 20rpx; border: 1px solid #e5e5e5; border-radius: 8rpx; font-size: 28rpx; margin-bottom: 20rpx; }
.add-album-btns { display: flex; gap: 20rpx; }
.btn-confirm { flex: 1; height: 80rpx; line-height: 80rpx; background: #1677ff; color: #fff; border-radius: 8rpx; font-size: 28rpx; border: none; }
.btn-cancel-text { flex: 1; height: 80rpx; line-height: 80rpx; background: #f5f5f5; color: #666; border-radius: 8rpx; font-size: 28rpx; border: none; }
</style>