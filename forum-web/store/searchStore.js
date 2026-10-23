/**
 * 搜索状态管理
 * 从 forum-web searchStore.ts 迁移
 */

const state = {
	keyWord: '',
	seed: '',
	historyRecords: []
}

// 初始化
function initFromStorage() {
	const keyword = uni.getStorageSync('keyword')
	const seed = uni.getStorageSync('seed')
	const records = uni.getStorageSync('historyRecords')
	if (keyword) state.keyWord = keyword
	if (seed) state.seed = seed
	if (records) {
		try {
			state.historyRecords = typeof records === 'string' ? JSON.parse(records) : records
		} catch (e) {
			state.historyRecords = []
		}
	}
}

const searchStore = {
	state,

	init() {
		initFromStorage()
	},

	/** 设置搜索关键词 */
	setKeyword(val) {
		state.keyWord = val
		uni.setStorageSync('keyword', val)
	},

	/** 设置随机种子（触发响应式更新） */
	setSeed(val) {
		state.seed = val
		uni.setStorageSync('seed', val)
	},

	/** 添加搜索记录 */
	pushRecord(val) {
		let records = this.getRecords()
		if (!records) records = []
		// 去重
		records = records.filter(item => item !== val)
		// 插到最前面
		records.unshift(val)
		// 最多保留 10 条
		if (records.length > 10) {
			records = records.slice(0, 10)
		}
		state.historyRecords = records
		uni.setStorageSync('historyRecords', JSON.stringify(records))
	},

	/** 获取搜索历史 */
	getRecords() {
		const records = uni.getStorageSync('historyRecords')
		if (!records) return []
		try {
			return typeof records === 'string' ? JSON.parse(records) : records
		} catch (e) {
			return []
		}
	},

	/** 清空历史 */
	clearAllRecord() {
		state.historyRecords = []
		uni.setStorageSync('historyRecords', JSON.stringify([]))
	},

	/** 删除某条记录 */
	deleteRecord(index) {
		const records = this.getRecords()
		records.splice(index, 1)
		state.historyRecords = records
		uni.setStorageSync('historyRecords', JSON.stringify(records))
	}
}

initFromStorage()

export default searchStore
