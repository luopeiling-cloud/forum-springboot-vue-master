package com.forum.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forum.common.entity.BaseEntity;
import lombok.Data;

@Data
@TableName("t_tag")
public class Tag extends BaseEntity {

    private Long likeCount;

    private String title;

    private Integer sort;
}
