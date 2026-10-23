<template>
	<view class="setting-page">
		<view class="menu-section">
			<view class="menu-item" @click="goEditProfile">
				<text>编辑资料</text>
				<text class="menu-arrow">></text>
			</view>
			<view class="menu-item" @click="goAccount">
				<text>账号与安全</text>
				<text class="menu-arrow">></text>
			</view>
		</view>

		<view class="menu-section">
			<view class="menu-item" @click="showAbout">
				<text>关于我们</text>
				<text class="menu-arrow">></text>
			</view>
			<view class="menu-item" @click="showPrivacy">
				<text>隐私协议</text>
				<text class="menu-arrow">></text>
			</view>
			<view class="menu-item" @click="showHelp">
				<text>帮助与客服</text>
				<text class="menu-arrow">></text>
			</view>
		</view>

		<view class="menu-section">
			<view class="menu-item">
				<text>深色模式</text>
				<switch :checked="darkMode" @change="toggleDarkMode" color="#1677ff" />
			</view>
		</view>

		<view class="logout-area">
			<button class="btn-logout" @click="onLogout">退出登录</button>
		</view>
	</view>
</template>

<script>
	import { requireLogin } from '@/utils/auth.js'
	import userStore from '@/store/userStore.js'
	import wsManager from '@/utils/websocket.js'
	import { loginOut } from '@/api/auth.js'

	export default {
		onLoad() {
			if (!requireLogin()) return
		},

		data() {
			return {
				darkMode: false
			}
		},
		methods: {
			goEditProfile() {
				uni.navigateTo({ url: '/pages/profile/edit' })
			},
			goAccount() {
				uni.navigateTo({ url: '/pages/profile/security' })
			},
			showAbout() {
				uni.showModal({
					title: '关于校园论坛',
					content: '校园论坛 v1.0\n一个属于校园的分享社区',
					showCancel: false
				})
			},
			showPrivacy() {
				uni.showModal({
					title: '隐私协议',
					content: '我们重视你的隐私...',
					showCancel: false
				})
			},
			showHelp() {
				uni.showModal({
					title: '帮助与客服',
					content: '如有问题请发送邮件至 support@example.com',
					showCancel: false
				})
			},
			toggleDarkMode(e) {
				this.darkMode = e.detail.value
				uni.showToast({ title: '深色模式开发中', icon: 'none' })
			},
			onLogout() {
				uni.showModal({
					title: '提示',
					content: '确定要退出登录吗？',
					success: async (res) => {
						if (res.confirm) {
							try {
								const userId = userStore.getUserId()
								if (userId) {
									await loginOut(userId)
								}
							} catch (e) {}
							wsManager.close()
							userStore.loginOut()
							uni.reLaunch({ url: '/pages/discover/index' })
						}
					}
				})
			}
		}
	}
</script>

<style scoped>
	.setting-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding-top: 20rpx;
	}
	.menu-section {
		background: #fff;
		margin-bottom: 20rpx;
	}
	.menu-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		height: 98rpx;
		padding: 0 30rpx;
		font-size: 28rpx;
		color: #333;
		border-bottom: 1px solid #f5f5f5;
	}
	.menu-item:last-child {
		border-bottom: none;
	}
	.menu-arrow {
		color: #ccc;
	}
	.logout-area {
		padding: 60rpx 30rpx;
	}
	.btn-logout {
		width: 100%;
		height: 88rpx;
		line-height: 88rpx;
		background: #fff;
		color: #f53f3f;
		border-radius: 8rpx;
		font-size: 32rpx;
		border: 1px solid #f53f3f;
	}
</style>
