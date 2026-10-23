<template>
	<view class="login-page">
		<view class="login-header">
			<text class="login-title">校园论坛</text>
			<text class="login-subtitle">校园分享社区</text>
		</view>

		<view class="login-form">
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
					placeholder="请输入密码"
					:password="showPassword"
				/>
				<text class="form-icon-right" @click="showPassword = !showPassword">
					{{ showPassword ? '👁' : '👁‍🗨' }}
				</text>
			</view>

			<button class="btn-login" :disabled="loading" @click="onLogin">
				{{ loading ? '登录中...' : '登 录' }}
			</button>

			<view class="login-options">
				<text class="option-link" @click="goRegister">没有账号？立即注册</text>
			</view>
		</view>

		<view class="login-footer">
			<text class="footer-text">登录即代表同意 用户协议 和 隐私政策</text>
		</view>
	</view>
</template>

<script>
	import { login } from '@/api/auth.js'
	import userStore from '@/store/userStore.js'
	import wsManager from '@/utils/websocket.js'

	export default {
		data() {
			return {
				username: '',
				password: '',
				showPassword: true,
				loading: false
			}
		},
		methods: {
			async onLogin() {
				if (!this.username.trim()) {
					uni.showToast({ title: '请输入用户名', icon: 'none' })
					return
				}
				if (!this.password) {
					uni.showToast({ title: '请输入密码', icon: 'none' })
					return
				}

				this.loading = true
				try {
					const res = await login({
						username: this.username.trim(),
						password: this.password
					})
					if (res && res.code === 200) {
						const { accessToken, refreshToken, userInfo } = res.data || {}
						// 保存登录状态
						userStore.loginSuccess({
							accessToken,
							refreshToken,
							userInfo
						})
						// 连接 WebSocket
						if (userInfo && userInfo.id) {
							wsManager.connect(userInfo.id)
						}
						uni.showToast({ title: '登录成功', icon: 'success' })
						setTimeout(() => {
							uni.switchTab({ url: '/pages/discover/index' })
						}, 1000)
					}
				} catch (e) {
					uni.showToast({ title: '登录失败，请重试', icon: 'none' })
				}
				this.loading = false
			},
			goRegister() {
				uni.navigateTo({ url: '/pages/login/register' })
			}
		}
	}
</script>

<style scoped>
	.login-page {
		min-height: 100vh;
		background: #fff;
		padding: 80rpx 40rpx;
	}
	.login-header {
		text-align: center;
		margin-bottom: 80rpx;
	}
	.login-title {
		font-size: 48rpx;
		font-weight: bold;
		color: #1677ff;
		display: block;
	}
	.login-subtitle {
		font-size: 28rpx;
		color: #999;
		margin-top: 15rpx;
		display: block;
	}
	.login-form {
		padding: 0 20rpx;
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
	.form-icon-right {
		font-size: 30rpx;
		margin-left: 15rpx;
	}
	.form-input {
		flex: 1;
		height: 100%;
		font-size: 28rpx;
	}
	.btn-login {
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
	.btn-login[disabled] {
		background: #ccc;
	}
	.login-options {
		text-align: center;
	}
	.option-link {
		font-size: 26rpx;
		color: #1677ff;
	}
	.login-footer {
		position: fixed;
		bottom: 60rpx;
		left: 0;
		right: 0;
		text-align: center;
	}
	.footer-text {
		font-size: 24rpx;
		color: #ccc;
	}
</style>
