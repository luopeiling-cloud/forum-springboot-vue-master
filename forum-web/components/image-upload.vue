<template>
	<view class="image-upload">
		<view class="img-grid">
			<view v-for="(img, index) in fileList" :key="index" class="img-item">
				<image :src="img" mode="aspectFill" class="upload-img" @click="previewImage(index)" />
				<view class="img-close" @click="removeImage(index)">
					<text>✕</text>
				</view>
			</view>
			<view v-if="fileList.length < maxCount" class="img-add" @click="chooseImage">
				<text class="add-icon">+</text>
				<text class="add-text">上传图片</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'ImageUpload',
		props: {
			maxCount: {
				type: Number,
				default: 9
			}
		},
		data() {
			return {
				fileList: []
			}
		},
		methods: {
			chooseImage() {
				const remain = this.maxCount - this.fileList.length
				uni.chooseImage({
					count: remain,
					sizeType: ['compressed'],
					sourceType: ['album', 'camera'],
					success: (res) => {
						this.fileList.push(...res.tempFilePaths)
						this.$emit('change', this.fileList)
					}
				})
			},
			removeImage(index) {
				this.fileList.splice(index, 1)
				this.$emit('change', this.fileList)
			},
			previewImage(index) {
				uni.previewImage({
					current: index,
					urls: this.fileList
				})
			},
			getFiles() {
				return this.fileList
			},
			clear() {
				this.fileList = []
			}
		}
	}
</script>

<style scoped>
	.img-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 15rpx;
	}
	.img-item {
		width: 210rpx;
		height: 210rpx;
		border-radius: 8rpx;
		overflow: hidden;
		position: relative;
	}
	.upload-img {
		width: 100%;
		height: 100%;
	}
	.img-close {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		width: 40rpx;
		height: 40rpx;
		line-height: 40rpx;
		text-align: center;
		background: rgba(0,0,0,0.5);
		color: #fff;
		border-radius: 50%;
		font-size: 22rpx;
	}
	.img-add {
		width: 210rpx;
		height: 210rpx;
		border: 1px dashed #e5e5e5;
		border-radius: 8rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		color: #999;
	}
	.add-icon {
		font-size: 60rpx;
		line-height: 1;
	}
	.add-text {
		font-size: 24rpx;
		margin-top: 10rpx;
	}
</style>
