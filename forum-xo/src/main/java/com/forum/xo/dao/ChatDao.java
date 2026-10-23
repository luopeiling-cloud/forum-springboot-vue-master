package com.forum.xo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.xo.entity.Chat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatDao extends BaseMapper<Chat> {
}
