<template>
	<view class="edit-page">
		<view class="edit-section">
			<!-- 头像 -->
			<view class="edit-item" @click="changeAvatar">
				<text class="edit-label">头像</text>
				<view class="avatar-wrap">
					<image :src="avatarPreview || getFullUrl(avatar)" mode="aspectFill" class="edit-avatar" />
					<text class="avatar-hint">点击更换</text>
				</view>
			</view>

			<!-- 封面 -->
			<view class="edit-item" @click="changeCover">
				<text class="edit-label">封面</text>
				<view class="cover-wrap">
					<image v-if="coverPreview || userCover" :src="coverPreview || getFullUrl(userCover)" mode="aspectFill" class="edit-cover" />
					<view v-else class="edit-cover-empty">🖼</view>
					<text class="cover-hint">点击更换</text>
				</view>
			</view>

			<view class="edit-item">
				<text class="edit-label">用户名</text>
				<input class="edit-input" v-model="username" placeholder="请输入用户名" />
			</view>

			<view class="edit-item">
				<text class="edit-label">简介</text>
				<textarea class="edit-textarea" v-model="description" placeholder="介绍一下自己..." />
			</view>

			<view class="edit-item">
				<text class="edit-label">性别</text>
				<view class="picker-wrap"><picker mode="selector" :range="genderOptions" @change="onGenderChange">
					<view class="picker-value">
						{{ genderText }}
						<text class="picker-arrow">▼</text>
					</view>
				</picker></view>
			</view>

			<view class="edit-item">
				<text class="edit-label">生日</text>
				<view class="picker-wrap"><picker mode="date" :value="birthday" @change="onBirthdayChange">
					<view class="picker-value">
						{{ birthday || '请选择' }}
						<text class="picker-arrow">▼</text>
					</view>
				</picker></view>
			</view>

			<view class="edit-item">
				<text class="edit-label">地址</text>
				<input class="edit-input" v-model="address" placeholder="请输入地址" />
			</view>
		</view>

		<view class="save-area">
			<button class="btn-save" :disabled="saving" @click="onSave">
				{{ saving ? '保存中...' : '保存' }}
			</button>
		</view>
	</view>
</template>

<script>
import userStore from '@/store/userStore.js'
import { updateUser } from '@/api/user.js'
import { getFullUrl } from '@/utils/util.js'
import { BASE_URL } from '@/utils/request.js'

