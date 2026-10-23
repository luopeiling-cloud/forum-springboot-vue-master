package com.forum.xo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.xo.entity.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteDao extends BaseMapper<Note> {
}
