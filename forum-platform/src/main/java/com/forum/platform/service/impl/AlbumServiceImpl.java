package com.forum.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.common.auth.AuthContextHolder;
import com.forum.common.utils.ConvertUtils;
import com.forum.platform.service.*;
import com.forum.xo.dao.AlbumDao;
import com.forum.xo.dto.AlbumDTO;
import com.forum.xo.entity.*;
import com.forum.xo.vo.AlbumVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class  AlbumServiceImpl extends ServiceImpl<AlbumDao, Album> implements AlbumService {

    @Autowired
    UserService userService;

    @Autowired
    AlbumNoteRelationService albumNoteRelationService;

    @Autowired
    LikeOrCollectionService likeOrCollectionService;

    @Autowired
    NoteService noteService;

    @Override
    public Page<Album> getAlbumPageByUserId(long currentPage, long pageSize,String userId) {
        return this.page(new Page<>(currentPage, pageSize), new QueryWrapper<Album>().eq("uid", userId).orderByDesc("update_date"));
    }

    @Override
    public void saveAlbumByDTO(AlbumDTO albumDTO) {
        Album album = ConvertUtils.sourceToTarget(albumDTO, Album.class);
        this.save(album);
    }

    @Override
    public AlbumVo getAlbumById(String id) {
        Album album = this.getById(id);
        AlbumVo albumVo = ConvertUtils.sourceToTarget(album, AlbumVo.class);
        User user = userService.getById(album.getUid());
        albumVo.setUsername(user.getUsername());
        albumVo.setAvatar(user.getAvatar());
        return albumVo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlbumById(String albumId) {
        // 1. 查询专辑下所有关联的笔记
        List<AlbumNoteRelation> relations = albumNoteRelationService.list(
                new QueryWrapper<AlbumNoteRelation>().eq("aid", albumId));
        List<String> nids = relations.stream().map(AlbumNoteRelation::getNid).collect(Collectors.toList());

        // 2. 删除关联关系
        albumNoteRelationService.remove(new QueryWrapper<AlbumNoteRelation>().eq("aid", albumId));

        // 3. 取消收藏每个笔记（删除 likeOrCollection 记录 + 更新 collectionCount）
        String currentUid = AuthContextHolder.getUserId();
        for (String nid : nids) {
            likeOrCollectionService.remove(new QueryWrapper<LikeOrCollection>()
                    .eq("uid", currentUid).eq("like_or_collection_id", nid).eq("type", 3));
            Note note = noteService.getById(nid);
            if (note != null) {
                note.setCollectionCount(Math.max(0, note.getCollectionCount() - 1));
                noteService.updateById(note);
            }
        }

        // 4. 删除专辑
        this.removeById(albumId);
    }


    @Override
    public void updateAlbumByDTO(AlbumDTO albumDTO) {
        Album album = ConvertUtils.sourceToTarget(albumDTO, Album.class);
        this.updateById(album);
    }

}
