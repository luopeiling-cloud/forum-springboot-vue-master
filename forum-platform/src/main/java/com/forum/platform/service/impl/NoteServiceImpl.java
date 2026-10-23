package com.forum.platform.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.common.auth.AuthContextHolder;
import com.forum.common.exception.ForumException;
import com.forum.common.result.Result;
import com.forum.common.utils.ConvertUtils;
import com.forum.platform.client.EsClient;
import com.forum.platform.client.OssClient;
import com.forum.platform.service.*;
import com.forum.xo.dao.NoteDao;
import com.forum.xo.dto.NoteDTO;
import com.forum.xo.entity.*;
import com.forum.xo.vo.NoteSearchVo;
import com.forum.xo.vo.NoteVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NoteServiceImpl extends ServiceImpl<NoteDao, Note> implements NoteService {
    @Autowired
    UserService userService;

    @Autowired
    TagNoteRelationService tagNoteRelationService;

    @Autowired
    TagService tagService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    EsClient esClient;

    @Autowired
    FollowerService followerService;

    @Autowired
    LikeOrCollectionService likeOrCollectionService;

    @Autowired
    OssClient ossClient;


    @Override
    public NoteVo getNoteById(String noteId) {
        Note note = this.getById(noteId);
        note.setViewCount(note.getViewCount() + 1);
        User user = userService.getById(note.getUid());
        NoteVo noteVo = ConvertUtils.sourceToTarget(note, NoteVo.class);
        noteVo.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setTime(note.getUpdateDate().getTime());

        boolean follow = followerService.isFollow(user.getId());
        noteVo.setIsFollow(follow);

        String currentUid = AuthContextHolder.getUserId();
        List<LikeOrCollection> likeOrCollectionList = likeOrCollectionService.list(new QueryWrapper<LikeOrCollection>().eq("like_or_collection_id", noteId).eq("uid", currentUid));
        if(!likeOrCollectionList.isEmpty()) {
            Set<Integer> types = likeOrCollectionList.stream().map(LikeOrCollection::getType).collect(Collectors.toSet());
            noteVo.setIsLike(types.contains(1));
            noteVo.setIsCollection(types.contains(3));
        }

        //得到标签
        List<TagNoteRelation> tagNoteRelationList = tagNoteRelationService.list(new QueryWrapper<TagNoteRelation>().eq("nid", noteId));
        List<String> tids = tagNoteRelationList.stream().map(TagNoteRelation::getTid).collect(Collectors.toList());

        if (!tids.isEmpty()) {
            List<Tag> tagList = tagService.listByIds(tids);
            noteVo.setTagList(tagList);
        }

        this.updateById(note);
        return noteVo;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveNoteByDTO(String noteData, MultipartFile[] files) {
        String currentUid = AuthContextHolder.getUserId();
        NoteDTO noteDTO = JSONUtil.toBean(noteData, NoteDTO.class);
        Note note =ConvertUtils.sourceToTarget(noteDTO, Note.class);
        note.setUid(currentUid);
        boolean  save = this.save(note);
        if(!save){
            return null;
        }
        // TODO 需要往专辑中添加

        User user = userService.getById(currentUid);
        user.setTrendCount((user.getTrendCount() == null ? 0L : user.getTrendCount()) + 1);
        userService.updateById(user);

        List<String> tagTitles = noteDTO.getTagList();
        log.info("[saveNoteByDTO] tagTitles from DTO: {}", tagTitles);
        List<TagNoteRelation> tagNoteRelationList = new ArrayList<>();
        List<String> tagTitleList = new ArrayList<>();

        if (tagTitles != null && !tagTitles.isEmpty()) {
            log.info("[saveNoteByDTO] processing {} tags", tagTitles.size());
            for (String title : tagTitles) {
                String t = title.trim();
                if (t.isEmpty()) continue;
                // 查找或创建标签（标题唯一）
                Tag tag = tagService.findOrCreateByTitle(t);
                log.info("[saveNoteByDTO] tag '{}' -> id={}", t, tag.getId());
                tagTitleList.add(t);
                TagNoteRelation tagNoteRelation = new TagNoteRelation();
                tagNoteRelation.setTid(tag.getId());
                tagNoteRelation.setNid(note.getId());
                tagNoteRelationList.add(tagNoteRelation);
            }
            if (!tagNoteRelationList.isEmpty()) {
                tagNoteRelationService.saveBatch(tagNoteRelationList);
                log.info("[saveNoteByDTO] saved {} tag relations for note {}", tagNoteRelationList.size(), note.getId());
            }
        } else {
            log.warn("[saveNoteByDTO] tagTitles is null or empty!");
        }
        String tags = String.join(",", tagTitleList);
        Category category = categoryService.getById(note.getCid());
        Category parentCategory = categoryService.getById(note.getCpid());

        List<String> dataList;
        if (files == null || files.length == 0) {
            // 前端已逐张直传七牛并在 noteData 中携带 urls（uni.uploadFile 一次只能传一个文件，多图需分步上传）
            dataList = noteDTO.getUrls();
            if (dataList == null || dataList.isEmpty()) {
                throw new ForumException("添加图片失败");
            }
        } else {
            try {
                Result<List<String>> result = ossClient.saveBatch(files, 1);
                dataList = result.getData();
            }catch (Exception e){
               log.error("[saveNoteByDTO] 图片上传失败", e);
               throw new ForumException("添加图片失败");
            }
        }
        String[] urlArr = dataList.toArray(new String[dataList.size()]);
        String urls = JSONUtil.toJsonStr(urlArr);
        note.setUrls(urls);
        note.setNoteCover(urlArr[0]);
        this.updateById(note);

        // 往es中添加数据
        NoteSearchVo noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
        noteSearchVo.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setLikeCount(0L)
                .setCategoryName(category.getTitle())
                .setCategoryParentName(parentCategory.getTitle())
                .setTags(tags)
                .setTime(note.getUpdateDate().getTime());
        esClient.addNote(noteSearchVo);
        return note.getId();
    }

    @Override
    public void deleteNoteByIds(List<String> noteIds) {

    }

    @Override
    public String updateNoteByDTO(NoteDTO noteDTO) {
        return null;
    }

    @Override
    public Page<NoteVo> getHotPage(long currentPage, long pageSize) {
        return null;
    }
}
