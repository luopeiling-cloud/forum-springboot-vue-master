<template>
	<view class="security-page">
		<!-- 基本信息 -->
		<view class="section">
			<view class="section-title">基本信息</view>
			<view class="info-item">
				<text class="info-label">校园号</text>
				<text class="info-value">{{ userInfo.yxId || '—' }}</text>
			</view>
			<view class="info-item" @click="editField('phone')">
				<text class="info-label">手机号</text>
				<text class="info-value">{{ userInfo.phone || '未绑定' }}</text>
				<text class="info-arrow">></text>
			</view>
			<view class="info-item" @click="editField('email')">
				<text class="info-label">邮箱</text>
				<text class="info-value">{{ userInfo.email || '未绑定' }}</text>
				<text class="info-arrow">></text>
			</view>
		</view>

		<!-- 修改密码 -->
		<view class="section">
			<view class="section-title">修改密码</view>
			<view class="pwd-form">
				<input class="pwd-input" v-model="oldPwd" type="password" placeholder="旧密码" />
				<input class="pwd-input" v-model="newPwd" type="password" placeholder="新密码（8-20位）" maxlength="20" />
				<input class="pwd-input" v-model="confirmPwd" type="password" placeholder="确认新密码" maxlength="20" />
				<button class="btn-save" :disabled="savingPwd" @click="onChangePwd">
					{{ savingPwd ? '保存中...' : '修改密码' }}
				</button>
			</view>
		</view>

		<!-- 编辑弹窗 -->
		<view class="modal-overlay" v-if="showModal" @click="showModal = false">
			<view class="modal-card" @click.stop>
				<view class="modal-title">{{ modalTitle }}</view>
				<input
					class="modal-input"
					v-model="modalValue"
					:placeholder="modalPlaceholder"
					:type="modalField === 'email' ? 'text' : 'number'"
					:maxlength="modalField === 'phone' ? 11 : 100"
					focus
				/>
				<view class="modal-btns">
					<button class="btn-cancel" @click="showModal = false">取消</button>
					<button class="btn-confirm" @click="saveField">保存</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { updatePassword } from '@/api/auth.js'
import { updateUser } from '@/api/user.js'
import userStore from '@/store/userStore.js'

export default {
	data() {
		return {
			userInfo: userStore.getUserInfo() || {},
			oldPwd: '',
			newPwd: '',
			confirmPwd: '',
			savingPwd: false,
			showModal: false,
			modalField: '',
			modalValue: ''
		}
	},
	computed: {
		modalTitle() {
			return this.modalField === 'phone' ? '修改手机号' : '修改邮箱'
		},
		modalPlaceholder() {
			return this.modalField === 'phone' ? '请输入手机号' : '请输入邮箱'
		}
	},
	methods: {
		editField(field) {
			this.modalField = field
			this.modalValue = this.userInfo[field] || ''
			this.showModal = true
		},
		async saveField() {
			const val = this.modalValue.trim()
			try {
				const userData = { id: userStore.getUserId() }
				userData[this.modalField] = val
				const res = await updateUser(userData)
				if (res && res.data) {
					const info = { ...this.userInfo }
					info[this.modalField] = res.data[this.modalField] !== undefined ? res.data[this.modalField] : val
					this.userInfo = info
					userStore.setUserInfo(info)
					uni.showToast({ title: '已更新', icon: 'success' })
				}
			} catch (e) {
				uni.showToast({ title: '修改失败', icon: 'none' })
			}
			this.showModal = false
		},
		async onChangePwd() {
			if (!this.oldPwd) {
				uni.showToast({ title: '请输入旧密码', icon: 'none' })
				return
			}
			if (!this.newPwd || this.newPwd.length < 8) {
				uni.showToast({ title: '新密码至少8位', icon: 'none' })
				return
			}
			if (this.newPwd !== this.confirmPwd) {
				uni.showToast({ title: '两次密码不一致', icon: 'none' })
				return
			}


			this.savingPwd = true
			try {
				const res = await updatePassword({
					id: userStore.getUserId(),
					password: this.newPwd,
					oldPassword: this.oldPwd,
					checkPassword: this.confirmPwd
				})
				if (res && res.code === 200) {
					uni.showToast({ title: '密码修改成功', icon: 'success' })
					this.oldPwd = ''
					this.newPwd = ''
					this.confirmPwd = ''
				} else {
					uni.showToast({ title: '修改失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '修改失败', icon: 'none' })
			}
			this.savingPwd = false
		}
	}
}
</script>

<style scoped>
.security-page { min-height: 100vh; background: #f5f5f5; padding-top: 20rpx; }
.section { background: #fff; margin-bottom: 20rpx; padding: 0 30rpx; }
.section-title { font-size: 26rpx; color: #999; padding: 25rpx 0 15rpx; }
.info-item { display: flex; align-items: center; padding: 30rpx 0; border-bottom: 1px solid #f5f5f5; }
.info-item:last-child { border-bottom: none; }
.info-label { font-size: 28rpx; color: #333; width: 160rpx; }
.info-value { flex: 1; font-size: 28rpx; color: #666; text-align: right; }
.info-arrow { font-size: 28rpx; color: #ccc; margin-left: 15rpx; }

.pwd-form { padding-bottom: 30rpx; }
.pwd-input { height: 80rpx; line-height: 80rpx; padding: 0 20rpx; border: 1px solid #e5e5e5; border-radius: 8rpx; font-size: 28rpx; margin-bottom: 20rpx; }
.btn-save { width: 100%; height: 88rpx; line-height: 88rpx; background: #1677ff; color: #fff; border-radius: 8rpx; font-size: 28rpx; border: none; margin-top: 10rpx; }
.btn-save[disabled] { background: #ccc; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 200; display: flex; align-items: center; justify-content: center; }
.modal-card { width: 600rpx; background: #fff; border-radius: 16rpx; padding: 40rpx; }
.modal-title { font-size: 32rpx; font-weight: 600; color: #333; text-align: center; margin-bottom: 30rpx; }
.modal-input { height: 80rpx; line-height: 80rpx; padding: 0 20rpx; border: 1px solid #e5e5e5; border-radius: 8rpx; font-size: 28rpx; }
.modal-btns { display: flex; gap: 20rpx; margin-top: 30rpx; }
.btn-cancel { flex: 1; height: 80rpx; line-height: 80rpx; background: #f5f5f5; color: #666; border-radius: 8rpx; font-size: 28rpx; border: none; }
.btn-confirm { flex: 1; height: 80rpx; line-height: 80rpx; background: #1677ff; color: #fff; border-radius: 8rpx; font-size: 28rpx; border: none; }
</style>
