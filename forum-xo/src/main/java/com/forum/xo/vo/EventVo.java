package com.forum.xo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class EventVo implements Serializable {
    private String id;
    private String fromUid;
    private String fromUsername;
    private String fromAvatar;
    private Integer eventType;
    private Integer targetType;
    private String targetId;
    private Integer readStatus;
    private Long time;
    // 笔记相关
    private String noteCover;
    private String noteTitle;
    // 评论相关
    private String content;
    // 用于跳转
    private String nid;
    private String url;
}
