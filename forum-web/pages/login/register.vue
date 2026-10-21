<template>
	<view class="register-page">
		<view class="reg-header">
			<text class="reg-title">注册账号</text>
		</view>

		<view class="reg-form">
			<view class="form-item">
				<text class="form-icon">👤</text>
				<input
					class="form-input"
					v-model="username"
					placeholder="请输入用户名"
				/>
			</view>
			<view class="form-item">
				<text class="form-icon">🔒</text>
				<input
					class="form-input"
					v-model="password"
					placeholder="请输入密码（至少6位）"
					:password="true"
				/>
			</view>
			<view class="form-item">
				<text class="form-icon">🔒</text>
				<input
					class="form-input"
					v-model="checkPassword"
					placeholder="请确认密码"
					:password="true"
				/>
			</view>

			<button class="btn-register" :disabled="loading" @click="onRegister">
				{{ loading ? '注册中...' : '注 册' }}
			</button>

			<view class="reg-options">
				<text class="option-link" @click="goLogin">已有账号？去登录</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { register } from '@/api/auth.js'

	export default {
		data() {
			return {
				username: '',
				password: '',
				checkPassword: '',
				loading: false
			}
		},
		methods: {
			async onRegister() {
				if (!this.username.trim()) {
					uni.showToast({ title: '请输入用户名', icon: 'none' })
					return
				}
				if (this.password.length < 6) {
					uni.showToast({ title: '密码至少6位', icon: 'none' })
					return
				}
				if (this.password !== this.checkPassword) {
					uni.showToast({ title: '两次密码不一致', icon: 'none' })
					return
				}

				this.loading = true
				try {
					const res = await register({
						username: this.username.trim(),
						password: this.password,
						checkPassword: this.checkPassword
					})
					if (res && res.code === 200) {
						uni.showToast({ title: '注册成功，请登录', icon: 'success' })
						setTimeout(() => {
							uni.navigateBack()
						}, 1500)
					}
				} catch (e) {
					uni.showToast({ title: '注册失败，请重试', icon: 'none' })
				}
				this.loading = false
			},
			goLogin() {
				uni.navigateBack()
			}
		}
	}
</script>

<style scoped>
	.register-page {
		min-height: 100vh;
		background: #fff;
		padding: 60rpx 40rpx;
	}
	.reg-header {
		margin-bottom: 60rpx;
	}
	.reg-title {
		font-size: 40rpx;
		font-weight: bold;
		color: #333;
	}
	.form-item {
		display: flex;
		align-items: center;
		height: 88rpx;
		border: 1px solid #e5e5e5;
		border-radius: 12rpx;
		padding: 0 20rpx;
		margin-bottom: 24rpx;
	}
	.form-icon {
		font-size: 36rpx;
		margin-right: 15rpx;
	}
	.form-input {
		flex: 1;
		height: 100%;
		font-size: 28rpx;
	}
	.btn-register {
		width: 100%;
		height: 88rpx;
		line-height: 88rpx;
		background: #007aff;
		color: #fff;
		border-radius: 12rpx;
		font-size: 32rpx;
		border: none;
		margin-top: 40rpx;
		margin-bottom: 30rpx;
	}
	.btn-register[disabled] {
		background: #ccc;
	}
	.reg-options {
		text-align: center;
	}
	.option-link {
		font-size: 26rpx;
		color: #1677ff;
	}
</style>
