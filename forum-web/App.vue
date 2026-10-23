<script>
	import wsManager from '@/utils/websocket.js'

	export default {
		globalData: {
			// 全局共享数据
		},
		onLaunch: function() {
			console.log('校园论坛 App Launch')

			// 检查登录状态：如果没有 token 则跳转登录页
			const accessToken = uni.getStorageSync('accessToken')
			if (!accessToken) {
				// 延迟跳转，确保 App 初始化完成
				setTimeout(() => {
					uni.reLaunch({
						url: '/pages/login/login'
					})
				}, 100)
			} else {
				// 已登录，连接 WebSocket
				const userInfo = uni.getStorageSync('userInfo')
				if (userInfo) {
					try {
						const info = typeof userInfo === 'string' ? JSON.parse(userInfo) : userInfo
						if (info.id) {
							wsManager.connect(info.id)
						}
					} catch (e) {}
				}
			}
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	/* 全局公共样式 */
	page {
		background-color: #f5f5f5;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
		font-size: 28rpx;
		color: #333333;
		-webkit-font-smoothing: antialiased;
	}

	/* 通用容器 */
	.container {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
	}

	/* 白色卡片 */
	.card {
		background: #fff;
		border-radius: 8rpx;
		padding: 20rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
	}

	/* 通用按钮 */
	.btn-primary {
		width: 100%;
		height: 88rpx;
		line-height: 88rpx;
		background-color: #007aff;
		color: #fff;
		text-align: center;
		border-radius: 8rpx;
		font-size: 32rpx;
		border: none;
	}
	.btn-primary:active {
		background-color: #0066cc;
	}

	/* 单行省略 */
	.ellipsis {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	/* 两行省略 */
	.ellipsis-2 {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}

	/* flex 常用 */
	.flex-center {
		display: flex;
		align-items: center;
		justify-content: center;
	}
	.flex-between {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	/* 安全区适配 */
	.safe-bottom {
		padding-bottom: constant(safe-area-inset-bottom);
		padding-bottom: env(safe-area-inset-bottom);
	}
</style>
