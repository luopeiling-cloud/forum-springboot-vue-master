package com.forum.platform.controller;

import com.forum.common.result.Result;
import com.forum.platform.service.CategoryService;
import com.forum.xo.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/category")
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取树形分类数据
     * @return 分类数据
     */
    @GetMapping("getCategoryTreeData")
    public Result<?> getCategoryTreeData() {
        List<CategoryVo> categoryList = categoryService.getCategoryTreeData();
        return Result.ok(categoryList);
    }
}
