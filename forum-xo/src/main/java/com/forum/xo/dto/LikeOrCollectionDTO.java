package com.forum.xo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LikeOrCollectionDTO implements Serializable {

    private String likeOrCollectionId;

    private String publishUid;

    // 1 点赞图片 2点赞评论  3收藏图片 4收藏专辑
    private Integer type;

    // 收藏到指定专辑（可选，仅 type=3 时生效）
    private String albumId;
}
