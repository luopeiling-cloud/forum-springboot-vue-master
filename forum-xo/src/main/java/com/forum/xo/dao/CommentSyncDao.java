package com.forum.xo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.xo.entity.CommentSync;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentSyncDao extends BaseMapper<CommentSync> {
}
