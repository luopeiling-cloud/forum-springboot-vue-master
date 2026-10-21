package com.forum.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forum.xo.entity.Category;
import com.forum.xo.vo.CategoryVo;
import com.forum.xo.vo.NoteVo;

import java.util.List;

/**
 * @author xiaozhao
 */
public interface CategoryService extends IService<Category> {
    /**
     * 获取树形分类数据
     *
     * @return 分类数据
     */
    List<CategoryVo> getCategoryTreeData();

}
