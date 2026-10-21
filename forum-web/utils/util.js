/**
 * 工具函数集（从 forum-web 迁移）
 */
import { BASE_URL } from './request.js'

/**
 * 格式化日期：时间戳 → "YYYY-MM-DD HH:mm"
 */
function formatDate(t) {
	t = t || Date.now()
	const time = new Date(t)
	let str = time.getFullYear() + '-'
	str += (time.getMonth() < 9 ? '0' + (time.getMonth() + 1) : time.getMonth() + 1) + '-'
	str += (time.getDate() < 10 ? '0' + time.getDate() : time.getDate()) + ' '
	str += time.getHours() + ':'
	str += (time.getMinutes() < 10 ? '0' + time.getMinutes() : time.getMinutes())
	return str
}

/**
 * 距当前时间点的时长（相对时间）
 * @param {number} time - 13位时间戳
 * @returns {string} "刚刚" / "x秒前" / "x分钟前" / "x小时前" / "x天前" / "x月前" / 完整日期
 */
function formateTime(time) {
	if (!time) return ''
	const second = 1000
	const minute = second * 60
	const hour = minute * 60
	const day = hour * 24
	const month = day * 30
	const now = new Date().getTime()
	const diffValue = now - time

	const monthC = diffValue / month
	const dayC = diffValue / day
	const hourC = diffValue / hour
	const minC = diffValue / minute
	const secondC = diffValue / second

	if (monthC > 3) {
		return formatDate(time)
	} else if (monthC >= 1 && monthC <= 3) {
		return parseInt(monthC) + '月前'
	} else if (dayC >= 1) {
		return parseInt(dayC) + '天前'
	} else if (hourC >= 1) {
		return parseInt(hourC) + '小时前'
	} else if (minC >= 1) {
		return parseInt(minC) + '分钟前'
	} else if (secondC >= 1) {
		return parseInt(secondC) + '秒前'
	} else {
		return '刚刚'
	}
}

/**
 * 随机生成字符串
 * @param {number} len - 长度
 */
function getRandomString(len) {
	const _charStr = 'abacdefghjklmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ0123456789'
	const min = 0
	const max = _charStr.length - 1
	len = len || 15
	let _str = ''
	for (let i = 0; i < len; i++) {
		let index = Math.floor(Math.random() * (max - min + 1) + min)
		// 确保首字符不是数字
		const numStart = _charStr.length - 10
		if (i === 0 && index >= numStart) {
			index = Math.floor(Math.random() * (numStart - 1))
		}
		_str += _charStr[index]
	}
	return _str
}

/**
 * 获取完整图片 URL
 * @param {string} path - 图片路径
 * @returns {string}
 */
function getFullUrl(path) {
	if (!path) return '/static/default-cover.png'
	if (path.startsWith('http')) return path
	// 存量 /oss/xxx → 七牛外链（本地图片已按去掉 /oss/ 前缀的原样 key 迁移至 forum-photo）
	// /oss/2026/07/03/x.jpg → http://tmo6yqvdl.hn-bkt.clouddn.com/2026/07/03/x.jpg
	if (path.startsWith('/oss/')) return 'http://tmo6yqvdl.hn-bkt.clouddn.com/' + path.slice(5)
	// 其余本地路径由 platform 服务静态资源映射提供（兜底）
	return BASE_URL + '/platform' + path
}

/**
 * 格式化数字（过万显示 "1.2w"）
 */
function formatCount(num) {
	if (!num) return '0'
	if (num >= 10000) {
		return (num / 10000).toFixed(1) + 'w'
	}
	return num.toString()
}

export {
	formatDate,
	formateTime,
	getRandomString,
	getFullUrl,
	formatCount
}