export default {
	data() {
		const info = userStore.getUserInfo() || {}
		return {
			avatar: info.avatar || '',
			avatarPreview: '',
			avatarFile: '',
			userCover: info.userCover || '',
			coverPreview: '',
			coverFile: '',
			username: info.username || '',
			description: info.description || '',
			gender: info.gender != null ? info.gender : 0,
			genderOptions: ['保密', '男', '女'],
			birthday: info.birthday || '',
			address: info.address || '',
			saving: false
		}
	},
	computed: {
		genderText() {
			return this.genderOptions[this.gender] || '保密'
		}
	},
	methods: {
		getFullUrl,
		changeAvatar() {
			uni.chooseImage({
				count: 1, sizeType: ['compressed'], sourceType: ['album'],
				success: (res) => {
					this.avatarFile = res.tempFilePaths[0]
					this.avatarPreview = res.tempFilePaths[0]
				}
			})
		},
		changeCover() {
			uni.chooseImage({
				count: 1, sizeType: ['compressed'], sourceType: ['album'],
				success: (res) => {
					this.coverFile = res.tempFilePaths[0]
					this.coverPreview = res.tempFilePaths[0]
				}
			})
		},
		onGenderChange(e) {
			this.gender = parseInt(e.detail.value)
		},
		onBirthdayChange(e) {
			this.birthday = e.detail.value
		},
		async onSave() {
			if (!this.username.trim()) {
				uni.showToast({ title: '用户名不能为空', icon: 'none' })
				return
			}
			this.saving = true
			try {
				let avatarUrl = this.avatar
				let coverUrl = this.userCover
				if (this.avatarFile) avatarUrl = await this.uploadFile(this.avatarFile)
				if (this.coverFile) coverUrl = await this.uploadFile(this.coverFile)

				const userData = {
					id: userStore.getUserId(),
					username: this.username.trim(),
					description: this.description.trim(),
					avatar: avatarUrl,
					userCover: coverUrl,
					gender: this.gender,
					birthday: this.birthday,
					address: this.address.trim()
				}

				const res = await updateUser(userData)
				if (res && res.data) {
					const info = userStore.getUserInfo() || {}
					Object.assign(info, {
						username: res.data.username || userData.username,
						description: res.data.description || userData.description,
						avatar: res.data.avatar || avatarUrl,
						userCover: res.data.userCover || coverUrl,
						gender: res.data.gender != null ? res.data.gender : userData.gender,
						birthday: res.data.birthday || userData.birthday,
						address: res.data.address || userData.address
					})
					userStore.setUserInfo(info)
					uni.showToast({ title: '保存成功', icon: 'success' })
					setTimeout(() => uni.navigateBack(), 1000)
				} else {
					uni.showToast({ title: '保存失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '保存失败', icon: 'none' })
			}
			this.saving = false
		},
		uploadFile(filePath) {
			return new Promise((resolve, reject) => {
				const accessToken = uni.getStorageSync('accessToken') || ''
				const userInfo = uni.getStorageSync('userInfo')
				const userId = userInfo ? userInfo.id : ''
				uni.uploadFile({
					url: BASE_URL + '/util/oss/saveBatch/1',
					filePath, name: 'uploadFiles',
					header: { 'accessToken': accessToken, 'userId': userId },
					success: (res) => {
						try {
							const data = JSON.parse(res.data)
							if (data.code === 200 && data.data) {
								const urls = Array.isArray(data.data) ? data.data[0] : data.data
								resolve(urls)
							} else { reject(data) }
						} catch (e) { reject(e) }
					},
					fail: reject
				})
			})
		}
	}
}
</script>

<style scoped>
.edit-page { min-height: 100vh; background: #f5f5f5; padding-top: 20rpx; }
.edit-section { background: #fff; padding: 0 30rpx; }
.edit-item { display: flex; align-items: center; justify-content: space-between; padding: 30rpx 0; border-bottom: 1px solid #f5f5f5; }
.edit-label { font-size: 28rpx; color: #333; width: 140rpx; flex-shrink: 0; }
.avatar-wrap, .cover-wrap { display: flex; align-items: center; gap: 15rpx; }
.edit-avatar { width: 120rpx; height: 120rpx; border-radius: 50%; flex-shrink: 0; }
.avatar-hint, .cover-hint { font-size: 24rpx; color: #999; }
.edit-cover { width: 160rpx; height: 100rpx; border-radius: 8rpx; flex-shrink: 0; }
.edit-cover-empty { width: 160rpx; height: 100rpx; border-radius: 8rpx; flex-shrink: 0; background: #f0f0f0; display: flex; align-items: center; justify-content: center; font-size: 36rpx; }
.edit-input { flex: 1; height: 80rpx; line-height: 80rpx; padding: 0 20rpx; border: 1px solid #eee; border-radius: 10rpx; font-size: 28rpx; }
.edit-textarea { flex: 1; min-height: 120rpx; padding: 15rpx; border: 1px solid #eee; border-radius: 10rpx; font-size: 28rpx; }
.picker-wrap { flex: 1; min-width: 0; overflow: hidden; }
.picker-value { height: 80rpx; line-height: 80rpx; padding: 0 20rpx; font-size: 28rpx; color: #333; display: flex; justify-content: space-between; }
.picker-arrow { color: #999; font-size: 24rpx; }
.save-area { padding: 60rpx 30rpx; }
.btn-save { width: 100%; height: 88rpx; line-height: 88rpx; background: #1677ff; color: #fff; border-radius: 8rpx; font-size: 32rpx; border: none; }
.btn-save[disabled] { background: #ccc; }
</style>
