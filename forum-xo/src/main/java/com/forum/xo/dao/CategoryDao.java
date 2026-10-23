package com.forum.xo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.xo.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryDao extends BaseMapper<Category> {
}
