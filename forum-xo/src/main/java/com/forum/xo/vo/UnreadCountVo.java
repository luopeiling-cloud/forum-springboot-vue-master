package com.forum.xo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UnreadCountVo implements Serializable {
    private Long likeCount;
    private Long commentCount;
    private Long collectionCount;
    private Long followCount;
}
