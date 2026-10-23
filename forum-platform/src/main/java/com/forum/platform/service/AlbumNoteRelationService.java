package com.forum.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forum.xo.entity.AlbumNoteRelation;
import com.forum.xo.vo.NoteSearchVo;
import com.forum.xo.vo.NoteVo;

public interface AlbumNoteRelationService extends IService<AlbumNoteRelation> {
    /**
     * 得到当前专辑下的所有笔记
     */
    Page<NoteSearchVo> getNotePageByAlbumId(long currentPage, long pageSize, String albumId, String userId);

    /**
     * 从专辑移除笔记（并取消收藏）
     */
    void removeNoteFromAlbum(String nid, String aid);

    /**
     * 移动笔记到另一个专辑
     */
    void moveNoteToAlbum(String nid, String fromAid, String toAid);
}
