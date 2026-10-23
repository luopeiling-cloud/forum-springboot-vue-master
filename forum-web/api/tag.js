/**
 * 标签 API
 * 路径前缀: /platform/tag/
 */
import request from '@/utils/request'

/**
 * 根据关键词搜索标签（分页）
 * @param {number} currentPage
 * @param {number} pageSize
 * @param {string} keyword
 */
export function getPageTagByKeyword(currentPage, pageSize, keyword) {
	return request.get(`/platform/tag/getPageTagByKeyword/${currentPage}/${pageSize}`, { keyword })
}
