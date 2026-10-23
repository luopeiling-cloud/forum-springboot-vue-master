package com.forum.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.platform.service.TagNoteRelationService;
import com.forum.xo.dao.TagNoteRelationDao;
import com.forum.xo.entity.TagNoteRelation;
import org.springframework.stereotype.Service;

@Service
public class TagNoteRelationServiceImpl extends ServiceImpl<TagNoteRelationDao, TagNoteRelation> implements TagNoteRelationService {
}
