package com.forum.xo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.xo.entity.Event;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventDao extends BaseMapper<Event> {
}
