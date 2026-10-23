package com.forum.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.result.Result;
import com.forum.platform.service.AlbumNoteRelationService;
import com.forum.xo.vo.NoteSearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/albumNoteRelation")
@RestController
public class AlbumNoteRelationController {

    @Autowired
    AlbumNoteRelationService albumNoteRelationService;

    /**
     * 得到当前专辑下的所有笔记
     */
    @RequestMapping("getNotePageByAid/{currentPage}/{pageSize}")
    public Result<?> getNotePageByAlbumId(@PathVariable long currentPage, @PathVariable long pageSize, String albumId, String userId) {
        Page<NoteSearchVo> pageInfo = albumNoteRelationService.getNotePageByAlbumId(currentPage, pageSize, albumId, userId);
        return Result.ok(pageInfo);
    }

    /**
     * 从专辑中移除笔记（同时取消收藏）
     */
    @RequestMapping("removeNoteFromAlbum")
    public Result<?> removeNoteFromAlbum(String nid, String aid) {
        albumNoteRelationService.removeNoteFromAlbum(nid, aid);
        return Result.ok();
    }

    /**
     * 移动笔记到另一个专辑
     */
    @RequestMapping("moveNoteToAlbum")
    public Result<?> moveNoteToAlbum(String nid, String fromAid, String toAid) {
        albumNoteRelationService.moveNoteToAlbum(nid, fromAid, toAid);
        return Result.ok();
    }
}
