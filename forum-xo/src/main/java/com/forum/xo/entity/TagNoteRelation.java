package com.forum.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forum.common.entity.BaseEntity;
import lombok.Data;

/**
 * @author xiaozhao
 */
@Data
@TableName("t_tag_note_relation")
public class TagNoteRelation extends BaseEntity {

    private String nid;

    private String tid;
}
