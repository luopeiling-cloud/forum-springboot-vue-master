<template>
	<view class="comment-input-bar safe-bottom">
		<view class="input-wrapper">
			<input
				class="comment-input"
				v-model="content"
				:placeholder="placeholder"
				confirm-type="send"
				@confirm="onSend"
				@input="onInput"
			/>
			<view class="clear-btn" v-if="content.length > 0" @click="onClear">
				<text>✕</text>
			</view>
		</view>
		<button class="send-btn" :disabled="!content.trim()" @click="onSend">发送</button>
	</view>
</template>

<script>
	export default {
		name: 'CommentInput',
		props: {
			placeholder: {
				type: String,
				default: '请输入内容'
			},
			replyInfo: {
				type: Object,
				default: null
			}
		},
		data() {
			return {
				content: ''
			}
		},
		watch: {
			replyInfo(val) {
				if (val && val.username) {
					this.placeholder = '回复 ' + val.username
				}
			}
		},
		methods: {
			onInput(e) {
				this.content = e.detail.value
			},
			onClear() {
				this.content = ''
				this.$emit('clear')
			},
			onSend() {
				const text = this.content.trim()
				if (!text) return
				this.$emit('send', text)
				this.content = ''
			}
		}
	}
</script>

<style scoped>
	.comment-input-bar {
		display: flex;
		align-items: center;
		padding: 15rpx 20rpx;
		background: #fff;
		border-top: 1px solid #f5f5f5;
	}
	.input-wrapper {
		flex: 1;
		position: relative;
		display: flex;
		align-items: center;
	}
	.comment-input {
		flex: 1;
		height: 70rpx;
		padding: 0 40rpx 0 20rpx;
		border: 1px solid #eee;
		border-radius: 35rpx;
		font-size: 26rpx;
		background: #fff;
	}
	.clear-btn {
		position: absolute;
		right: 15rpx;
		width: 30rpx;
		height: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		color: #999;
	}
	.send-btn {
		width: 120rpx;
		height: 70rpx;
		line-height: 70rpx;
		background: #007aff;
		color: #fff;
		border-radius: 35rpx;
		font-size: 26rpx;
		margin-left: 15rpx;
		border: none;
		padding: 0;
		text-align: center;
	}
	.send-btn[disabled] {
		background: #ccc;
		opacity: 0.8;
	}
</style>
