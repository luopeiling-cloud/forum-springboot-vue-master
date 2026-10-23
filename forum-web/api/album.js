/**
 * 专辑 API
 */
import request from '@/utils/request'

/**
 * 获取用户专辑列表
 */
export function getAlbumPageByUserId(currentPage, pageSize, userId) {
	return request.get(`/platform/album/getAlbumPageByUserId/${currentPage}/${pageSize}`, { userId })
}

/**
 * 创建专辑
 * @param {Object} data - { title, uid }
 */
export function saveAlbumByDTO(data) {
	return request.post('/platform/album/saveAlbumByDTO', data)
}

/**
 * 获取专辑详情
 */
export function getAlbumById(albumId) {
	return request.get('/platform/album/getAlbumById', { albumId })
}

/**
 * 删除专辑（同时取消收藏所有笔记）
 */
export function deleteAlbumById(albumId) {
	return request.get('/platform/album/deleteAlbumById', { albumId })
}

/**
 * 获取专辑下的笔记列表
 */
export function getNotePageByAlbumId(currentPage, pageSize, albumId, userId) {
	return request.get(`/platform/albumNoteRelation/getNotePageByAid/${currentPage}/${pageSize}`, { albumId, userId })
}

/**
 * 从专辑移除笔记（取消收藏）
 */
export function removeNoteFromAlbum(nid, aid) {
	return request.get('/platform/albumNoteRelation/removeNoteFromAlbum', { nid, aid })
}

/**
 * 移动笔记到另一个专辑
 */
export function moveNoteToAlbum(nid, fromAid, toAid) {
	return request.get('/platform/albumNoteRelation/moveNoteToAlbum', { nid, fromAid, toAid })
}
