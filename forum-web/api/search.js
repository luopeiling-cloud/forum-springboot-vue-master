/**
 * 搜索 API
 * 路径前缀: /search/
 */
import request from '@/utils/request'

/**
 * 获取推荐笔记（瀑布流首页）
 * @param {number} currentPage
 * @param {number} pageSize
 */
export function getRecommendNotePage(currentPage, pageSize) {
	return request.get(`/search/note/getRecommendNotePage/${currentPage}/${pageSize}`)
}

/**
 * 按条件搜索笔记
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {Object} data - NoteDTO { keyword, type, cid, cpid }
 *   type: 0=综合, 1=按点赞排序, 2=按时间排序
 */
export function getNotePageByDTO(currentPage, pageSize, data) {
	return request.post(`/search/note/getNotePageByDTO/${currentPage}/${pageSize}`, data)
}

/**
 * 根据关键词获取搜索建议
 * @param {string} keyword
 */
export function getRecordByKeyWord(keyword) {
	return request.get('/search/record/getRecordByKeyWord', { keyword })
}

/**
 * 获取热搜榜
 */
export function getHotRecord() {
	return request.get('/search/record/getHotRecord')
}

/**
 * 记录搜索词
 * @param {string} keyword
 */
export function addRecord(keyword) {
	return request.get('/search/record/addRecord', { keyword })
}
