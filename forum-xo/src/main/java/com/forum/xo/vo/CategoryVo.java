package com.forum.xo.vo;

import com.forum.common.utils.TreeNode;
import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryVo extends TreeNode<CategoryVo> implements Serializable {
    private String title;
    private long likeCount;
}
