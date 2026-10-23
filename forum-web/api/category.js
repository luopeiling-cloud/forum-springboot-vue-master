/**
 * 分类 API
 * 路径前缀: /platform/category/
 */
import request from '@/utils/request'

/**
 * 获取分类树数据
 * @returns {Promise} 树形分类列表
 */
export function getCategoryTreeData() {
	return request.get('/platform/category/getCategoryTreeData')
}
