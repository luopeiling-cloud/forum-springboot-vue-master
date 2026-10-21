<template>
	<view class="publish-page">
		<!-- 图片上传 -->
		<view class="section card">
			<image-upload ref="imageUpload" @change="onFilesChange" />
		</view>

		<!-- 标题 -->
		<view class="section card">
			<input
				class="input-title"
				v-model="title"
				placeholder="请输入标题（最多20字）"
				maxlength="20"
			/>
		</view>

		<!-- 正文 -->
		<view class="section card">
			<textarea
				class="input-content"
				v-model="content"
				placeholder="分享你的校园生活..."
				maxlength="200"
				:show-confirm-bar="false"
			/>
		</view>

		<!-- 分类选择 -->
		<view class="section card">
			<view class="section-label">选择分类</view>
			<picker
				mode="multiSelector"
				:range="categoryRange"
				range-key="title"
t			@columnchange="onColumnChange"
				@change="onCategoryChange"
			>
				<view class="picker-value">
					{{ categoryText || '请选择分类' }}
					<text class="picker-arrow">▼</text>
				</view>
			</picker>
		</view>

		<!-- 标签 -->
		<view class="section card">
			<view class="section-label"># 话题标签</view>
			<input
				class="tag-input"
				v-model="tagInput"
				placeholder="用 # 分隔，如 #美食#校园#考研"
			/>
		</view>

		<!-- 发布按钮 -->
		<view class="publish-btn-area">
			<button class="btn-publish" :disabled="publishing" @click="onPublish">
				{{ publishing ? '发布中...' : '发布' }}
			</button>
			<button class="btn-cancel" @click="onCancel">取消</button>
		</view>
	</view>
</template>

<script>
import ImageUpload from '@/components/image-upload.vue'
import { requireLogin } from '@/utils/auth.js'
import { getCategoryTreeData } from '@/api/category.js'
import { saveNoteByDTO } from '@/api/note.js'
import userStore from '@/store/userStore.js'

export default {
	components: { ImageUpload },
	data() {
		return {
			title: '',
			content: '',
			files: [],
			categoryRange: [[], []],
			selectedCpId: '',
			selectedCid: '',
			categoryText: '',
			tagInput: '',
			publishing: false,
			allCategories: []
		}
	},
	onLoad() {
		if (!requireLogin()) return
		this.loadCategories()
	},
	onShow() {
		this.resetForm()
	},
	methods: {
		resetForm() {
			this.title = ''
			this.content = ''
			this.files = []
			this.selectedCpId = ''
			this.selectedCid = ''
			this.categoryText = ''
			this.tagInput = ''
			this.publishing = false
			const imgUpload = this.$refs.imageUpload
			if (imgUpload && imgUpload.clear) {
				imgUpload.clear()
			}
		},

		async loadCategories() {
			try {
				const res = await getCategoryTreeData()
				if (res && res.data) {
					this.allCategories = res.data || []
					const level1 = this.allCategories
					this.categoryRange = [level1, []]
					if (level1.length > 0 && level1[0].children) {
						this.categoryRange[1] = level1[0].children || []
					}
				}
			} catch (e) {}
		},

		onFilesChange(files) {
			this.files = files
		},

		onColumnChange(e) {
				const { column, value } = e.detail
				if (column === 0) {
					const parent = this.allCategories[value]
					if (parent) {
						this.categoryRange = [this.categoryRange[0], parent.children || []]
					}
				}
			},

		onCategoryChange(e) {
			const [pIdx, cIdx] = e.detail.value
			const parent = this.allCategories[pIdx]
			if (parent) {
				this.selectedCpId = parent.id
				const children = parent.children || []
				this.categoryRange[1] = children
				if (children.length > 0 && cIdx < children.length) {
					this.selectedCid = children[cIdx].id
					this.categoryText = parent.title + ' > ' + children[cIdx].title
				} else {
					this.categoryText = parent.title
				}
			}
		},

		/** 解析标签：#美食#校园 → ['美食', '校园'] */
		parseTags(raw) {
			if (!raw) return []
			return raw.split('#')
				.map(t => t.trim())
				.filter(t => t.length > 0)
		},

		async onPublish() {
			if (this.files.length === 0) {
				uni.showToast({ title: '请至少上传一张图片', icon: 'none' })
				return
			}
			if (!this.title.trim() || !this.content.trim()) {
				uni.showToast({ title: '标题和内容不能为空', icon: 'none' })
				return
			}
			if (!this.selectedCpId) {
				uni.showToast({ title: '请选择分类', icon: 'none' })
				return
			}

			this.publishing = true

			const noteData = {
				title: this.title.trim(),
				content: this.content.trim(),
				cpid: this.selectedCpId,
				cid: this.selectedCid || '',
				tagList: this.parseTags(this.tagInput),
				count: this.files.length,
				type: 1,
				noteCoverHeight: 200
			}

			try {
				await saveNoteByDTO({ noteData: JSON.stringify(noteData), files: this.files })
				this.resetForm()
				uni.showToast({ title: '发布成功', icon: 'success' })
				setTimeout(() => {
					uni.switchTab({ url: '/pages/discover/index' })
				}, 1500)
			} catch (e) {
				uni.showToast({ title: '发布失败，请重试', icon: 'none' })
			}
			this.publishing = false
		},

		onCancel() {
			uni.switchTab({ url: '/pages/discover/index' })
		}
	}
}
</script>

<style scoped>
.publish-page { min-height: 100vh; background: #f5f5f5; padding: 20rpx; }
.section { margin-bottom: 20rpx; }
.section-label { font-size: 28rpx; color: #666; margin-bottom: 15rpx; }
.input-title { height: 80rpx; line-height: 80rpx; padding: 0 20rpx; border: 1px solid #e5e5e5; border-radius: 8rpx; font-size: 28rpx; background: #fff; }
.input-content { width: 100%; min-height: 200rpx; padding: 20rpx; border: 1px solid #e5e5e5; border-radius: 8rpx; font-size: 28rpx; background: #fff; box-sizing: border-box; }
.picker-value { height: 80rpx; line-height: 80rpx; padding: 0 20rpx; border: 1px solid #e5e5e5; border-radius: 8rpx; font-size: 28rpx; color: #333; display: flex; justify-content: space-between; }
.picker-arrow { color: #999; font-size: 24rpx; }
.tag-input { width: 100%; height: 70rpx; line-height: 70rpx; padding: 0 20rpx; border: 1px solid #e5e5e5; border-radius: 8rpx; font-size: 26rpx; background: #fff; box-sizing: border-box; }
.publish-btn-area { padding: 40rpx 0; }
.btn-publish { width: 100%; height: 88rpx; line-height: 88rpx; background: #ff2442; color: #fff; border-radius: 8rpx; font-size: 32rpx; border: none; margin-bottom: 20rpx; }
.btn-publish[disabled] { background: #ccc; }
.btn-cancel { width: 100%; height: 88rpx; line-height: 88rpx; background: #fff; color: #666; border-radius: 8rpx; font-size: 32rpx; border: 1px solid #e5e5e5; }
</style>
