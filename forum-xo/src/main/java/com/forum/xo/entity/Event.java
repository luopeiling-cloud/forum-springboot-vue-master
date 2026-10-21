package com.forum.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forum.common.entity.BaseEntity;
import lombok.Data;

@Data
@TableName("t_event")
public class Event extends BaseEntity {
    private String fromUid;
    private String toUid;
    private Integer eventType;
    private Integer targetType;
    private String targetId;
    private Integer readStatus;
    private String url;
}
