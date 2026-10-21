package com.forum.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.common.utils.ConvertUtils;
import com.forum.common.utils.TreeUtils;
import com.forum.platform.service.CategoryService;
import com.forum.xo.dao.CategoryDao;
import com.forum.xo.entity.Category;
import com.forum.xo.vo.CategoryVo;
import com.forum.xo.vo.NoteVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, Category> implements CategoryService {
    @Override
    public List<CategoryVo> getCategoryTreeData() {
        List<Category> list = this.list(new QueryWrapper<Category>().orderByDesc("like_count"));
        List<CategoryVo> voList = ConvertUtils.sourceToTarget(list, CategoryVo.class);
        return TreeUtils.build(voList);
    }
}
