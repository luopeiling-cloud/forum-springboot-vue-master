package com.forum.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.common.auth.AuthContextHolder;
import com.forum.common.utils.ConvertUtils;
import com.forum.platform.service.*;
import com.forum.xo.dao.AlbumNoteRelationDao;
import com.forum.xo.entity.*;
import com.forum.xo.vo.NoteSearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlbumNoteRelationServiceImpl extends ServiceImpl<AlbumNoteRelationDao, AlbumNoteRelation> implements AlbumNoteRelationService {

    @Autowired
    NoteService noteService;

    @Autowired
    UserService userService;

    @Autowired
    LikeOrCollectionService likeOrCollectionService;

    @Autowired
    AlbumService albumService;

    @Override
    public Page<NoteSearchVo> getNotePageByAlbumId(long currentPage, long pageSize, String albumId, String userId) {
        Page<NoteSearchVo> result = new Page<>();
        Page<AlbumNoteRelation> albumImgRelationPage = this.page(new Page<>(currentPage, pageSize), new QueryWrapper<AlbumNoteRelation>().eq("aid", albumId).orderByDesc("create_date"));
        long total = albumImgRelationPage.getTotal();
        List<AlbumNoteRelation> records = albumImgRelationPage.getRecords();
        List<String> nids = records.stream().map(AlbumNoteRelation::getNid).collect(Collectors.toList());
        String currentUser = AuthContextHolder.getUserId();
        List<Note> noteList;
        if(currentUser.equals(userId)){
            noteList = noteService.listByIds(nids);
        }else{
            noteList = noteService.list(new QueryWrapper<Note>().in("id",nids).eq("status",1));
        }
        List<String> uids = noteList.stream().map(Note::getUid).collect(Collectors.toList());
        List<User> userList = userService.listByIds(uids);
        HashMap<String, User> userMap = new HashMap<>(16);
        userList.forEach(item -> {
            userMap.put(item.getId(), item);
        });
        HashMap<String, Note> noteMap = new HashMap<>(16);
        noteList.forEach(item -> {
            noteMap.put(item.getId(), item);
        });
        List<NoteSearchVo> noteVoList = new ArrayList<>();
        Note note;
        User user;
        NoteSearchVo noteSearchVo;
        for (AlbumNoteRelation model : records) {
            note = noteMap.get(model.getNid());
            if (note == null) continue;
            user = userMap.get(note.getUid());
            noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
            noteSearchVo.setUsername(user.getUsername())
                    .setAvatar(user.getAvatar());
            noteVoList.add(noteSearchVo);
        }
        result.setTotal(total);
        result.setRecords(noteVoList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeNoteFromAlbum(String nid, String aid) {
        String currentUid = AuthContextHolder.getUserId();
        // 删除专辑-笔记关联
        this.remove(new QueryWrapper<AlbumNoteRelation>().eq("nid", nid).eq("aid", aid));
        // 取消收藏
        likeOrCollectionService.remove(new QueryWrapper<LikeOrCollection>()
                .eq("uid", currentUid).eq("like_or_collection_id", nid).eq("type", 3));
        // 更新笔记收藏数
        Note note = noteService.getById(nid);
        if (note != null) {
            note.setCollectionCount(Math.max(0, note.getCollectionCount() - 1));
            noteService.updateById(note);
            // 更新专辑图片数量
            int count = (note.getCount() != null) ? note.getCount() : 1;
            Album album = albumService.getById(aid);
            if (album != null) {
                album.setImgCount(Math.max(0, album.getImgCount() - count));
                albumService.updateById(album);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveNoteToAlbum(String nid, String fromAid, String toAid) {
        AlbumNoteRelation relation = this.getOne(new QueryWrapper<AlbumNoteRelation>()
                .eq("nid", nid).eq("aid", fromAid));
        if (relation != null) {
            relation.setAid(toAid);
            this.updateById(relation);

            // 获取笔记的图片数量
            Note note = noteService.getById(nid);
            int count = (note != null && note.getCount() != null) ? note.getCount() : 1;

            // 源专辑数量 -count
            Album fromAlbum = albumService.getById(fromAid);
            if (fromAlbum != null) {
                fromAlbum.setImgCount(Math.max(0, fromAlbum.getImgCount() - count));
                albumService.updateById(fromAlbum);
            }

            // 目标专辑数量 +count
            Album toAlbum = albumService.getById(toAid);
            if (toAlbum != null) {
                toAlbum.setImgCount(toAlbum.getImgCount() + count);
                albumService.updateById(toAlbum);
            }
        }
    }
}
