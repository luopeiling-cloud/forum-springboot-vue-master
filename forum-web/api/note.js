/**
 * 笔记/帖子 API
 * 路径前缀: /platform/note/
 */
import request, { BASE_URL } from '@/utils/request'

/**
 * 根据笔记 ID 获取笔记详情
 * @param {string} noteId
 * @returns {Promise} NoteVo（含用户信息、关注/点赞/收藏状态）
 */
export function getNoteById(noteId) {
	return request.get('/platform/note/getNoteById', { noteId })
}

/**
 * 保存/发布笔记
 * uni.uploadFile 一次只能传一个文件，因此先逐张直传七牛拿到 URL，
 * 再把 urls 放进 noteData 调保存接口（后端 uploadFiles 非必填，见 NoteController.saveNoteByDTO）
 * @param {Object} params - { noteData: JSON string, files: tempFilePath[] }
 */
export function saveNoteByDTO(params) {
	return new Promise((resolve, reject) => {
		const accessToken = uni.getStorageSync('accessToken') || ''
		const userInfo = uni.getStorageSync('userInfo')
		const userId = userInfo ? userInfo.id : ''
		const files = params.files || []

		if (files.length === 0) {
			reject(new Error('至少需要一张图片'))
			return
		}

		// 1 逐张上传到七牛（/util/oss/saveBatch/1，每张返回一个 URL）
		const uploadPromises = files.map((filePath) => {
			return new Promise((res, rej) => {
				uni.uploadFile({
					url: BASE_URL + '/util/oss/saveBatch/1',
					filePath: filePath,
					name: 'uploadFiles',
					header: {
						'accessToken': accessToken,
						'userId': userId
					},
					success: (uploadRes) => {
						try {
							const data = JSON.parse(uploadRes.data)
							if (data.code === 200 && data.data && data.data.length) {
								res(data.data[0])
							} else {
								rej(data)
							}
						} catch (e) {
							rej(e)
						}
					},
					fail: rej
				})
			})
		})

		// 2 全部上传成功后，带 urls 调保存接口（form-urlencoded，后端 @RequestParam 绑定）
		Promise.all(uploadPromises).then((urls) => {
			const noteData = JSON.parse(params.noteData)
			noteData.urls = urls
			uni.request({
				url: BASE_URL + '/platform/note/saveNoteByDTO',
				method: 'POST',
				header: {
					'Content-Type': 'application/x-www-form-urlencoded',
					'accessToken': accessToken,
					'userId': userId
				},
				data: { noteData: JSON.stringify(noteData) },
				success: (res) => {
					if (res.data && res.data.code === 200) {
						resolve(res.data)
					} else {
						reject(res.data)
					}
				},
				fail: reject
			})
		}).catch(reject)
	})
}

/**
 * 删除笔记
 * @param {Array<string>} noteIds
 */
export function deleteNoteByIds(noteIds) {
	return request.post('/platform/note/deleteNoteByIds', noteIds)
}
