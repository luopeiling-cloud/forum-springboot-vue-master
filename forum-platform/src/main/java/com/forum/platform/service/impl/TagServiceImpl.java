package com.forum.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.platform.service.TagService;
import com.forum.xo.dao.TagDao;
import com.forum.xo.dto.TagDTO;
import com.forum.xo.entity.Tag;
import com.forum.xo.vo.NoteVo;
import com.forum.xo.vo.TagVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TagServiceImpl extends ServiceImpl<TagDao, Tag> implements TagService {
    @Override
    public List<TagVo> getHotTagList() {
        return null;
    }

    @Override
    public Page<NoteVo> getNotePageByTagId(long currentPage, long pageSize, String tagId, Integer type) {
        return null;
    }

    @Override
    public void saveTagByDTO(TagDTO tagDTO) {
        // 空实现，标签创建走 findOrCreateByTitle
    }

    @Override
    public Tag findOrCreateByTitle(String title) {
        Tag tag = this.getOne(new QueryWrapper<Tag>().eq("title", title));
        if (tag != null) {
            log.info("[findOrCreateByTitle] found existing tag '{}' id={}", title, tag.getId());
            return tag;
        }
        tag = new Tag();
        tag.setTitle(title);
        tag.setLikeCount(0L);
        tag.setSort(0);
        boolean ok = this.save(tag);
        log.info("[findOrCreateByTitle] created new tag '{}' id={} saved={}", title, tag.getId(), ok);
        return tag;
    }

    @Override
    public Page<Tag> getPageTagByKeyword(long currentPage, long pageSize, String keyword) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like("title", keyword);
        }
        queryWrapper.orderByDesc("like_count");
        return this.page(new Page<Tag>((int) currentPage, (int) pageSize), queryWrapper);
    }
}
